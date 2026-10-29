package com.DulceMayor.backend.controllers.facturacion;

import com.DulceMayor.backend.controllers.BaseController;
import com.DulceMayor.backend.dtos.facturacion.FacturaMiCuentaPage;
import com.DulceMayor.backend.dtos.facturacion.FacturaPage;
import com.DulceMayor.backend.dtos.facturacion.FacturaRequest;
import com.DulceMayor.backend.dtos.facturacion.FacturaResponse;
import com.DulceMayor.backend.services.facturacion.FacturaService;
import java.time.LocalDate;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para la consulta, anulación, descarga PDF, QR de pago y reenvío por correo de facturas.
 *
 * <p><b>Patrón GoF aplicado: Template Method (Comportamiento)</b></p>
 * <ul>
 *   <li><b>Participante GoF:</b> ConcreteClass que extiende {@link BaseController}.</li>
 *   <li><b>Implementación:</b> Reutiliza {@link #ejecutarBuscar(String)} para la búsqueda individual de factura
 *       y expone los endpoints específicos de dominio definidos en {@code 04-openapi.yaml}.</li>
 * </ul>
 */
@RestController
public class FacturaController extends BaseController<FacturaService, FacturaRequest, FacturaResponse> {

    public FacturaController(FacturaService facturaService) {
        super(facturaService);
    }

    @GetMapping({"/facturas", "/api/v1/facturas"})
    @PreAuthorize("hasAnyRole('JEFE', 'ADMINISTRATIVO')")
    public ResponseEntity<FacturaPage> listarFactura(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String minoristaId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            Pageable pageable) {
        return ResponseEntity.ok(service.listarPaginado(estado, minoristaId, desde, hasta, pageable));
    }

    @GetMapping({"/facturas/{id}", "/api/v1/facturas/{id}"})
    @PreAuthorize("hasAnyRole('JEFE', 'ADMINISTRATIVO', 'MINORISTA')")
    public ResponseEntity<FacturaResponse> buscarFactura(@PathVariable String id) {
        return ejecutarBuscar(id);
    }

    @PostMapping({"/facturas/{id}/anular", "/api/v1/facturas/{id}/anular"})
    @PreAuthorize("hasAnyRole('JEFE', 'ADMINISTRATIVO')")
    public ResponseEntity<FacturaResponse> anularFactura(@PathVariable String id) {
        return ResponseEntity.ok(service.anularFactura(id));
    }

    @GetMapping(
            value = {"/facturas/{id}/pdf", "/api/v1/facturas/{id}/pdf"},
            produces = MediaType.APPLICATION_PDF_VALUE
    )
    @PreAuthorize("hasAnyRole('JEFE', 'ADMINISTRATIVO', 'MINORISTA')")
    public ResponseEntity<byte[]> descargarFacturaPdf(@PathVariable String id) {
        byte[] pdf = service.generarFacturaPdf(id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"factura-" + id + ".pdf\"")
                .body(pdf);
    }

    @GetMapping(
            value = {"/facturas/{id}/qr-pago", "/api/v1/facturas/{id}/qr-pago"},
            produces = MediaType.IMAGE_PNG_VALUE
    )
    @PreAuthorize("hasAnyRole('JEFE', 'ADMINISTRATIVO', 'MINORISTA')")
    public ResponseEntity<byte[]> generarQrPago(@PathVariable String id) {
        byte[] png = service.generarQrPago(id);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(png);
    }

    @PostMapping({"/facturas/{id}/reenviar-correo", "/api/v1/facturas/{id}/reenviar-correo"})
    @PreAuthorize("hasAnyRole('JEFE', 'ADMINISTRATIVO')")
    public ResponseEntity<Void> reenviarFacturaPorCorreo(@PathVariable String id) {
        service.reenviarFacturaPorCorreo(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping({"/mi-cuenta/facturas", "/api/v1/mi-cuenta/facturas"})
    @PreAuthorize("hasRole('MINORISTA')")
    public ResponseEntity<FacturaMiCuentaPage> listarFacturaPorMinorista(
            @RequestParam(required = false) String estado,
            Pageable pageable) {
        return ResponseEntity.ok(service.listarMiCuenta(estado, pageable));
    }
}
