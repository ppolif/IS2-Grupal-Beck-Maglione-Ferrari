package com.example.zero.services;

import com.example.zero.dto.reporte.FacturaItemReporteDTO;
import com.example.zero.entidades.compra.Detalle;
import com.example.zero.entidades.compra.Factura;
import com.example.zero.entidades.compraCliente.FacturaCliente;
import com.example.zero.entidades.compraProveedor.FacturaProveedor;
import com.example.zero.entidades.persona.Cliente;
import com.example.zero.entidades.zona.Direccion;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.*;

// servicio para la generación de facturas en PDF utilizando JasperReports.
@Service
public class FacturaPdfService {

    private static final Logger logger = LoggerFactory.getLogger(FacturaPdfService.class);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final VentaService ventaService;
    private final ContactoService contactoService;

    // cache del reporte compilado en memoria para optimizar el rendimiento
    private JasperReport jasperReportCache;

    @Autowired
    public FacturaPdfService(VentaService ventaService,
                             @Autowired(required = false) ContactoService contactoService) {
        this.ventaService = ventaService;
        this.contactoService = contactoService;
    }

    // obtiene la plantilla JasperReport desde el classpath.
    private synchronized JasperReport obtenerReporteCompilado() throws JRException {
        if (jasperReportCache == null) {
            ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
            try {
                // asegura el ClassLoader de la aplicacion para compilar expresiones JR
                Thread.currentThread().setContextClassLoader(FacturaPdfService.class.getClassLoader());
                ClassPathResource resource = new ClassPathResource("reports/factura_venta.jrxml");
                try (InputStream inputStream = resource.getInputStream()) {
                    jasperReportCache = JasperCompileManager.compileReport(inputStream);
                }
            } catch (Exception e) {
                throw new JRException("Error al cargar la plantilla de reporte de factura", e);
            } finally {
                Thread.currentThread().setContextClassLoader(originalClassLoader);
            }
        }
        return jasperReportCache;
    }

