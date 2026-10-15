package com.example.zero.services;

import com.example.zero.entidades.compra.Detalle;
import com.example.zero.entidades.compra.Factura;
import com.example.zero.entidades.compra.FormaDePago;
import com.example.zero.entidades.compraCliente.FacturaCliente;
import com.example.zero.entidades.persona.Cliente;
import com.example.zero.entidades.producto.Categoria;
import com.example.zero.entidades.producto.Producto;
import com.example.zero.entidades.producto.SubCategoria;
import com.example.zero.enums.EstadoFactura;
import com.example.zero.enums.TipoDePago;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias para FacturaPdfService.
 * Valida la integracion de JasperReports, la compilacion del reporte y la exportacion a PDF.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests Unitarios - FacturaPdfService (Reporte PDF con JasperReports)")
class FacturaPdfServiceTest {

    @Mock
    private VentaService ventaService;

    @Mock
    private ContactoService contactoService;

    @InjectMocks
    private FacturaPdfService facturaPdfService;

    private Factura facturaTest;

    @BeforeEach
    void setUp() {
        Cliente cliente = Cliente.builder()
                .numeroDocumento("35123456")
                .nombre("Martin")
                .apellido("Ferrari")
                .eliminado(false)
                .build();

        Categoria categoria = Categoria.builder()
                .id("cat-1")
                .nombre("Calzado")
                .eliminado(false)
                .build();

        SubCategoria subCategoria = SubCategoria.builder()
                .id("subcat-1")
                .nombre("Zapatillas Running")
                .categoria(categoria)
                .eliminado(false)
                .build();

        Producto producto1 = Producto.builder()
                .id("prod-1")
                .codigo("ZAP-001")
                .nombre("Zapatilla Running Pro")
                .subCategoria(subCategoria)
                .eliminado(false)
                .build();

        Producto producto2 = Producto.builder()
                .id("prod-2")
                .codigo("MED-002")
                .nombre("Medias Deportivas")
                .subCategoria(subCategoria)
                .eliminado(false)
                .build();

        FormaDePago formaDePago = FormaDePago.builder()
                .tipoPago(TipoDePago.BILLETERA_VIRTUAL)
                .eliminado(false)
                .build();

        Set<Detalle> detalles = new HashSet<>();
        detalles.add(Detalle.builder()
                .id("det-1")
                .producto(producto1)
                .cantidad(1)
                .subtotal(45000.0)
                .eliminado(false)
                .build());

        detalles.add(Detalle.builder()
                .id("det-2")
                .producto(producto2)
                .cantidad(2)
                .subtotal(5000.0)
                .eliminado(false)
                .build());

        facturaTest = FacturaCliente.builder()
                .id("fac-uuid-1234")
                .numeroFactura(1001L)
                .fechaFactura(LocalDateTime.now())
                .estado(EstadoFactura.PAGADA)
                .formaDePago(formaDePago)
                .totalPagado(50000.0)
                .detalles(detalles)
                .eliminado(false)
                .build();
    }

    @Test
    @DisplayName("Debe generar exitosamente el arreglo de bytes del PDF con encabezado %PDF")
    void testGenerarFacturaPdf_Exitoso() {
        when(ventaService.obtenerNombreComprobante(any(Factura.class))).thenReturn("Martin Ferrari");
        when(ventaService.obtenerEmailComprobante(any(Factura.class))).thenReturn("martin@zero.com");

        byte[] pdfBytes = facturaPdfService.generarFacturaPdf(facturaTest);

        assertNotNull(pdfBytes, "El arreglo de bytes del PDF no debe ser nulo");
        assertTrue(pdfBytes.length > 1000, "El tamaño del PDF generado debe ser mayor a 1 KB");

        // Comprobacion de firma magica de archivo PDF (%PDF-...)
        String encabezado = new String(pdfBytes, 0, 4);
        assertEquals("%PDF", encabezado, "El archivo generado debe comenzar con la cabecera estándar %PDF");
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException si la factura enviada es nula")
    void testGenerarFacturaPdf_FacturaNula_LanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> facturaPdfService.generarFacturaPdf(null));
    }
}
