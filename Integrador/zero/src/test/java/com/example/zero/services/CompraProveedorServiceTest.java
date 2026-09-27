package com.example.zero.services;

import com.example.zero.entidades.compra.Detalle;
import com.example.zero.entidades.compra.FormaDePago;
import com.example.zero.entidades.compra.Stock;
import com.example.zero.entidades.compraProveedor.FacturaProveedor;
import com.example.zero.entidades.compraProveedor.Proveedor;
import com.example.zero.entidades.producto.Producto;
import com.example.zero.enums.EstadoFactura;
import com.example.zero.enums.TipoDePago;
import com.example.zero.repositories.*;
import com.example.zero.services.producto.ProductoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompraProveedorServiceTest {

    @Mock
    private FacturaProveedorRepository facturaProveedorRepository;

    @Mock
    private FacturaRepository facturaRepository;

    @Mock
    private DetalleRepository detalleRepository;

    @Mock
    private StockRepository stockRepository;

    @Mock
    private ProveedorRepository proveedorRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private FormaDePagoRepository formaDePagoRepository;

    @Mock
    private ProductoService productoService;

    @Mock
    private StockService stockService;

    @InjectMocks
    private CompraProveedorService compraProveedorService;

    @Test
    void registrarCompraProveedor_exitoso_incrementaStockYGuardaFactura() {
        // Arrange
        String provId = "prov-1";
        Proveedor prov = Proveedor.builder().id(provId).razonSocial("Textil S.A.").cuit("30-12345678-9").build();
        when(proveedorRepository.findActive(provId)).thenReturn(Optional.of(prov));

        Producto prod = Producto.builder().id("p1").nombre("Remera").stock(10).build();
        when(productoService.buscarPorId("p1")).thenReturn(prod);
        when(stockService.calcularStockActual("p1")).thenReturn(10);
        when(productoService.aumentarStock(10, 25)).thenReturn(35);

        FormaDePago fdp = FormaDePago.builder().id("fdp-1").tipoPago(TipoDePago.TRANSFERENCIA).build();
        when(formaDePagoRepository.findByTipoPagoAndEliminadoFalse(TipoDePago.TRANSFERENCIA)).thenReturn(Optional.of(fdp));

        when(facturaProveedorRepository.save(any(FacturaProveedor.class))).thenAnswer(inv -> {
            FacturaProveedor fp = inv.getArgument(0);
            fp.setId("fp-1");
            return fp;
        });

        when(detalleRepository.save(any(Detalle.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        FacturaProveedor resultado = compraProveedorService.registrarCompraProveedor(
                provId,
                5001L,
                LocalDateTime.now(),
                "TRANSFERENCIA",
                "ENTREGADA",
                List.of("p1"),
                List.of(25),
                List.of(100.0)
        );

        // Assert
        assertNotNull(resultado);
        assertEquals("fp-1", resultado.getId());
        assertEquals(5001L, resultado.getNumeroFactura());
        assertEquals(2500.0, resultado.getTotalPagado());
        assertEquals(35, prod.getStock()); // 10 iniciales + 25 pedidos

        verify(productoRepository).save(prod);
        verify(stockService).crearStock(any(Detalle.class), eq(35), anyString());
        verify(facturaProveedorRepository).save(any(FacturaProveedor.class));
    }

    @Test
    @DisplayName("registrarCompraProveedor con estado SIN_DEFINIR no incrementa stock ni crea Stock previo a entrega")
    void registrarCompraProveedor_conEstadoSinDefinir_noIncrementaStockHastaEntrega() {
        String provId = "prov-2";
        Proveedor prov = Proveedor.builder().id(provId).razonSocial("Calzados S.A.").cuit("30-98765432-1").build();
        when(proveedorRepository.findActive(provId)).thenReturn(Optional.of(prov));

        Producto prod = Producto.builder().id("p2").nombre("Zapatillas").stock(5).build();
        when(productoService.buscarPorId("p2")).thenReturn(prod);

        FormaDePago fdp = FormaDePago.builder().id("fdp-2").tipoPago(TipoDePago.TRANSFERENCIA).build();
        when(formaDePagoRepository.findByTipoPagoAndEliminadoFalse(TipoDePago.TRANSFERENCIA)).thenReturn(Optional.of(fdp));

        when(facturaProveedorRepository.save(any(FacturaProveedor.class))).thenAnswer(inv -> {
            FacturaProveedor fp = inv.getArgument(0);
            fp.setId("fp-2");
            return fp;
        });
        when(detalleRepository.save(any(Detalle.class))).thenAnswer(inv -> inv.getArgument(0));

        FacturaProveedor resultado = compraProveedorService.registrarCompraProveedor(
                provId,
                5002L,
                LocalDateTime.now(),
                "TRANSFERENCIA",
                "SIN_DEFINIR",
                List.of("p2"),
                List.of(15),
                List.of(200.0)
        );

        assertNotNull(resultado);
        assertEquals(EstadoFactura.SIN_DEFINIR, resultado.getEstado());
        assertEquals(5, prod.getStock()); // No se altera el stock
        verify(productoRepository, never()).save(any(Producto.class));
        verifyNoInteractions(stockService);
    }

    @Test
    @DisplayName("marcarComoEntregada aumenta existencias, audita movimiento y actualiza estado a ENTREGADA")
    void marcarComoEntregada_conFacturaValida_aumentaStockAuditaYActualizaEstado() {
        Producto producto = Producto.builder().id("p-10").nombre("Pelota Zero").stock(15).build();
        Detalle detalle = Detalle.builder()
                .id("det-1")
                .producto(producto)
                .cantidad(10)
                .eliminado(false)
                .build();

        FacturaProveedor factura = new FacturaProveedor();
        factura.setId("fp-100");
        factura.setNumeroFactura(5010L);
        factura.setEstado(EstadoFactura.SIN_DEFINIR);
        factura.setDetalles(new HashSet<>(Set.of(detalle)));

        when(facturaProveedorRepository.findActive("fp-100")).thenReturn(Optional.of(factura));
        when(stockService.calcularStockActual("p-10")).thenReturn(15);
        when(productoService.aumentarStock(15, 10)).thenReturn(25);
        when(facturaProveedorRepository.save(any(FacturaProveedor.class))).thenAnswer(inv -> inv.getArgument(0));

        FacturaProveedor resultado = compraProveedorService.marcarComoEntregada("fp-100");

        assertNotNull(resultado);
        assertEquals(EstadoFactura.ENTREGADA, resultado.getEstado());
        assertEquals(25, producto.getStock());
        verify(productoRepository).save(producto);
        verify(stockService).crearStock(eq(detalle), eq(25), contains("Factura N° 5010"));
        verify(facturaProveedorRepository).save(factura);
    }

    @Test
    @DisplayName("marcarComoEntregada con factura ya entregada lanza IllegalStateException y evita duplicar stock")
    void marcarComoEntregada_conFacturaYaEntregada_lanzaIllegalStateException() {
        FacturaProveedor factura = new FacturaProveedor();
        factura.setId("fp-200");
        factura.setEstado(EstadoFactura.ENTREGADA);

        when(facturaProveedorRepository.findActive("fp-200")).thenReturn(Optional.of(factura));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                compraProveedorService.marcarComoEntregada("fp-200"));

        assertEquals("La Factura ya fue entregada previamente", ex.getMessage());
        verifyNoInteractions(stockService);
        verify(productoRepository, never()).save(any());
    }

    @Test
    @DisplayName("marcarComoEntregada con ID inexistente lanza IllegalArgumentException")
    void marcarComoEntregada_facturaInexistente_lanzaIllegalArgumentException() {
        when(facturaProveedorRepository.findActive("no-existe")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                compraProveedorService.marcarComoEntregada("no-existe"));
    }

    @Test
    @DisplayName("marcarComoPagada delega en marcarComoEntregada")
    void marcarComoPagada_delegaEnMarcarComoEntregada() {
        FacturaProveedor factura = new FacturaProveedor();
        factura.setId("fp-300");
        factura.setEstado(EstadoFactura.SIN_DEFINIR);
        factura.setDetalles(new HashSet<>());

        when(facturaProveedorRepository.findActive("fp-300")).thenReturn(Optional.of(factura));
        when(facturaProveedorRepository.save(any(FacturaProveedor.class))).thenAnswer(inv -> inv.getArgument(0));

        FacturaProveedor resultado = compraProveedorService.marcarComoPagada("fp-300");

        assertNotNull(resultado);
        assertEquals(EstadoFactura.ENTREGADA, resultado.getEstado());
    }

    @Test
    void registrarCompraProveedor_proveedorInexistente_lanzaExcepcion() {
        when(proveedorRepository.findActive("no-existe")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                compraProveedorService.registrarCompraProveedor(
                        "no-existe", 5001L, LocalDateTime.now(), "TRANSFERENCIA", "PAGADA",
                        List.of("p1"), List.of(10), List.of(50.0)
                ));
    }

    @Test
    void validarCompra_parametrosInvalidos_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () ->
                compraProveedorService.validarCompra(null, List.of("p1"), List.of(10)));
        assertThrows(IllegalArgumentException.class, () ->
                compraProveedorService.validarCompra("prov-1", null, List.of(10)));
        assertThrows(IllegalArgumentException.class, () ->
                compraProveedorService.validarCompra("prov-1", List.of("p1"), List.of()));
        assertThrows(IllegalArgumentException.class, () ->
                compraProveedorService.validarCompra("prov-1", List.of("p1"), List.of(0)));
    }

    @Test
    void listarComprasProveedor_retornaLista() {
        FacturaProveedor fp = new FacturaProveedor();
        fp.setId("fp-1");
        when(facturaProveedorRepository.findByEliminadoFalseOrderByFechaFacturaDesc()).thenReturn(List.of(fp));

        List<FacturaProveedor> lista = compraProveedorService.listarComprasProveedor();

        assertEquals(1, lista.size());
        assertEquals("fp-1", lista.get(0).getId());
    }

    @Test
    void buscarPorId_existente_retornaFactura() {
        FacturaProveedor fp = new FacturaProveedor();
        fp.setId("fp-1");
        when(facturaProveedorRepository.findActive("fp-1")).thenReturn(Optional.of(fp));

        FacturaProveedor encontrada = compraProveedorService.buscarPorId("fp-1");

        assertNotNull(encontrada);
        assertEquals("fp-1", encontrada.getId());
    }

    @Test
    void eliminarCompraProveedor_marcaEliminada() {
        FacturaProveedor fp = new FacturaProveedor();
        fp.setId("fp-1");
        when(facturaProveedorRepository.findActive("fp-1")).thenReturn(Optional.of(fp));

        compraProveedorService.eliminarCompraProveedor("fp-1");

        assertTrue(fp.isEliminado());
        verify(facturaProveedorRepository).save(fp);
    }
}
