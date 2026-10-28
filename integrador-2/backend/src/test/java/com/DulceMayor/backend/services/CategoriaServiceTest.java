package com.DulceMayor.backend.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.DulceMayor.backend.config.ConflictoException;
import com.DulceMayor.backend.config.RecursoNoEncontradoException;
import com.DulceMayor.backend.dtos.producto.CategoriaMapper;
import com.DulceMayor.backend.dtos.producto.CategoriaRequest;
import com.DulceMayor.backend.dtos.producto.CategoriaResponse;
import com.DulceMayor.backend.entities.producto.Categoria;
import com.DulceMayor.backend.repositories.producto.CategoriaRepository;
import java.util.Optional;

import com.DulceMayor.backend.services.producto.CategoriaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests unitarios de {@link CategoriaService} con Mockito.
 * Cubre: crear, duplicado, modificar, eliminar (borrado lógico) y buscar inexistente.
 */
@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    private final CategoriaMapper categoriaMapper = Mappers.getMapper(CategoriaMapper.class);

    private CategoriaService categoriaService;

    @BeforeEach
    void setUp() {
        categoriaService = new CategoriaService(categoriaRepository, categoriaMapper);
    }

    @Test
    @DisplayName("crear: normaliza espacios sobrantes, persiste con eliminado=false y devuelve CategoriaResponse")
    void crearCategoriaExitoso() {
        CategoriaRequest request = new CategoriaRequest("  Chocolates  ");

        when(categoriaRepository.existsByNombreIgnoreCaseAndEliminadoFalse("Chocolates")).thenReturn(false);
        when(categoriaRepository.save(any(Categoria.class))).thenAnswer(invocation -> {
            Categoria entidad = invocation.getArgument(0);
            entidad.setId("cat-uuid-1");
            return entidad;
        });

        CategoriaResponse response = categoriaService.crear(request);

        assertNotNull(response);
        assertEquals("cat-uuid-1", response.id());
        assertEquals("Chocolates", response.nombre());

        ArgumentCaptor<Categoria> captor = ArgumentCaptor.forClass(Categoria.class);
        verify(categoriaRepository).save(captor.capture());
        assertEquals("Chocolates", captor.getValue().getNombre());
        assertEquals(false, captor.getValue().isEliminado());
    }

    @Test
    @DisplayName("crear: lanza ConflictoException con code=DUPLICADO si el nombre ya existe (sin distinguir mayúsculas)")
    void crearCategoriaDuplicadaLanzaConflicto() {
        CategoriaRequest request = new CategoriaRequest("chocolates");

        when(categoriaRepository.existsByNombreIgnoreCaseAndEliminadoFalse("chocolates")).thenReturn(true);

        ConflictoException ex = assertThrows(
                ConflictoException.class,
                () -> categoriaService.crear(request)
        );

        assertEquals("DUPLICADO", ex.getCode());
        verify(categoriaRepository, never()).save(any());
    }

    @Test
    @DisplayName("modificar: actualiza el nombre sin chocar consigo misma y devuelve CategoriaResponse")
    void modificarCategoriaExitoso() {
        String id = "cat-uuid-1";
        Categoria existente = Categoria.builder()
                .id(id)
                .nombre("Chocolates")
                .eliminado(false)
                .build();

        CategoriaRequest request = new CategoriaRequest("  Chocolates Artesanales  ");

        when(categoriaRepository.findByIdAndEliminadoFalse(id)).thenReturn(Optional.of(existente));
        when(categoriaRepository.existsByNombreIgnoreCaseAndIdNotAndEliminadoFalse("Chocolates Artesanales", id))
                .thenReturn(false);
        when(categoriaRepository.save(any(Categoria.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CategoriaResponse response = categoriaService.modificar(id, request);

        assertEquals(id, response.id());
        assertEquals("Chocolates Artesanales", response.nombre());
        verify(categoriaRepository).save(existente);
    }

    @Test
    @DisplayName("eliminar: realiza borrado lógico marcando eliminado=true")
    void eliminarCategoriaMarcaEliminadoTrue() {
        String id = "cat-uuid-1";
        Categoria existente = Categoria.builder()
                .id(id)
                .nombre("Caramelos")
                .eliminado(false)
                .build();

        when(categoriaRepository.findByIdAndEliminadoFalse(id)).thenReturn(Optional.of(existente));
        when(categoriaRepository.save(any(Categoria.class))).thenAnswer(invocation -> invocation.getArgument(0));

        categoriaService.eliminar(id);

        assertTrue(existente.isEliminado());
        verify(categoriaRepository).save(existente);
    }

    @Test
    @DisplayName("buscar: lanza RecursoNoEncontradoException (NO_ENCONTRADO) cuando no existe o está eliminada")
    void buscarCategoriaInexistenteLanzaNoEncontrado() {
        String id = "id-inexistente";
        when(categoriaRepository.findByIdAndEliminadoFalse(id)).thenReturn(Optional.empty());

        RecursoNoEncontradoException ex = assertThrows(
                RecursoNoEncontradoException.class,
                () -> categoriaService.buscar(id)
        );

        assertEquals("NO_ENCONTRADO", ex.getCode());
    }
}

