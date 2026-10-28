package com.DulceMayor.backend.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.DulceMayor.backend.config.ConflictoException;
import com.DulceMayor.backend.dtos.producto.VigenciaPrecioMapper;
import com.DulceMayor.backend.dtos.producto.VigenciaPrecioRequest;
import com.DulceMayor.backend.dtos.producto.VigenciaPrecioResponse;
import com.DulceMayor.backend.entities.producto.Producto;
import com.DulceMayor.backend.entities.producto.VigenciaPrecio;
import com.DulceMayor.backend.repositories.producto.VigenciaPrecioRepository;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.DulceMayor.backend.services.producto.ProductoService;
import com.DulceMayor.backend.services.producto.VigenciaPrecioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VigenciaPrecioServiceTest {

    @Mock
    private VigenciaPrecioRepository vigenciaPrecioRepository;

    @Mock
    private ProductoService productoService;

    private final VigenciaPrecioMapper vigenciaPrecioMapper = Mappers.getMapper(VigenciaPrecioMapper.class);

    private VigenciaPrecioService vigenciaPrecioService;

    private Producto productoMock;

    @BeforeEach
    void setUp() {
        vigenciaPrecioService = new VigenciaPrecioService(vigenciaPrecioRepository, vigenciaPrecioMapper, productoService);
        productoMock = Producto.builder()
                .id("prod-1")
                .nombre("Caramelo Ácido")
                .codigo("CAR-01")
                .eliminado(false)
                .build();
    }

    @Test
    @DisplayName("registrarPrecio: cierra la vigencia previa al día anterior e inserta la nueva")
    void registrarPrecioCierraPrevia() {
        LocalDate hoy = LocalDate.of(2026, 10, 27);
        LocalDate inicioPrevio = LocalDate.of(2026, 9, 1);

        VigenciaPrecio previa = VigenciaPrecio.builder()
                .id("vig-ant")
                .producto(productoMock)
                .fechaDesde(inicioPrevio)
                .fechaHasta(null)
                .precio(100.0)
                .eliminado(false)
                .build();

        VigenciaPrecioRequest req = new VigenciaPrecioRequest(hoy, null, 150.0);

        when(productoService.obtenerEntidad("prod-1")).thenReturn(productoMock);
        when(vigenciaPrecioRepository.findByProductoIdAndFechaHastaIsNullAndEliminadoFalse("prod-1")).thenReturn(Optional.of(previa));
        when(vigenciaPrecioRepository.buscarSolapamientos("prod-1", hoy, null, null)).thenReturn(Collections.emptyList());
        when(vigenciaPrecioRepository.save(any(VigenciaPrecio.class))).thenAnswer(invocation -> {
            VigenciaPrecio v = invocation.getArgument(0);
            if (v.getId() == null) {
                v.setId("vig-nueva");
            }
            return v;
        });

        VigenciaPrecioResponse res = vigenciaPrecioService.registrarPrecio("prod-1", req);

        assertNotNull(res);
        assertEquals("vig-nueva", res.id());
        assertEquals(150.0, res.precio());
        // Verifica que la previa se cerró con fechaHasta = hoy - 1
        assertEquals(hoy.minusDays(1), previa.getFechaHasta());
    }

    @Test
    @DisplayName("registrarPrecio: solapamiento detectado lanza ConflictoException")
    void registrarPrecioSolapamientoLanzaConflicto() {
        LocalDate hoy = LocalDate.of(2026, 10, 27);
        VigenciaPrecioRequest req = new VigenciaPrecioRequest(hoy, null, 150.0);

        when(productoService.obtenerEntidad("prod-1")).thenReturn(productoMock);
        when(vigenciaPrecioRepository.findByProductoIdAndFechaHastaIsNullAndEliminadoFalse("prod-1")).thenReturn(Optional.empty());
        when(vigenciaPrecioRepository.buscarSolapamientos("prod-1", hoy, null, null)).thenReturn(List.of(new VigenciaPrecio()));

        assertThrows(ConflictoException.class, () -> vigenciaPrecioService.registrarPrecio("prod-1", req));
    }

    @Test
    @DisplayName("buscarVigente: devuelve la vigencia abierta activa")
    void buscarVigenteExitoso() {
        VigenciaPrecio vigente = VigenciaPrecio.builder()
                .id("vig-1")
                .producto(productoMock)
                .fechaDesde(LocalDate.of(2026, 1, 1))
                .fechaHasta(null)
                .precio(250.0)
                .eliminado(false)
                .build();

        when(productoService.obtenerEntidad("prod-1")).thenReturn(productoMock);
        when(vigenciaPrecioRepository.findByProductoIdAndFechaHastaIsNullAndEliminadoFalse("prod-1")).thenReturn(Optional.of(vigente));

        VigenciaPrecioResponse res = vigenciaPrecioService.buscarVigente("prod-1");
        assertNotNull(res);
        assertEquals(250.0, res.precio());
    }
}

