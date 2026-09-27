package com.example.zero.services;

import com.example.zero.entidades.compra.Detalle;
import com.example.zero.entidades.compra.Factura;
import com.example.zero.entidades.compra.FormaDePago;
import com.example.zero.entidades.compra.Stock;
import com.example.zero.entidades.persona.Cliente;
import com.example.zero.entidades.persona.Nacionalidad;
import com.example.zero.entidades.producto.Producto;
import com.example.zero.enums.EstadoFactura;
import com.example.zero.enums.TipoDePago;
import com.example.zero.enums.TipoDocumento;
import com.example.zero.repositories.*;
import com.example.zero.services.persona.ClienteService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VentaServiceTest {

    @Mock
    private FacturaRepository facturaRepository;

    @Mock
    private DetalleRepository detalleRepository;

    @Mock
    private FormaDePagoRepository formaDePagoRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ClienteService clienteService;

    @Mock
    private NacionalidadRepository nacionalidadRepository;

    @Mock
    private ProductoService productoService;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private StockService stockService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private VentaService ventaService;

    @Test
    @DisplayName("validarVenta no lanza excepción con datos y stock suficientes")
    void validarVenta_conDatosValidos_noLanzaExcepcion() {
        Producto prod = Producto.builder().id("prod-1").nombre("Zapatillas Running").stock(10).build();
        when(productoService.buscarPorId("prod-1")).thenReturn(prod);
        when(stockService.calcularStockActual("prod-1")).thenReturn(10);

        assertDoesNotThrow(() -> ventaService.validarVenta(
                "12345678", "Juan", "Perez",
                List.of("prod-1"), List.of(2)
        ));
    }

    @Test
    @DisplayName("validarVenta lanza IllegalArgumentException con mensaje exacto del diagrama ante stock insuficiente")
    void validarVenta_conStockInsuficiente_lanzaIllegalArgumentExceptionConMensajeExacto() {
        Producto prod = Producto.builder().id("prod-1").nombre("Remera Zero Fit").stock(2).build();
        when(productoService.buscarPorId("prod-1")).thenReturn(prod);
        when(stockService.calcularStockActual("prod-1")).thenReturn(2);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                ventaService.validarVenta("12345678", "Juan", "Perez", List.of("prod-1"), List.of(5))
        );

        assertEquals("Stock insuficiente para: Remera Zero Fit", ex.getMessage());
    }

    @Test
    void validarVenta_conDniVacio_lanzaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> ventaService.validarVenta(
                "", "Juan", "Perez",
                List.of("prod-1"), List.of(1)
        ));
        assertThrows(IllegalArgumentException.class, () -> ventaService.validarVenta(
                null, "Juan", "Perez",
                List.of("prod-1"), List.of(1)
        ));
    }

    @Test
    void validarVenta_conNombreOApellidoVacio_lanzaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> ventaService.validarVenta(
                "12345678", "  ", "Perez",
                List.of("prod-1"), List.of(1)
        ));
        assertThrows(IllegalArgumentException.class, () -> ventaService.validarVenta(
                "12345678", "Juan", "",
                List.of("prod-1"), List.of(1)
        ));
    }

    @Test
    void validarVenta_conProductosVacios_lanzaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> ventaService.validarVenta(
                "12345678", "Juan", "Perez",
                Collections.emptyList(), Collections.emptyList()
        ));
        assertThrows(IllegalArgumentException.class, () -> ventaService.validarVenta(
                "12345678", "Juan", "Perez",
                null, List.of(1)
        ));
    }

    @Test
    void validarVenta_conCantidadesDesiguales_lanzaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> ventaService.validarVenta(
                "12345678", "Juan", "Perez",
                List.of("prod-1"), List.of(1, 2)
        ));
    }

    @Test
    void validarVenta_conCantidadInvalida_lanzaIllegalArgumentException() {
        List<String> prods = List.of("prod-1");
        List<Integer> cantCero = List.of(0);
        assertThrows(IllegalArgumentException.class, () -> ventaService.validarVenta(
                "12345678", "Juan", "Perez", prods, cantCero
        ));
    }

    @Test
    @DisplayName("registrarVenta con stock insuficiente interrumpe flujo y no persiste factura (rollback)")
    void registrarVenta_conStockInsuficiente_lanzaExcepcionYNoPersisteFactura() {
        Producto prod = Producto.builder().id("prod-1").nombre("Campera Pro").build();
        when(productoService.buscarPorId("prod-1")).thenReturn(prod);
        when(stockService.calcularStockActual("prod-1")).thenReturn(1);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                ventaService.registrarVenta("12345678", "Carlos", "Tevez", "carlos@test.com", "EFECTIVO",
                        List.of("prod-1"), List.of(3))
        );

        assertEquals("Stock insuficiente para: Campera Pro", ex.getMessage());
        verifyNoInteractions(facturaRepository);
        verify(stockService, never()).crearStock(any(), anyInt(), anyString());
    }

    @Test
    @DisplayName("registrarVenta con cliente existente descuenta stock, audita Stock y actualiza Producto")
    void registrarVenta_conClienteExistente_reutilizaClienteYPersisteFactura() {
        Cliente clienteExistente = Cliente.builder()
                .numeroDocumento("12345678")
                .nombre("Juan")
                .apellido("Perez")
                .eliminado(false)
                .build();
        when(clienteRepository.findByNumeroDocumentoAndEliminadoFalse("12345678"))
                .thenReturn(Optional.of(clienteExistente));
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(i -> i.getArgument(0));

        FormaDePago formaExistente = FormaDePago.builder()
                .id("fdp-1")
                .tipoPago(TipoDePago.EFECTIVO)
                .eliminado(false)
                .build();
        when(formaDePagoRepository.findByTipoPagoAndEliminadoFalse(TipoDePago.EFECTIVO))
                .thenReturn(Optional.of(formaExistente));

        Factura ultimaFactura = Factura.builder().numeroFactura(1050L).build();
        when(facturaRepository.findTopByOrderByNumeroFacturaDesc()).thenReturn(Optional.of(ultimaFactura));

        Producto prod = Producto.builder().id("p1").nombre("Zapatillas").stock(10).build();
        when(productoService.buscarPorId("p1")).thenReturn(prod);
        when(productoService.obtenerPrecioActual("p1")).thenReturn(2500.0);
        when(stockService.calcularStockActual("p1")).thenReturn(10);
        when(productoService.disminuirStock(10, 2)).thenReturn(8);

        when(facturaRepository.save(any(Factura.class))).thenAnswer(i -> i.getArgument(0));

        Factura resultado = ventaService.registrarVenta(
                "12345678", "Juan", "Perez", "juan@test.com", "EFECTIVO",
                List.of("p1"), List.of(2)
        );

        assertNotNull(resultado);
        assertEquals(1051L, resultado.getNumeroFactura());
        assertEquals(5000.0, resultado.getTotalPagado());
        assertEquals(EstadoFactura.PAGADA, resultado.getEstado());
        assertEquals(clienteExistente, resultado.getCliente());
        assertEquals(formaExistente, resultado.getFormaDePago());
        assertEquals(1, resultado.getDetalles().size());

        // Verificación de descuento y auditoría de Stock
        verify(stockService).crearStock(any(Detalle.class), eq(8), contains("Factura N° 1051"));
        assertEquals(8, prod.getStock());
        verify(productoRepository).save(prod);
        verify(clienteService, never()).crearCliente(any(), any(), any(), any(), any(), any());
        verify(facturaRepository).save(any(Factura.class));
    }

    @Test
    @DisplayName("registrarVenta con cliente nuevo crea cliente, persiste factura y descuenta stock")
    void registrarVenta_conClienteNuevo_creaClienteYPersisteFactura() {
        when(clienteRepository.findByNumeroDocumentoAndEliminadoFalse("87654321"))
                .thenReturn(Optional.empty());

        Nacionalidad nac = Nacionalidad.builder().id("nac-1").nombre("Argentina").build();
        when(nacionalidadRepository.findByNombreAndEliminadoFalse("Argentina")).thenReturn(Optional.of(nac));

        Cliente clienteCreado = Cliente.builder()
                .numeroDocumento("87654321")
                .nombre("Maria")
                .apellido("Gomez")
                .nacionalidad(nac)
                .eliminado(false)
                .build();
        when(clienteService.crearCliente(eq("87654321"), eq("Maria"), eq("Gomez"), any(), eq(TipoDocumento.DNI), eq(nac)))
                .thenReturn(clienteCreado);
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(i -> i.getArgument(0));

        when(formaDePagoRepository.findByTipoPagoAndEliminadoFalse(TipoDePago.TARJETA_DEBITO))
                .thenReturn(Optional.empty());
        when(formaDePagoRepository.save(any(FormaDePago.class))).thenAnswer(i -> {
            FormaDePago fdp = i.getArgument(0);
            fdp.setId("fdp-new");
            return fdp;
        });

        when(facturaRepository.findTopByOrderByNumeroFacturaDesc()).thenReturn(Optional.empty());

        Producto prod = Producto.builder().id("p2").nombre("Remera").stock(20).build();
        when(productoService.buscarPorId("p2")).thenReturn(prod);
        when(productoService.obtenerPrecioActual("p2")).thenReturn(1500.0);
        when(stockService.calcularStockActual("p2")).thenReturn(20);
        when(productoService.disminuirStock(20, 3)).thenReturn(17);

        when(facturaRepository.save(any(Factura.class))).thenAnswer(i -> i.getArgument(0));

        Factura resultado = ventaService.registrarVenta(
                "87654321", "Maria", "Gomez", "maria@test.com", "TARJETA_DEBITO",
                List.of("p2"), List.of(3)
        );

        assertNotNull(resultado);
        assertEquals(1001L, resultado.getNumeroFactura());
        assertEquals(4500.0, resultado.getTotalPagado());
        verify(clienteService, times(1)).crearCliente(any(), any(), any(), any(), any(), any());
        verify(stockService).crearStock(any(Detalle.class), eq(17), contains("Factura N° 1001"));
        assertEquals(17, prod.getStock());
        verify(productoRepository).save(prod);
    }

    @Test
    @DisplayName("registrarVenta con múltiples productos descuenta el stock de cada uno y genera auditoría")
    void registrarVenta_conMultiplesProductos_descuentaStockIndividualYAudita() {
        Cliente cliente = Cliente.builder().numeroDocumento("11112222").nombre("Ana").apellido("Lopez").build();
        when(clienteRepository.findByNumeroDocumentoAndEliminadoFalse("11112222")).thenReturn(Optional.of(cliente));
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(i -> i.getArgument(0));

        FormaDePago fdp = FormaDePago.builder().tipoPago(TipoDePago.EFECTIVO).build();
        when(formaDePagoRepository.findByTipoPagoAndEliminadoFalse(TipoDePago.EFECTIVO)).thenReturn(Optional.of(fdp));

        when(facturaRepository.findTopByOrderByNumeroFacturaDesc()).thenReturn(Optional.empty());

        Producto p1 = Producto.builder().id("prod-a").nombre("Medias Zero").stock(15).build();
        Producto p2 = Producto.builder().id("prod-b").nombre("Gorra Zero").stock(8).build();

        when(productoService.buscarPorId("prod-a")).thenReturn(p1);
        when(productoService.buscarPorId("prod-b")).thenReturn(p2);
        when(stockService.calcularStockActual("prod-a")).thenReturn(15);
        when(stockService.calcularStockActual("prod-b")).thenReturn(8);

        when(productoService.obtenerPrecioActual("prod-a")).thenReturn(500.0);
        when(productoService.obtenerPrecioActual("prod-b")).thenReturn(1200.0);

        when(productoService.disminuirStock(15, 2)).thenReturn(13);
        when(productoService.disminuirStock(8, 1)).thenReturn(7);

        when(facturaRepository.save(any(Factura.class))).thenAnswer(i -> i.getArgument(0));

        Factura factura = ventaService.registrarVenta(
                "11112222", "Ana", "Lopez", "ana@test.com", "EFECTIVO",
                List.of("prod-a", "prod-b"), List.of(2, 1)
        );

        assertNotNull(factura);
        assertEquals(2200.0, factura.getTotalPagado());
        assertEquals(13, p1.getStock());
        assertEquals(7, p2.getStock());
        verify(productoRepository).save(p1);
        verify(productoRepository).save(p2);
        verify(stockService, times(2)).crearStock(any(Detalle.class), anyInt(), anyString());
    }

    @Test
    void listarVentas_retornaListaFacturasOrdenadas() {
        Factura f1 = Factura.builder().id("fac-1").numeroFactura(1001L).build();
        Factura f2 = Factura.builder().id("fac-2").numeroFactura(1002L).build();
        when(facturaRepository.findByEliminadoFalseOrderByFechaFacturaDesc()).thenReturn(List.of(f2, f1));

        List<Factura> resultado = ventaService.listarVentas();

        assertEquals(2, resultado.size());
        assertEquals("fac-2", resultado.get(0).getId());
        verify(facturaRepository).findByEliminadoFalseOrderByFechaFacturaDesc();
    }

    @Test
    void buscarPorId_existente_retornaFactura() {
        Factura f = Factura.builder().id("fac-1").numeroFactura(1001L).build();
        when(facturaRepository.findActive("fac-1")).thenReturn(Optional.of(f));

        Factura res = ventaService.buscarPorId("fac-1");
        assertNotNull(res);
        assertEquals(1001L, res.getNumeroFactura());
    }

    @Test
    void buscarPorId_inexistente_lanzaExcepcion() {
        when(facturaRepository.findActive("fac-99")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> ventaService.buscarPorId("fac-99"));
    }

    @Test
    void buscarPorId_nuloOVacio_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> ventaService.buscarPorId(null));
        assertThrows(IllegalArgumentException.class, () -> ventaService.buscarPorId("  "));
    }

    @Test
    void buscarPorNumeroFactura_existente_retornaFactura() {
        Factura f = Factura.builder().id("fac-1").numeroFactura(1005L).build();
        when(facturaRepository.findByNumeroFacturaAndEliminadoFalse(1005L)).thenReturn(Optional.of(f));

        Factura res = ventaService.buscarPorNumeroFactura(1005L);
        assertNotNull(res);
        assertEquals("fac-1", res.getId());
    }

    @Test
    void buscarPorNumeroFactura_inexistente_lanzaExcepcion() {
        when(facturaRepository.findByNumeroFacturaAndEliminadoFalse(9999L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> ventaService.buscarPorNumeroFactura(9999L));
    }

    @Test
    void buscarPorNumeroFactura_nulo_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> ventaService.buscarPorNumeroFactura(null));
    }

    @Test
    void eliminarVenta_marcaEliminadoFacturaYDetalles() {
        Detalle d1 = Detalle.builder().id("d1").eliminado(false).build();
        Detalle d2 = Detalle.builder().id("d2").eliminado(false).build();
        Set<Detalle> detalles = new HashSet<>(List.of(d1, d2));

        Factura f = Factura.builder()
                .id("fac-1")
                .numeroFactura(1001L)
                .eliminado(false)
                .detalles(detalles)
                .build();

        when(facturaRepository.findActive("fac-1")).thenReturn(Optional.of(f));
        when(facturaRepository.save(any(Factura.class))).thenAnswer(i -> i.getArgument(0));

        ventaService.eliminarVenta("fac-1");

        assertTrue(f.isEliminado());
        assertTrue(d1.isEliminado());
        assertTrue(d2.isEliminado());
        verify(facturaRepository).save(f);
    }

    @Test
    void buscarFacturaPorIdentificador_conNumeroFacturaConPrefijo_retornaFactura() {
        Factura f = Factura.builder()
                .id("fac-1")
                .numeroFactura(1001L)
                .fechaFactura(LocalDateTime.now())
                .totalPagado(1500.0)
                .eliminado(false)
                .detalles(new HashSet<>())
                .build();

        when(facturaRepository.findByNumeroFacturaAndEliminadoFalse(1001L)).thenReturn(Optional.of(f));

        Factura resultado = ventaService.buscarFacturaPorIdentificador("#ORD-1001");

        assertNotNull(resultado);
        assertEquals("#ORD-1001", resultado.getOrderNumber());
        assertEquals(1500.0, resultado.getTotalAmount());
    }

    @Test
    void buscarFacturaPorIdentificador_noExistente_retornaNull() {
        when(facturaRepository.findByNumeroFacturaAndEliminadoFalse(9999L)).thenReturn(Optional.empty());
        when(facturaRepository.findActive(anyString())).thenReturn(Optional.empty());

        Factura resultado = ventaService.buscarFacturaPorIdentificador("#ORD-9999");

        assertNull(resultado);
    }

    @Test
    void buscarFacturaPorIdentificador_nuloOVacio_retornaNull() {
        assertNull(ventaService.buscarFacturaPorIdentificador(null));
        assertNull(ventaService.buscarFacturaPorIdentificador("   "));
    }
}
