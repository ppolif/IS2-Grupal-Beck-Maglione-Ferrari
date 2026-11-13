package com.DulceMayor.backend.services.ruta;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import com.DulceMayor.backend.dtos.direccion.DireccionMapper;
import com.DulceMayor.backend.dtos.persona.EmpleadoMapper;
import com.DulceMayor.backend.dtos.persona.PuntoDeVentaMapper;
import com.DulceMayor.backend.dtos.ruta.RutaResponse;
import com.DulceMayor.backend.entities.facturacion.FormaDePago;
import com.DulceMayor.backend.entities.facturacion.TipoPago;
import com.DulceMayor.backend.entities.pedido.EstadoPedido;
import com.DulceMayor.backend.entities.pedido.Pedido;
import com.DulceMayor.backend.entities.persona.Distribuidor;
import com.DulceMayor.backend.entities.persona.PuntoDeVenta;
import com.DulceMayor.backend.entities.ruta.RutaDistribuidor;
import com.DulceMayor.backend.entities.ruta.RutaEstado;
import com.DulceMayor.backend.services.mayorista.MayoristaService;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests unitarios de {@link RutaMapperHelper} verificando el cálculo de dinero recaudado
 * y pendiente de cobro en efectivo para las entregas del distribuidor.
 */
class RutaMapperHelperTest {

    @Test
    @DisplayName("aResponse calcula dineroRecaudadoEfectivo y dineroPendienteEfectivo en RutaDistribuidor")
    void calculaRecaudacionEfectivoEnRutaDistribuidor() {
        EmpleadoMapper empleadoMapper = mock(EmpleadoMapper.class);
        PuntoDeVentaMapper puntoDeVentaMapper = mock(PuntoDeVentaMapper.class);
        DireccionMapper direccionMapper = mock(DireccionMapper.class);
        MayoristaService mayoristaService = mock(MayoristaService.class);

        RutaMapperHelper helper = new RutaMapperHelper(
                empleadoMapper,
                puntoDeVentaMapper,
                direccionMapper,
                mayoristaService
        );

        FormaDePago efectivo = FormaDePago.builder().id("fp-efe").tipoPago(TipoPago.EFECTIVO).build();
        FormaDePago transferencia = FormaDePago.builder().id("fp-trf").tipoPago(TipoPago.TRANSFERENCIA).build();

        Pedido pedEfectivoEntregado = Pedido.builder()
                .id("ped-1")
                .numeroPedido(101L)
                .total(25000.0)
                .estado(EstadoPedido.ENTREGADO)
                .formaDePago(efectivo)
                .puntoDeVenta(PuntoDeVenta.builder().id("pdv-1").nombreComercio("Kiosco Sol").build())
                .build();

        Pedido pedEfectivoPendiente = Pedido.builder()
                .id("ped-2")
                .numeroPedido(102L)
                .total(18500.0)
                .estado(EstadoPedido.EN_REPARTO)
                .formaDePago(efectivo)
                .puntoDeVenta(PuntoDeVenta.builder().id("pdv-2").nombreComercio("Almacén Luna").build())
                .build();

        Pedido pedTransferenciaEntregado = Pedido.builder()
                .id("ped-3")
                .numeroPedido(103L)
                .total(40000.0)
                .estado(EstadoPedido.ENTREGADO)
                .formaDePago(transferencia)
                .puntoDeVenta(PuntoDeVenta.builder().id("pdv-3").nombreComercio("Drugstore Mitre").build())
                .build();

        RutaDistribuidor ruta = RutaDistribuidor.builder()
                .id("ruta-1")
                .fecha(LocalDate.now())
                .estado(RutaEstado.PENDIENTE)
                .responsable(Distribuidor.builder().id("dist-1").nombre("Carlos").apellido("Gómez").build())
                .pedidos(List.of(pedEfectivoEntregado, pedEfectivoPendiente, pedTransferenciaEntregado))
                .build();

        RutaResponse res = helper.aResponse(ruta);

        assertNotNull(res);
        assertEquals(3, res.paradas().size());
        assertEquals(25000.0, res.dineroRecaudadoEfectivo(), 0.01);
        assertEquals(18500.0, res.dineroPendienteEfectivo(), 0.01);

        assertTrue(res.paradas().get(0).esEfectivo());
        assertTrue(res.paradas().get(0).completada());
        assertEquals(101L, res.paradas().get(0).numeroPedido());
        assertEquals(25000.0, res.paradas().get(0).totalPedido(), 0.01);

        assertTrue(res.paradas().get(1).esEfectivo());
        assertFalse(res.paradas().get(1).completada());
        assertEquals(18500.0, res.paradas().get(1).totalPedido(), 0.01);

        assertFalse(res.paradas().get(2).esEfectivo());
        assertTrue(res.paradas().get(2).completada());
    }
}
