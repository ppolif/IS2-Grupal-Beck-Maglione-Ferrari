package com.example.zero.controllers;

import com.example.zero.entidades.compra.Factura;
import com.example.zero.services.FacturaPdfService;
import com.example.zero.services.VentaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// controlador para emitir facturas de compra a los clientes
// funcionamiento: nos redirige a otra pestaña donde podemos ver el pdf, desde ahi lo podremos descargar
@Controller
public class FacturaPdfController {

    private static final Logger logger = LoggerFactory.getLogger(FacturaPdfController.class);

    private final VentaService ventaService;
    private final FacturaPdfService facturaPdfService;

    @Autowired
    public FacturaPdfController(VentaService ventaService, FacturaPdfService facturaPdfService) {
        this.ventaService = ventaService;
        this.facturaPdfService = facturaPdfService;
    }

    // endpoint para que el cliente final vea y descargue su factura en formato pdf
    @GetMapping("/shop/facturas/{id}/pdf")
    public ResponseEntity<byte[]> verFacturaPdfCliente(@PathVariable("id") String id) {
        return generarRespuestaPdf(id);
    }

    // endpoint para que el administrador vea y descargue la factura en formato pdf

    @GetMapping("/admin/orders/{id}/factura/pdf")
    public ResponseEntity<byte[]> verFacturaPdfAdmin(@PathVariable("id") String id) {
        return generarRespuestaPdf(id);
    }

    /**
     * Metodo auxiliar para buscar la factura, invocar el motor de JasperReports
     * y empaquetar los bytes del PDF en un ResponseEntity con las cabeceras HTTP correctas.
     */
    private ResponseEntity<byte[]> generarRespuestaPdf(String id) {
        try {
            Factura factura = ventaService.buscarFacturaPorIdentificador(id);
            if (factura == null) {
                logger.warn("No se encontro la factura para generar el PDF con identificador: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(("Factura u orden no encontrada: " + id).getBytes());
            }

            // generamos el archivo PDF mediante JasperReports
            byte[] pdfBytes = facturaPdfService.generarFacturaPdf(factura);

            // determinamos nombre del archivo pdf usando el prefijo #ORD-XXXX
            String nombreArchivo;
            if (factura.getNumeroFactura() != null) {
                nombreArchivo = "Factura_ORD-" + factura.getNumeroFactura() + ".pdf";
            } else {
                nombreArchivo = "Factura_ORD-" + factura.getId() + ".pdf";
            }

            // construir encabezados HTTP para visualizacion inline en nueva pestana
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("inline", nombreArchivo);
            headers.set("Content-Disposition", "inline; filename=\"" + nombreArchivo + "\"");
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            logger.error("Error al procesar y emitir el PDF de la factura con ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Error interno al generar el PDF de la factura: " + e.getMessage()).getBytes());
        }
    }
}
