package com.DulceMayor.backend.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.DulceMayor.backend.config.ConflictoException;
import com.DulceMayor.backend.dtos.facturacion.PagoMapper;
import com.DulceMayor.backend.dtos.facturacion.PagoRequest;
import com.DulceMayor.backend.dtos.facturacion.PagoResponse;
import com.DulceMayor.backend.entities.facturacion.EstadoFactura;
import com.DulceMayor.backend.entities.facturacion.Factura;
import com.DulceMayor.backend.entities.facturacion.FormaDePago;
import com.DulceMayor.backend.entities.facturacion.Pago;
import com.DulceMayor.backend.entities.facturacion.TipoPago;
import com.DulceMayor.backend.repositories.facturacion.PagoRepository;
import com.DulceMayor.backend.services.facturacion.FacturaLecturaService;
import com.DulceMayor.backend.services.facturacion.FacturaService;
import com.DulceMayor.backend.services.facturacion.FormaDePagoService;
import com.DulceMayor.backend.services.facturacion.PagoService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests unitarios de {@link PagoService} con Mockito.
 */
@ExtendWith(MockitoExtension.class)
class PagoServiceTest {

    @Mock
    private PagoRepository pagoRepository;

    @Mock
    private FacturaService facturaService;

    @Mock
    private FacturaLecturaService facturaLecturaService;

    @Mock
    private FormaDePagoService formaDePagoService;

    private final PagoMapper pagoMapper = Mappers.getMapper(PagoMapper.class);

    private PagoService pagoService;

    @BeforeEach
    void setUp() {
        pagoService = new PagoService(
                pagoRepository,
                pagoMapper,
                facturaService,
                facturaLecturaService,
                formaDePagoService
        );
    }

    @Test
    @DisplayName("registrarPago: registra pago parcial o total y actualiza el estado de la factura")
    void registrarPagoExitoso() {
        Factura factura = Factura.builder()
                .id("fac-1")
                .numeroFactura(1L)
                .total(1000.0)
                .estado(EstadoFactura.PENDIENTE_PAGO)
                .eliminado(false)
                .build();

        FormaDePago forma = FormaDePago.builder()
                .id("fp-1")
                .tipoPago(TipoPago.EFECTIVO)
                .eliminado(false)
                .build();

        when(facturaService.obtenerEntidad("fac-1")).thenReturn(factura);
        when(formaDePagoService.obtenerEntidad("fp-1")).thenReturn(forma);
        when(facturaLecturaService.obtenerTotalPagado("fac-1")).thenReturn(400.0);
        when(pagoRepository.save(any(Pago.class))).thenAnswer(inv -> {
            Pago p = inv.getArgument(0);
            p.setId("pago-1");
            return p;
        });

        PagoResponse res = pagoService.registrarPago("fac-1", new PagoRequest("fp-1", 600.0, "REF-1", "Cancelación saldo"));

        assertNotNull(res);
        assertEquals("pago-1", res.id());
        assertEquals(600.0, res.monto());
        assertEquals(TipoPago.EFECTIVO, res.formaDePago().tipoPago());
        verify(facturaService).actualizarEstadoSegunPagos(factura, 1000.0);
    }

    @Test
    @DisplayName("registrarPago: lanza ConflictoException si el monto supera el saldo pendiente")
    void registrarPagoExcedeSaldoLanzaConflicto() {
        Factura factura = Factura.builder()
                .id("fac-1")
                .numeroFactura(1L)
                .total(1000.0)
                .estado(EstadoFactura.PENDIENTE_PAGO)
                .eliminado(false)
                .build();

        FormaDePago forma = FormaDePago.builder()
                .id("fp-1")
                .tipoPago(TipoPago.TRANSFERENCIA)
                .eliminado(false)
                .build();

        when(facturaService.obtenerEntidad("fac-1")).thenReturn(factura);
        when(formaDePagoService.obtenerEntidad("fp-1")).thenReturn(forma);
        when(facturaLecturaService.obtenerTotalPagado("fac-1")).thenReturn(700.0);

        ConflictoException ex = assertThrows(
                ConflictoException.class,
                () -> pagoService.registrarPago("fac-1", new PagoRequest("fp-1", 350.0, null, null))
        );
        assertEquals("CONFLICTO", ex.getCode());
    }

    @Test
    @DisplayName("eliminar: realiza borrado lógico del pago y recalcula el estado de la factura")
    void eliminarPagoRecalculaEstadoFactura() {
        Factura factura = Factura.builder()
                .id("fac-1")
                .numeroFactura(1L)
                .total(1000.0)
                .estado(EstadoFactura.PAGADA)
                .eliminado(false)
                .build();

        Pago pago = Pago.builder()
                .id("pago-1")
                .factura(factura)
                .monto(1000.0)
                .eliminado(false)
                .build();

        when(pagoRepository.findByIdAndEliminadoFalse("pago-1")).thenReturn(Optional.of(pago));
        when(pagoRepository.save(any(Pago.class))).thenAnswer(inv -> inv.getArgument(0));
        when(facturaLecturaService.obtenerTotalPagado("fac-1")).thenReturn(0.0);

        pagoService.eliminar("pago-1");

        verify(facturaService).actualizarEstadoSegunPagos(factura, 0.0);
    }
}
