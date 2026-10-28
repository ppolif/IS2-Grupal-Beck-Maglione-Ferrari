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
import com.DulceMayor.backend.dtos.producto.SubCategoriaMapper;
import com.DulceMayor.backend.dtos.producto.SubCategoriaRequest;
import com.DulceMayor.backend.dtos.producto.SubCategoriaResponse;
import com.DulceMayor.backend.entities.producto.Categoria;
import com.DulceMayor.backend.entities.producto.SubCategoria;
import com.DulceMayor.backend.repositories.producto.SubCategoriaRepository;
import java.util.Optional;

import com.DulceMayor.backend.services.producto.CategoriaService;
import com.DulceMayor.backend.services.producto.SubCategoriaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SubCategoriaServiceTest {

    @Mock
    private SubCategoriaRepository subCategoriaRepository;

    @Mock
    private CategoriaService categoriaService;

    private final SubCategoriaMapper subCategoriaMapper = Mappers.getMapper(SubCategoriaMapper.class);

    private SubCategoriaService subCategoriaService;

    private Categoria categoriaMock;

    @BeforeEach
    void setUp() {
        subCategoriaService = new SubCategoriaService(subCategoriaRepository, subCategoriaMapper, categoriaService);
        categoriaMock = Categoria.builder()
                .id("cat-1")
                .nombre("Caramelos")
                .eliminado(false)
                .build();
    }

    @Test
    @DisplayName("crear: éxito asociando categoría y persistiendo con eliminado=false")
    void crearExitoso() {
        SubCategoriaRequest req = new SubCategoriaRequest("  Masticables  ", "cat-1");
        when(categoriaService.obtenerEntidad("cat-1")).thenReturn(categoriaMock);
        when(subCategoriaRepository.existsByNombreIgnoreCaseAndCategoriaIdAndEliminadoFalse("Masticables", "cat-1")).thenReturn(false);
        when(subCategoriaRepository.save(any(SubCategoria.class))).thenAnswer(invocation -> {
            SubCategoria s = invocation.getArgument(0);
            s.setId("sub-1");
            return s;
        });

        SubCategoriaResponse res = subCategoriaService.crear(req);

        assertNotNull(res);
        assertEquals("sub-1", res.id());
        assertEquals("Masticables", res.nombre());
        assertEquals("cat-1", res.categoriaId());

        ArgumentCaptor<SubCategoria> captor = ArgumentCaptor.forClass(SubCategoria.class);
        verify(subCategoriaRepository).save(captor.capture());
        assertEquals("Masticables", captor.getValue().getNombre());
        assertEquals(categoriaMock, captor.getValue().getCategoria());
    }

    @Test
    @DisplayName("crear: lanza ConflictoException si el nombre ya existe en la misma categoría")
    void crearDuplicadoLanzaConflicto() {
        SubCategoriaRequest req = new SubCategoriaRequest("Masticables", "cat-1");
        when(categoriaService.obtenerEntidad("cat-1")).thenReturn(categoriaMock);
        when(subCategoriaRepository.existsByNombreIgnoreCaseAndCategoriaIdAndEliminadoFalse("Masticables", "cat-1")).thenReturn(true);

        ConflictoException ex = assertThrows(ConflictoException.class, () -> subCategoriaService.crear(req));
        assertEquals("DUPLICADO", ex.getCode());
        verify(subCategoriaRepository, never()).save(any());
    }

    @Test
    @DisplayName("modificar: éxito actualizando nombre y categoría")
    void modificarExitoso() {
        SubCategoria existente = SubCategoria.builder()
                .id("sub-1")
                .nombre("Duros")
                .categoria(categoriaMock)
                .eliminado(false)
                .build();

        SubCategoriaRequest req = new SubCategoriaRequest("Duros Frutales", "cat-1");
        when(subCategoriaRepository.findByIdAndEliminadoFalse("sub-1")).thenReturn(Optional.of(existente));
        when(categoriaService.obtenerEntidad("cat-1")).thenReturn(categoriaMock);
        when(subCategoriaRepository.existsByNombreIgnoreCaseAndCategoriaIdAndIdNotAndEliminadoFalse("Duros Frutales", "cat-1", "sub-1")).thenReturn(false);
        when(subCategoriaRepository.save(any(SubCategoria.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SubCategoriaResponse res = subCategoriaService.modificar("sub-1", req);

        assertEquals("Duros Frutales", res.nombre());
        assertEquals("cat-1", res.categoriaId());
    }

    @Test
    @DisplayName("eliminar: aplica borrado lógico seteando eliminado=true")
    void eliminarBorradoLogico() {
        SubCategoria existente = SubCategoria.builder()
                .id("sub-1")
                .nombre("Masticables")
                .categoria(categoriaMock)
                .eliminado(false)
                .build();

        when(subCategoriaRepository.findByIdAndEliminadoFalse("sub-1")).thenReturn(Optional.of(existente));

        subCategoriaService.eliminar("sub-1");

        assertTrue(existente.isEliminado());
        verify(subCategoriaRepository).save(existente);
    }

    @Test
    @DisplayName("buscar: lanza RecursoNoEncontradoException cuando no existe o está eliminada")
    void buscarInexistenteLanza404() {
        when(subCategoriaRepository.findByIdAndEliminadoFalse("inexistente")).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> subCategoriaService.buscar("inexistente"));
    }
}
