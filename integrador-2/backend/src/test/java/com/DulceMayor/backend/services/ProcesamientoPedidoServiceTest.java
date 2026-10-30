package com.DulceMayor.backend.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.DulceMayor.backend.config.ConflictoException;
import com.DulceMayor.backend.dtos.facturacion.FacturaResponse;
import com.DulceMayor.backend.dtos.pedido.PedidoResponse;
import com.DulceMayor.backend.dtos.pedido.ProcesamientoPedidoResponse;
import com.DulceMayor.backend.dtos.producto.StockResponse;
import com.DulceMayor.backend.entities.facturacion.EstadoFactura;
import com.DulceMayor.backend.entities.pedido.DetallePedido;
import com.DulceMayor.backend.entities.pedido.EstadoPedido;
import com.DulceMayor.backend.entities.pedido.Pedido;
import com.DulceMayor.backend.entities.producto.Producto;
import com.DulceMayor.backend.services.facturacion.FacturaService;
import com.DulceMayor.backend.services.pedido.PedidoService;
import com.DulceMayor.backend.services.pedido.ProcesamientoPedidoService;
import com.DulceMayor.backend.services.producto.StockService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests unitarios del servicio orquestador {@link ProcesamientoPedidoService}.
 */
@ExtendWith(MockitoExtension.class)
class ProcesamientoPedidoServiceTest {

    @Mock
    private PedidoService pedidoService;

    @Mock
    private StockService stockService;

    @Mock
    private FacturaService facturaService;

    private ProcesamientoPedidoService procesamientoPedidoService;

    @BeforeEach
    void setUp() {
        procesamientoPedidoService = new ProcesamientoPedidoService(
                pedidoService,
                stockService,
                facturaService
        );
    }

    @Test
    @DisplayName("procesarPedido: si falta stock pasa a SIN_STOCK, no genera factura y devuelve itemsSinStock")
    void procesarPedidoSinStockRetieneYNoFactura() {
        Producto prod = Producto.builder()
                .id("prod-1")
                .nombre("Chupetín Frutal")
                .eliminado(false)
                .build();

        Pedido pedido = Pedido.builder()
                .id("ped-1")
                .numeroPedido(1L)
                .estado(EstadoPedido.CARGADO)
                .total(5000.0)
                .detalles(List.of(DetallePedido.builder()
                        .id("dp-1")
                        .producto(prod)
                        .cantidad(10)
                        .precioUnitario(500.0)
                        .subtotal(5000.0)
                        .build()))
                .eliminado(false)
                .build();

        when(pedidoService.obtenerEntidad("ped-1")).thenReturn(pedido);
        when(stockService.buscarPorProducto("prod-1")).thenReturn(
                new StockResponse("st-1", "prod-1", "Chupetín Frutal", 4, "Stock actual", LocalDateTime.now())
        );
        when(pedidoService.marcarSinStock(eq(pedido), any(String.class))).thenReturn(
                new PedidoResponse("ped-1", 1L, LocalDateTime.now(), EstadoPedido.SIN_STOCK,
                        "pdv-1", "prev-1", null, null, 5000.0, 0.0, 5000.0, List.of(), null)
        );

        ProcesamientoPedidoResponse res = procesamientoPedidoService.procesarPedido("ped-1");

        assertEquals(EstadoPedido.SIN_STOCK, res.pedido().estado());
        assertNull(res.factura());
        assertEquals(1, res.itemsSinStock().size());
        assertEquals(10, res.itemsSinStock().get(0).solicitado());
        assertEquals(4, res.itemsSinStock().get(0).disponible());
        verify(facturaService, never()).generarDesdePedido(any());
    }

    @Test
    @DisplayName("procesarPedido: con stock suficiente genera Factura PENDIENTE_PAGO y pasa el pedido a FACTURADO")
    void procesarPedidoConStockGeneraFacturaYPasaAFacturado() {
        Producto prod = Producto.builder()
                .id("prod-1")
                .nombre("Gomitas Ácidas")
                .eliminado(false)
                .build();

        Pedido pedido = Pedido.builder()
                .id("ped-1")
                .numeroPedido(2L)
                .estado(EstadoPedido.SIN_STOCK)
                .total(2000.0)
                .detalles(List.of(DetallePedido.builder()
                        .id("dp-1")
                        .producto(prod)
                        .cantidad(2)
                        .precioUnitario(1000.0)
                        .subtotal(2000.0)
                        .build()))
                .eliminado(false)
                .build();

        FacturaResponse facturaGenerada = new FacturaResponse(
                "fac-1", 10L, LocalDateTime.now(), "ped-1", EstadoFactura.PENDIENTE_PAGO,
                2000.0, 0.0, 2000.0, List.of(), List.of()
        );

        when(pedidoService.obtenerEntidad("ped-1")).thenReturn(pedido);
        when(stockService.buscarPorProducto("prod-1")).thenReturn(
                new StockResponse("st-1", "prod-1", "Gomitas Ácidas", 15, "Reposición", LocalDateTime.now())
        );
        when(facturaService.generarDesdePedido(pedido)).thenReturn(facturaGenerada);
        when(pedidoService.marcarFacturado(eq(pedido), any(String.class))).thenReturn(
                new PedidoResponse("ped-1", 2L, LocalDateTime.now(), EstadoPedido.FACTURADO,
                        "pdv-1", "prev-1", null, null, 2000.0, 0.0, 2000.0, List.of(), "fac-1")
        );

        ProcesamientoPedidoResponse res = procesamientoPedidoService.procesarPedido("ped-1");

        assertEquals(EstadoPedido.FACTURADO, res.pedido().estado());
        assertNotNull(res.factura());
        assertEquals("fac-1", res.factura().id());
        assertTrue(res.itemsSinStock().isEmpty());
    }

    @Test
    @DisplayName("procesarPedido: lanza 409 ConflictoException si el pedido ya está FACTURADO o ANULADO")
    void procesarPedidoEstadoInvalidoLanza409() {
        Pedido yaFacturado = Pedido.builder()
                .id("ped-1")
                .estado(EstadoPedido.FACTURADO)
                .detalles(List.of())
                .eliminado(false)
                .build();

        when(pedidoService.obtenerEntidad("ped-1")).thenReturn(yaFacturado);

        assertThrows(ConflictoException.class, () -> procesamientoPedidoService.procesarPedido("ped-1"));
    }
}

