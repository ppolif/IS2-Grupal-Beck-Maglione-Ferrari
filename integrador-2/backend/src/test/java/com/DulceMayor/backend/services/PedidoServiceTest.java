package com.DulceMayor.backend.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.DulceMayor.backend.config.ConflictoException;
import com.DulceMayor.backend.config.ValidacionException;
import com.DulceMayor.backend.dtos.pedido.AnularPedidoRequest;
import com.DulceMayor.backend.dtos.pedido.DetallePedidoRequest;
import com.DulceMayor.backend.dtos.pedido.DetallePedidoResponse;
import com.DulceMayor.backend.dtos.pedido.PedidoEstadoCambiadoEvent;
import com.DulceMayor.backend.dtos.pedido.PedidoMapper;
import com.DulceMayor.backend.dtos.pedido.PedidoRequest;
import com.DulceMayor.backend.dtos.pedido.PedidoResponse;
import com.DulceMayor.backend.dtos.pedido.PedidoUpdateRequest;
import com.DulceMayor.backend.dtos.producto.VigenciaPrecioResponse;
import com.DulceMayor.backend.entities.pedido.DetallePedido;
import com.DulceMayor.backend.entities.pedido.EstadoPedido;
import com.DulceMayor.backend.entities.pedido.Pedido;
import com.DulceMayor.backend.entities.producto.Producto;
import com.DulceMayor.backend.entities.producto.Promocion;
import com.DulceMayor.backend.repositories.pedido.PedidoRepository;
import com.DulceMayor.backend.services.pedido.HistorialEstadoPedidoService;
import com.DulceMayor.backend.services.pedido.PedidoLecturaService;
import com.DulceMayor.backend.services.pedido.PedidoService;
import com.DulceMayor.backend.services.producto.ProductoService;
import com.DulceMayor.backend.services.producto.PromocionService;
import com.DulceMayor.backend.services.producto.VigenciaPrecioService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

/**
 * Tests unitarios de {@link PedidoService} con Mockito.
 */
