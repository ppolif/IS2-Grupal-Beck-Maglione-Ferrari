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
import com.DulceMayor.backend.dtos.producto.PromocionMapper;
import com.DulceMayor.backend.dtos.producto.PromocionRequest;
import com.DulceMayor.backend.dtos.producto.PromocionResponse;
import com.DulceMayor.backend.entities.producto.Promocion;
import com.DulceMayor.backend.repositories.producto.PromocionRepository;
import java.time.LocalDate;
import java.util.Optional;

import com.DulceMayor.backend.services.producto.ProductoService;
import com.DulceMayor.backend.services.producto.PromocionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PromocionServiceTest {

    @Mock
    private PromocionRepository promocionRepository;

    @Mock
    private ProductoService productoService;

    private final PromocionMapper promocionMapper = Mappers.getMapper(PromocionMapper.class);

    private PromocionService promocionService;

    @BeforeEach
    void setUp() {
        promocionService = new PromocionService(promocionRepository, promocionMapper, productoService);
    }

    @Test
    @DisplayName("crear: normaliza código a mayúsculas, valida unicidad y persiste")
    void crearExitoso() {
        LocalDate hoy = LocalDate.now();
        PromocionRequest req = new PromocionRequest(
                "  dulce2026  ",
                "Descuento de primavera",
                15.0,
                10,
                null,
                hoy,
                hoy.plusDays(30)
        );

        when(promocionRepository.existsByCodigoIgnoreCaseAndEliminadoFalse("DULCE2026")).thenReturn(false);
        when(promocionRepository.save(any(Promocion.class))).thenAnswer(invocation -> {
            Promocion p = invocation.getArgument(0);
            p.setId("promo-1");
            return p;
        });

        PromocionResponse res = promocionService.crear(req);

        assertNotNull(res);
        assertEquals("promo-1", res.id());
        assertEquals("DULCE2026", res.codigo());
        assertEquals(15.0, res.porcentajeDescuento());
    }

    @Test
    @DisplayName("crear: fechaHasta anterior a fechaDesde lanza ValidacionException")
    void crearFechasInvalidas() {
        LocalDate hoy = LocalDate.now();
        PromocionRequest req = new PromocionRequest(
                "PROMO",
                "Desc",
                10.0,
                null,
                null,
                hoy,
                hoy.minusDays(1)
        );

        assertThrows(ValidacionException.class, () -> promocionService.crear(req));
    }

    @Test
    @DisplayName("buscarVigentePorCodigo: devuelve promoción si fecha actual está en rango")
    void buscarVigentePorCodigoExitoso() {
        LocalDate hoy = LocalDate.now();
        Promocion promo = Promocion.builder()
                .id("promo-1")
                .codigo("VERANO10")
                .fechaDesde(hoy.minusDays(5))
                .fechaHasta(hoy.plusDays(5))
                .porcentajeDescuento(10.0)
                .eliminado(false)
                .build();

        when(promocionRepository.findByCodigoIgnoreCaseAndEliminadoFalse("VERANO10")).thenReturn(Optional.of(promo));

        PromocionResponse res = promocionService.buscarVigentePorCodigo("verano10");

        assertNotNull(res);
        assertEquals("VERANO10", res.codigo());
    }

    @Test
    @DisplayName("buscarVigentePorCodigo: lanza RecursoNoEncontradoException si la promoción ya expiró")
    void buscarVigentePorCodigoExpirada() {
        LocalDate hoy = LocalDate.now();
        Promocion promoExpirada = Promocion.builder()
                .id("promo-1")
                .codigo("VIEJA")
                .fechaDesde(hoy.minusDays(20))
                .fechaHasta(hoy.minusDays(5))
                .porcentajeDescuento(10.0)
                .eliminado(false)
                .build();

        when(promocionRepository.findByCodigoIgnoreCaseAndEliminadoFalse("VIEJA")).thenReturn(Optional.of(promoExpirada));

        assertThrows(RecursoNoEncontradoException.class, () -> promocionService.buscarVigentePorCodigo("VIEJA"));
    }
}

