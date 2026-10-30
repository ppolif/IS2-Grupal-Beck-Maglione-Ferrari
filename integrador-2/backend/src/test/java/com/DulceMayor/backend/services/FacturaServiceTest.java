package com.DulceMayor.backend.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.DulceMayor.backend.config.ConflictoException;
import com.DulceMayor.backend.dtos.facturacion.DetalleFacturaMapper;
import com.DulceMayor.backend.dtos.facturacion.DetalleFacturaRequest;
import com.DulceMayor.backend.dtos.facturacion.DetalleFacturaResponse;
import com.DulceMayor.backend.dtos.facturacion.FacturaMapper;
import com.DulceMayor.backend.dtos.facturacion.FacturaRequest;
import com.DulceMayor.backend.dtos.facturacion.FacturaResponse;
import com.DulceMayor.backend.entities.facturacion.DetalleFactura;
import com.DulceMayor.backend.entities.facturacion.EstadoFactura;
import com.DulceMayor.backend.entities.facturacion.Factura;
import com.DulceMayor.backend.entities.pedido.DetallePedido;
import com.DulceMayor.backend.entities.pedido.EstadoPedido;
import com.DulceMayor.backend.entities.pedido.Pedido;
import com.DulceMayor.backend.entities.producto.Producto;
import com.DulceMayor.backend.repositories.facturacion.FacturaRepository;
import com.DulceMayor.backend.services.facturacion.FacturaLecturaService;
import com.DulceMayor.backend.services.facturacion.FacturaService;
import com.DulceMayor.backend.services.producto.ProductoService;
import com.DulceMayor.backend.services.producto.StockService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests unitarios de {@link FacturaService} con Mockito.
 */
@ExtendWith(MockitoExtension.class)
class FacturaServiceTest {

    @Mock
    private FacturaRepository facturaRepository;

    @Mock
    private FacturaMapper facturaMapper;

    @Mock
    private FacturaLecturaService facturaLecturaService;

    @Mock
    private ProductoService productoService;

    @Mock
    private StockService stockService;

    private FacturaService facturaService;

    @BeforeEach
    void setUp() {
        facturaService = new FacturaService(
                facturaRepository,
                facturaMapper,
                facturaLecturaService,
                productoService,
                stockService
        );
    }

    @Test
    @DisplayName("crear: genera factura en PENDIENTE_PAGO, asigna número correlativo y descuenta stock con DetalleFactura")
    void crearFacturaExitosoDescuentaStock() {
        Producto prod = Producto.builder()
                .id("prod-1")
                .codigo("ALF-01")
                .nombre("Alfajor Triple")
                .eliminado(false)
                .build();

        DetalleFacturaRequest itemReq = new DetalleFacturaRequest("prod-1", 3, 1000.0);
        FacturaRequest request = new FacturaRequest("ped-1", List.of(itemReq), 200.0);

        when(facturaRepository.findByPedidoIdAndEliminadoFalse("ped-1")).thenReturn(Optional.empty());
        when(productoService.obtenerEntidad("prod-1")).thenReturn(prod);
        when(facturaRepository.findFirstByOrderByNumeroFacturaDesc()).thenReturn(Optional.empty());
        when(facturaRepository.save(any(Factura.class))).thenAnswer(inv -> {
            Factura f = inv.getArgument(0);
            f.setId("fac-1");
            f.getDetalles().get(0).setId("det-1");
            return f;
        });

        FacturaResponse respuestaEsperada = new FacturaResponse(
                "fac-1",
                1L,
                LocalDateTime.now(),
                "ped-1",
                EstadoFactura.PENDIENTE_PAGO,
                2800.0,
                0.0,
                2800.0,
                List.of(new DetalleFacturaResponse("det-1", "prod-1", "Alfajor Triple", 3, 1000.0, 3000.0)),
                List.of()
        );
        when(facturaLecturaService.enriquecerResponse(any(Factura.class))).thenReturn(respuestaEsperada);

        FacturaResponse response = facturaService.crear(request);

        assertNotNull(response);
        assertEquals("fac-1", response.id());
        assertEquals(1L, response.numeroFactura());
        assertEquals(EstadoFactura.PENDIENTE_PAGO, response.estado());
        assertEquals(2800.0, response.total());
        verify(stockService).descontar(eq("prod-1"), eq(3), any(DetalleFactura.class));
    }