@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private PedidoMapper pedidoMapper;

    @Mock
    private PedidoLecturaService pedidoLecturaService;

    @Mock
    private HistorialEstadoPedidoService historialEstadoPedidoService;

    @Mock
    private ProductoService productoService;

    @Mock
    private VigenciaPrecioService vigenciaPrecioService;

    @Mock
    private PromocionService promocionService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private PedidoService pedidoService;

    @BeforeEach
    void setUp() {
        pedidoService = new PedidoService(
                pedidoRepository,
                pedidoMapper,
                pedidoLecturaService,
                historialEstadoPedidoService,
                productoService,
                vigenciaPrecioService,
                promocionService,
                eventPublisher
        );
    }

    @Test
    @DisplayName("crear: carga pedido en CARGADO copiando VigenciaPrecio, aplicando Promocion y registrando historial")
    void crearPedidoConPromocionExitoso() {
        Producto prod = Producto.builder()
                .id("prod-1")
                .codigo("ALF-01")
                .nombre("Alfajor Triple")
                .presentacion("Caja x24")
                .eliminado(false)
                .build();

        Promocion promo = Promocion.builder()
                .id("promo-1")
                .codigo("DESC10")
                .porcentajeDescuento(10.0)
                .cantidadMinima(2)
                .producto(prod)
                .fechaDesde(LocalDate.now().minusDays(1))
                .fechaHasta(LocalDate.now().plusDays(5))
                .eliminado(false)
                .build();

        when(productoService.obtenerEntidad("prod-1")).thenReturn(prod);
        when(vigenciaPrecioService.buscarVigente("prod-1")).thenReturn(
                new VigenciaPrecioResponse("vp-1", "prod-1", LocalDate.now(), null, 1000.0)
        );
        when(promocionService.obtenerVigentePorCodigoParaPedido("DESC10")).thenReturn(promo);
        when(pedidoRepository.findFirstByOrderByNumeroPedidoDesc()).thenReturn(Optional.empty());
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> {
            Pedido p = inv.getArgument(0);
            p.setId("ped-1");
            return p;
        });
        when(pedidoLecturaService.enriquecerResponse(any(Pedido.class))).thenAnswer(inv -> {
            Pedido p = inv.getArgument(0);
            return new PedidoResponse(
                    p.getId(),
                    p.getNumeroPedido(),
                    p.getFechaHoraCarga(),
                    p.getEstado(),
                    p.getPuntoDeVentaId(),
                    p.getPreventorId(),
                    p.getDistribuidorId(),
                    p.getPromocion() != null ? p.getPromocion().getCodigo() : null,
                    p.getSubtotal(),
                    p.getDescuento(),
                    p.getTotal(),
                    List.of(new DetallePedidoResponse("dp-1", "prod-1", "Alfajor Triple", "Caja x24", 3, 1000.0, 3000.0)),
                    null
            );
        });

        PedidoRequest request = new PedidoRequest(
                "pdv-1",
                "prev-1",
                "DESC10",
                List.of(new DetallePedidoRequest("prod-1", 3))
        );

        PedidoResponse res = pedidoService.crear(request);

        assertNotNull(res);
        assertEquals(EstadoPedido.CARGADO, res.estado());
        assertEquals(3000.0, res.subtotal());
        assertEquals(300.0, res.descuento());
        assertEquals(2700.0, res.total());
        verify(historialEstadoPedidoService).registrarEstado(
                any(Pedido.class),
                eq(EstadoPedido.CARGADO),
                any(String.class),
                eq("Pedido cargado")
        );
    }

    @Test
    @DisplayName("crear: lanza ValidacionException cuando no se alcanza la cantidadMinima de la promoción")
    void crearPedidoSinCantidadMinimaDePromocionLanza400() {
        Producto prod = Producto.builder()
                .id("prod-1")
                .nombre("Caramelos")
                .eliminado(false)
                .build();

        Promocion promo = Promocion.builder()
                .id("promo-1")
                .codigo("MAYORISTA20")
                .porcentajeDescuento(20.0)
                .cantidadMinima(10)
                .fechaDesde(LocalDate.now().minusDays(1))
                .fechaHasta(LocalDate.now().plusDays(5))
                .eliminado(false)
                .build();

        when(productoService.obtenerEntidad("prod-1")).thenReturn(prod);
        when(vigenciaPrecioService.buscarVigente("prod-1")).thenReturn(
                new VigenciaPrecioResponse("vp-1", "prod-1", LocalDate.now(), null, 500.0)
        );
        when(promocionService.obtenerVigentePorCodigoParaPedido("MAYORISTA20")).thenReturn(promo);

        PedidoRequest request = new PedidoRequest(
                "pdv-1",
                "prev-1",
                "MAYORISTA20",
                List.of(new DetallePedidoRequest("prod-1", 2))
        );

        assertThrows(ValidacionException.class, () -> pedidoService.crear(request));
    }

    @Test
    @DisplayName("modificarPedido: rechaza con 409 ConflictoException si el pedido ya no está en CARGADO")
    void modificarPedidoFueraDeCargadoLanzaConflicto() {
        Pedido facturado = Pedido.builder()
                .id("ped-1")
                .numeroPedido(1L)
                .estado(EstadoPedido.FACTURADO)
                .puntoDeVentaId("pdv-1")
                .preventorId("prev-1")
                .detalles(new ArrayList<>())
                .eliminado(false)
                .build();

        when(pedidoRepository.findByIdAndEliminadoFalse("ped-1")).thenReturn(Optional.of(facturado));

        PedidoUpdateRequest update = new PedidoUpdateRequest(null, List.of(new DetallePedidoRequest("prod-1", 5)));
        assertThrows(ConflictoException.class, () -> pedidoService.modificarPedido("ped-1", update));
    }

    @Test
    @DisplayName("anularPedido: permite anular desde SIN_STOCK pero rechaza con 409 desde FACTURADO")
    void anularPedidoValidaTransiciones() {
        Pedido sinStock = Pedido.builder()
                .id("ped-1")
                .numeroPedido(5L)
                .fechaHoraCarga(LocalDateTime.now())
                .estado(EstadoPedido.SIN_STOCK)
                .puntoDeVentaId("pdv-1")
                .preventorId("prev-1")
                .detalles(List.of(DetallePedido.builder().cantidad(2).precioUnitario(500.0).subtotal(1000.0).build()))
                .eliminado(false)
                .build();

        when(pedidoRepository.findByIdAndEliminadoFalse("ped-1")).thenReturn(Optional.of(sinStock));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));
        when(pedidoLecturaService.enriquecerResponse(any(Pedido.class))).thenAnswer(inv -> {
            Pedido p = inv.getArgument(0);
            return new PedidoResponse(p.getId(), p.getNumeroPedido(), p.getFechaHoraCarga(), p.getEstado(),
                    p.getPuntoDeVentaId(), p.getPreventorId(), null, null, 1000.0, 0.0, 1000.0, List.of(), null);
        });

        PedidoResponse anulado = pedidoService.anularPedido("ped-1", new AnularPedidoRequest("Cancelado por cliente"));
        assertEquals(EstadoPedido.ANULADO, anulado.estado());
        verify(eventPublisher).publishEvent(any(PedidoEstadoCambiadoEvent.class));

        sinStock.setEstado(EstadoPedido.FACTURADO);
        assertThrows(ConflictoException.class, () -> pedidoService.anularPedido("ped-1", new AnularPedidoRequest("Intento inválido")));
    }
}