    // genera el archivo PDF de la factura dada en formato byte[] (como lo hace en el primer video de la guia)
    public byte[] generarFacturaPdf(Factura factura) {
        if (factura == null) {
            throw new IllegalArgumentException("La factura no puede ser nula para generar el PDF.");
        }

        // salvaguarda del ClassLoader de contexto para testing / servidores multihilo
        ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(FacturaPdfService.class.getClassLoader());

            JasperReport report = obtenerReporteCompilado();

            // mapeo de items de la factura
            List<FacturaItemReporteDTO> itemsReporte = new ArrayList<>();
            int itemNum = 1;

            if (factura.getDetalles() != null && !factura.getDetalles().isEmpty()) {
                for (Detalle d : factura.getDetalles()) {
                    if (d == null || d.isEliminado()) continue;

                    // obtenemos el precio de cada producto e inicializamos codigo descripción y categoria
                    String codigo = "-";
                    String descripcion = "Articulo Deportivo";
                    String categoria = "General";
                    int cantidad = d.getCantidad() > 0 ? d.getCantidad() : 1;
                    double subtotal = d.getSubtotal();
                    double precioUnitario = Math.round((subtotal / cantidad) * 100.0) / 100.0;

                    if (d.getProducto() != null) {
                        // si nos podemos traer el producto del detalle, obtenemos su codigo, descripcion y categoria
                        codigo = d.getProducto().getCodigo() != null ? d.getProducto().getCodigo() : "-";
                        descripcion = d.getProducto().getNombre() != null ? d.getProducto().getNombre() : "Producto";
                        if (d.getProducto().getSubCategoria() != null && d.getProducto().getSubCategoria().getCategoria() != null) {
                            categoria = d.getProducto().getSubCategoria().getCategoria().getNombre();
                        }
                    }

                    // armamos el DTO de la factura para el reporte
                    itemsReporte.add(FacturaItemReporteDTO.builder()
                            .numeroItem(itemNum++)
                            .codigo(codigo)
                            .descripcion(descripcion)
                            .categoria(categoria)
                            .cantidad(cantidad)
                            .precioUnitario(precioUnitario)
                            .subtotal(subtotal)
                            .build());
                }
            }

            // si por algun motivo la factura no tuviera detalles, colocamos un renglon descriptivo
            if (itemsReporte.isEmpty()) {
                double total = factura.getTotalPagado();
                itemsReporte.add(FacturaItemReporteDTO.builder()
                        .numeroItem(1)
                        .codigo("GEN-01")
                        .descripcion("Venta General / Comprobante de Compra")
                        .categoria("General")
                        .cantidad(1)
                        .precioUnitario(total)
                        .subtotal(total)
                        .build());
            }

            // calculo y formateo de datos fiscales y monetarios
            double totalFacturado = factura.getTotalPagado();
            // desglose fiscal estimado (IVA 21% para Factura B a Consumidor Final)
            double subtotalNeto = Math.round((totalFacturado / 1.21) * 100.0) / 100.0;
            double totalIva = Math.round((totalFacturado - subtotalNeto) * 100.0) / 100.0;

            // usamos el prefijo del proyecto para la orden: #ORD-XXXX
            String numeroFacturaStr;
            if (factura.getNumeroFactura() != null) {
                numeroFacturaStr = "#ORD-" + factura.getNumeroFactura();
            } else if (factura.getId() != null) {
                numeroFacturaStr = "#ORD-" + factura.getId();
            } else {
                numeroFacturaStr = "#ORD-0001";
            }

            String fechaEmisionStr = factura.getFechaFactura() != null
                    ? factura.getFechaFactura().format(DATE_TIME_FORMATTER)
                    : "";

            String formaPagoStr = "CONTADO";
            if (factura.getFormaDePago() != null && factura.getFormaDePago().getTipoPago() != null) {
                formaPagoStr = factura.getFormaDePago().getTipoPago().name().replace('_', ' ');
            }

            // obtenemos la información del comprador
            String clienteNombre = ventaService.obtenerNombreComprobante(factura);
            String clienteEmail = ventaService.obtenerEmailComprobante(factura);
            String clienteDoc = "N/A";
            String clienteDomicilio = "Retiro en Sucursal Central";
            String clienteLocalidad = "Mendoza";
            String clienteTelefono = "-";

            Cliente cliente = ventaService.obtenerClienteDeFactura(factura);
            if (cliente != null) {
                if (cliente.getNumeroDocumento() != null && !cliente.getNumeroDocumento().isBlank()) {
                    clienteDoc = cliente.getNumeroDocumento().trim();
                }
                if (contactoService != null) {
                    clienteTelefono = contactoService.obtenerTelefonoPrincipal(cliente).orElse("-");
                }
                if (cliente.getDireccion() != null && !cliente.getDireccion().isEmpty()) {
                    for (Direccion dir : cliente.getDireccion()) {
                        if (dir != null && !dir.isEliminado()) {
                            StringBuilder dirSb = new StringBuilder();
                            if (dir.getCalle() != null) dirSb.append(dir.getCalle());
                            if (dir.getNumeracion() != null) dirSb.append(" ").append(dir.getNumeracion());
                            if (dir.getBarrio() != null) dirSb.append(", B° ").append(dir.getBarrio());
                            clienteDomicilio = dirSb.toString().trim();

                            if (dir.getLocalidad() != null && dir.getLocalidad().getNombre() != null) {
                                clienteLocalidad = dir.getLocalidad().getNombre();
                            }
                            break;
                        }
                    }
                }
            } else if (factura instanceof FacturaProveedor fp && fp.getProveedor() != null) {
                clienteNombre = fp.getProveedor().getRazonSocial();
                clienteDoc = "CUIT: " + fp.getProveedor().getCuit();
                if (contactoService != null) {
                    clienteTelefono = contactoService.obtenerTelefonoProveedor(fp.getProveedor()).orElse("-");
                }
            }

            // CAE y vencimiento fiscal
            String caeNumero = "7429182390" + (factura.getNumeroFactura() != null ? factura.getNumeroFactura() : "1001");
            String caeVencimiento = factura.getFechaFactura() != null
                    ? factura.getFechaFactura().plusDays(10).format(DATE_FORMATTER)
                    : "31/12/2026";

            // carga del mapa de parametros para JasperReports
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("EMPRESA_NOMBRE", "ZERO SPORTS");
            parameters.put("EMPRESA_RAZON_SOCIAL", "ZERO Argentina S.A.");
            parameters.put("EMPRESA_CUIT", "30-71829384-9");
            parameters.put("EMPRESA_DIRECCION", "Av. Corrientes 1234 - CABA");
            parameters.put("EMPRESA_CONDICION_IVA", "IVA Responsable Inscripto");
            parameters.put("EMPRESA_IIBB", "30-71829384-9");
            parameters.put("EMPRESA_INICIO_ACT", "01/01/2026");

            parameters.put("FACTURA_NUMERO", numeroFacturaStr);
            parameters.put("FACTURA_FECHA", fechaEmisionStr);
            parameters.put("FACTURA_TIPO", "B");
            parameters.put("FACTURA_CODIGO_TIPO", "COD. 06");
            parameters.put("FORMA_PAGO", formaPagoStr);

            parameters.put("CLIENTE_NOMBRE", clienteNombre);
            parameters.put("CLIENTE_DOCUMENTO", clienteDoc);
            parameters.put("CLIENTE_DOMICILIO", clienteDomicilio);
            parameters.put("CLIENTE_LOCALIDAD", clienteLocalidad);
            parameters.put("CLIENTE_EMAIL", clienteEmail);
            parameters.put("CLIENTE_TELEFONO", clienteTelefono);

            parameters.put("TOTAL_NETO", subtotalNeto);
            parameters.put("TOTAL_IVA", totalIva);
            parameters.put("TOTAL_FACTURADO", totalFacturado);

            parameters.put("CAE_NUMERO", caeNumero);
            parameters.put("CAE_VENCIMIENTO", caeVencimiento);

            // creacion del DataSource JRBeanCollectionDataSource y generacion del PDF
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(itemsReporte);
            JasperPrint jasperPrint = JasperFillManager.fillReport(report, parameters, dataSource);

            return JasperExportManager.exportReportToPdf(jasperPrint);

        } catch (JRException e) {
            logger.error("Error al exportar factura a PDF mediante JasperReports: {}", e.getMessage(), e);
            throw new RuntimeException("Error al generar el archivo PDF de la factura: " + e.getMessage(), e);
        } finally {
            Thread.currentThread().setContextClassLoader(originalClassLoader);
        }
    }
}
