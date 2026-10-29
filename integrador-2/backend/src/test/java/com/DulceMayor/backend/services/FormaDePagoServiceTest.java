package com.DulceMayor.backend.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.DulceMayor.backend.config.RecursoNoEncontradoException;
import com.DulceMayor.backend.config.ValidacionException;
import com.DulceMayor.backend.dtos.facturacion.FormaDePagoMapper;
import com.DulceMayor.backend.dtos.facturacion.FormaDePagoRequest;
import com.DulceMayor.backend.dtos.facturacion.FormaDePagoResponse;
import com.DulceMayor.backend.entities.facturacion.FormaDePago;
import com.DulceMayor.backend.entities.facturacion.TipoPago;
import com.DulceMayor.backend.repositories.facturacion.FormaDePagoRepository;
import com.DulceMayor.backend.services.facturacion.FormaDePagoService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests unitarios de {@link FormaDePagoService} con Mockito.
 */
@ExtendWith(MockitoExtension.class)
class FormaDePagoServiceTest {

    @Mock
    private FormaDePagoRepository formaDePagoRepository;

    private final FormaDePagoMapper formaDePagoMapper = Mappers.getMapper(FormaDePagoMapper.class);

    private FormaDePagoService formaDePagoService;

    @BeforeEach
    void setUp() {
        formaDePagoService = new FormaDePagoService(formaDePagoRepository, formaDePagoMapper);
    }

    @Test
    @DisplayName("crear: normaliza observación, persiste con eliminado=false y devuelve FormaDePagoResponse")
    void crearFormaDePagoExitoso() {
        FormaDePagoRequest request = new FormaDePagoRequest(TipoPago.TRANSFERENCIA, "  CBU Banco Nación  ");

        when(formaDePagoRepository.save(any(FormaDePago.class))).thenAnswer(inv -> {
            FormaDePago entidad = inv.getArgument(0);
            entidad.setId("fp-1");
            return entidad;
        });

        FormaDePagoResponse response = formaDePagoService.crear(request);

        assertNotNull(response);
        assertEquals("fp-1", response.id());
        assertEquals(TipoPago.TRANSFERENCIA, response.tipoPago());
        assertEquals("CBU Banco Nación", response.observacion());
    }

    @Test
    @DisplayName("crear: lanza ValidacionException cuando tipoPago es nulo")
    void crearSinTipoPagoLanzaValidacion() {
        FormaDePagoRequest request = new FormaDePagoRequest(null, "Efectivo en caja");

        assertThrows(ValidacionException.class, () -> formaDePagoService.crear(request));
    }

    @Test
    @DisplayName("modificar: actualiza tipoPago y observación")
    void modificarFormaDePagoExitoso() {
        FormaDePago existente = FormaDePago.builder()
                .id("fp-1")
                .tipoPago(TipoPago.EFECTIVO)
                .observacion("Caja mostrador")
                .eliminado(false)
                .build();

        when(formaDePagoRepository.findByIdAndEliminadoFalse("fp-1")).thenReturn(Optional.of(existente));
        when(formaDePagoRepository.save(any(FormaDePago.class))).thenAnswer(inv -> inv.getArgument(0));

        FormaDePagoResponse res = formaDePagoService.modificar(
                "fp-1",
                new FormaDePagoRequest(TipoPago.BILLETERA_VIRTUAL, "  MercadoPago QR ")
        );

        assertEquals("fp-1", res.id());
        assertEquals(TipoPago.BILLETERA_VIRTUAL, res.tipoPago());
        assertEquals("MercadoPago QR", res.observacion());
    }

    @Test
    @DisplayName("eliminar: marca eliminado=true (borrado lógico)")
    void eliminarFormaDePagoBorradoLogico() {
        FormaDePago existente = FormaDePago.builder()
                .id("fp-1")
                .tipoPago(TipoPago.EFECTIVO)
                .eliminado(false)
                .build();

        when(formaDePagoRepository.findByIdAndEliminadoFalse("fp-1")).thenReturn(Optional.of(existente));
        when(formaDePagoRepository.save(any(FormaDePago.class))).thenAnswer(inv -> inv.getArgument(0));

        formaDePagoService.eliminar("fp-1");

        assertTrue(existente.isEliminado());
        verify(formaDePagoRepository).save(existente);
    }

    @Test
    @DisplayName("buscar: lanza RecursoNoEncontradoException si no existe")
    void buscarInexistenteLanza404() {
        when(formaDePagoRepository.findByIdAndEliminadoFalse("no-existe")).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> formaDePagoService.buscar("no-existe"));
    }
}