    @Test
    @DisplayName("anularFactura: lanza ConflictoException si la factura posee pagos activos")
    void anularFacturaConPagosLanzaConflicto() {
        Factura existente = Factura.builder()
                .id("fac-1")
                .numeroFactura(10L)
                .estado(EstadoFactura.PENDIENTE_PAGO)
                .total(5000.0)
                .eliminado(false)
                .build();

        when(facturaRepository.findByIdAndEliminadoFalse("fac-1")).thenReturn(Optional.of(existente));
        when(facturaLecturaService.tienePagosActivos("fac-1")).thenReturn(true);

        ConflictoException ex = assertThrows(ConflictoException.class, () -> facturaService.anularFactura("fac-1"));
        assertEquals("CONFLICTO", ex.getCode());
    }

    @Test
    @DisplayName("anularFactura: cambia estado a ANULADA cuando no tiene pagos")
    void anularFacturaSinPagosExitoso() {
        Factura existente = Factura.builder()
                .id("fac-1")
                .numeroFactura(10L)
                .estado(EstadoFactura.PENDIENTE_PAGO)
                .total(5000.0)
                .eliminado(false)
                .build();

        when(facturaRepository.findByIdAndEliminadoFalse("fac-1")).thenReturn(Optional.of(existente));
        when(facturaLecturaService.tienePagosActivos("fac-1")).thenReturn(false);
        when(facturaRepository.save(any(Factura.class))).thenAnswer(inv -> inv.getArgument(0));
        when(facturaLecturaService.enriquecerResponse(any(Factura.class))).thenAnswer(inv -> {
            Factura f = inv.getArgument(0);
            return new FacturaResponse(f.getId(), f.getNumeroFactura(), LocalDateTime.now(), null, f.getEstado(), f.getTotal(), 0.0, f.getTotal(), List.of(), List.of());
        });

        FacturaResponse res = facturaService.anularFactura("fac-1");
        assertEquals(EstadoFactura.ANULADA, res.estado());
    }

    @Test
    @DisplayName("generarQrPago: genera imagen PNG cuando está PENDIENTE_PAGO y lanza 409 si está PAGADA")
    void generarQrPagoValidaEstado() {
        Factura pendiente = Factura.builder()
                .id("fac-1")
                .numeroFactura(1L)
                .estado(EstadoFactura.PENDIENTE_PAGO)
                .total(1500.0)
                .eliminado(false)
                .build();

        when(facturaRepository.findByIdAndEliminadoFalse("fac-1")).thenReturn(Optional.of(pendiente));
        when(facturaLecturaService.obtenerTotalPagado("fac-1")).thenReturn(500.0);

        byte[] png = facturaService.generarQrPago("fac-1");
        assertNotNull(png);
        assertTrue(png.length > 50);

        pendiente.setEstado(EstadoFactura.PAGADA);
        assertThrows(ConflictoException.class, () -> facturaService.generarQrPago("fac-1"));
    }

    @Test
    @DisplayName("generarDesdePedido: clona DetallePedido, vincula Pedido con @ManyToOne y descuenta stock")
    void generarDesdePedidoExitoso() {
        Producto prod = Producto.builder()
                .id("prod-1")
                .nombre("Alfajor Simple")
                .eliminado(false)
                .build();

        Pedido pedido = Pedido.builder()
                .id("ped-10")
                .numeroPedido(10L)
                .estado(EstadoPedido.CARGADO)
                .subtotal(2000.0)
                .descuento(200.0)
                .total(1800.0)
                .detalles(List.of(DetallePedido.builder()
                        .id("dp-1")
                        .producto(prod)
                        .cantidad(2)
                        .precioUnitario(1000.0)
                        .subtotal(2000.0)
                        .build()))
                .eliminado(false)
                .build();

        when(facturaRepository.findByPedidoIdAndEliminadoFalse("ped-10")).thenReturn(Optional.empty());
        when(facturaRepository.findFirstByOrderByNumeroFacturaDesc()).thenReturn(Optional.empty());
        when(facturaRepository.save(any(Factura.class))).thenAnswer(inv -> {
            Factura f = inv.getArgument(0);
            f.setId("fac-10");
            f.getDetalles().get(0).setId("df-10");
            return f;
        });
        when(facturaLecturaService.enriquecerResponse(any(Factura.class))).thenAnswer(inv -> {
            Factura f = inv.getArgument(0);
            return new FacturaResponse(
                    f.getId(),
                    f.getNumeroFactura(),
                    f.getFechaFactura(),
                    f.getPedido() != null ? f.getPedido().getId() : null,
                    f.getEstado(),
                    f.getTotal(),
                    0.0,
                    f.getTotal(),
                    List.of(),
                    List.of()
            );
        });

        FacturaResponse res = facturaService.generarDesdePedido(pedido);

        assertEquals("fac-10", res.id());
        assertEquals("ped-10", res.pedidoId());
        assertEquals(1800.0, res.total());
        verify(stockService).descontar(eq("prod-1"), eq(2), any(DetalleFactura.class));
    }
}
