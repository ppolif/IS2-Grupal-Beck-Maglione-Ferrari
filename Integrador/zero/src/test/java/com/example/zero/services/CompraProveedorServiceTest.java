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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
                "PAGADA",
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
        verify(stockRepository).save(any(Stock.class));
        verify(facturaProveedorRepository).save(any(FacturaProveedor.class));
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
