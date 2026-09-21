package com.example.zero.services;

import com.example.zero.entidades.producto.Categoria;
import com.example.zero.entidades.producto.SubCategoria;
import com.example.zero.repositories.SubCategoriaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubCategoriaServiceTest {

    @Mock
    private SubCategoriaRepository subCategoriaRepository;

    @Mock
    private CategoriaService categoriaService;

    @InjectMocks
    private SubCategoriaService subCategoriaService;

    @Test
    void crearSubCategoria_conDatosValidos_guardaYRetornaSubCategoria() {
        // Arrange
        Categoria cat = Categoria.builder().id("cat-1").nombre("Hombres").eliminado(false).build();
        when(categoriaService.buscarPorId("cat-1")).thenReturn(cat);
        when(subCategoriaRepository.save(any(SubCategoria.class))).thenAnswer(invocation -> {
            SubCategoria s = invocation.getArgument(0);
            s.setId("sub-1");
            return s;
        });

        // Act
        SubCategoria resultado = subCategoriaService.crearSubCategoria("Pantalones", "cat-1");

        // Assert
        assertNotNull(resultado);
        assertEquals("sub-1", resultado.getId());
        assertEquals("Pantalones", resultado.getNombre());
        assertEquals("cat-1", resultado.getCategoria().getId());
        assertFalse(resultado.isEliminado());
        verify(subCategoriaRepository, times(1)).save(any(SubCategoria.class));
    }

    @Test
    void crearSubCategoria_conNombreVacio_lanzaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> subCategoriaService.crearSubCategoria("", "cat-1"));
        assertThrows(IllegalArgumentException.class, () -> subCategoriaService.crearSubCategoria(null, "cat-1"));
        verify(subCategoriaRepository, never()).save(any());
    }

    @Test
    void crearSubCategoria_conCategoriaIdVacio_lanzaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> subCategoriaService.crearSubCategoria("Pantalones", ""));
        assertThrows(IllegalArgumentException.class, () -> subCategoriaService.crearSubCategoria("Pantalones", null));
        verify(subCategoriaRepository, never()).save(any());
    }

    @Test
    void crearSubCategoria_categoriaInexistente_propagaExcepcion() {
        when(categoriaService.buscarPorId("cat-inexistente"))
                .thenThrow(new IllegalArgumentException("No se encontró la categoría activa"));

        assertThrows(IllegalArgumentException.class,
                () -> subCategoriaService.crearSubCategoria("Pantalones", "cat-inexistente"));
        verify(subCategoriaRepository, never()).save(any());
    }

    @Test
    void modificarSubCategoria_conDatosValidos_actualizaYRetorna() {
        // Arrange
        Categoria cat1 = Categoria.builder().id("cat-1").nombre("Hombres").build();
        Categoria cat2 = Categoria.builder().id("cat-2").nombre("Mujeres").build();
        SubCategoria sub = SubCategoria.builder().id("sub-1").nombre("Remeras").categoria(cat1).eliminado(false).build();

        when(subCategoriaRepository.findActive("sub-1")).thenReturn(Optional.of(sub));
        when(categoriaService.buscarPorId("cat-2")).thenReturn(cat2);
        when(subCategoriaRepository.save(any(SubCategoria.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        SubCategoria actualizada = subCategoriaService.modificarSubCategoria("sub-1", "Remeras Estampadas", "cat-2");

        // Assert
        assertEquals("Remeras Estampadas", actualizada.getNombre());
        assertEquals("cat-2", actualizada.getCategoria().getId());
        verify(subCategoriaRepository, times(1)).save(sub);
    }

    @Test
    void modificarSubCategoria_inexistente_lanzaIllegalArgumentException() {
        when(subCategoriaRepository.findActive("sub-inexistente")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> subCategoriaService.modificarSubCategoria("sub-inexistente", "Nuevo", "cat-1"));
        verify(subCategoriaRepository, never()).save(any());
    }

    @Test
    void eliminarSubCategoria_existente_marcaEliminado() {
        SubCategoria sub = SubCategoria.builder().id("sub-1").nombre("Zapatillas").eliminado(false).build();
        when(subCategoriaRepository.findActive("sub-1")).thenReturn(Optional.of(sub));

        subCategoriaService.eliminarSubCategoria("sub-1");

        assertTrue(sub.isEliminado());
        verify(subCategoriaRepository, times(1)).save(sub);
    }

    @Test
    void buscarPorId_existente_retornaSubCategoria() {
        SubCategoria sub = SubCategoria.builder().id("sub-1").nombre("Camisas").eliminado(false).build();
        when(subCategoriaRepository.findActive("sub-1")).thenReturn(Optional.of(sub));

        SubCategoria encontrada = subCategoriaService.buscarPorId("sub-1");

        assertNotNull(encontrada);
        assertEquals("Camisas", encontrada.getNombre());
    }

    @Test
    void buscarPorId_inexistente_lanzaIllegalArgumentException() {
        when(subCategoriaRepository.findActive("sub-inexistente")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> subCategoriaService.buscarPorId("sub-inexistente"));
    }

    @Test
    void listarPorCategoria_retornaSubCategoriasDeLaCategoria() {
        List<SubCategoria> subs = List.of(
                SubCategoria.builder().id("s1").nombre("Buzos").build(),
                SubCategoria.builder().id("s2").nombre("Camperas").build()
        );
        when(subCategoriaRepository.findByCategoriaIdAndEliminadoFalse("cat-1")).thenReturn(subs);

        List<SubCategoria> resultado = subCategoriaService.listarPorCategoria("cat-1");

        assertEquals(2, resultado.size());
        verify(subCategoriaRepository, times(1)).findByCategoriaIdAndEliminadoFalse("cat-1");
    }

    @Test
    void listarActivas_retornaListaDeSubCategorias() {
        List<SubCategoria> subs = List.of(
                SubCategoria.builder().id("s1").nombre("Buzos").build()
        );
        when(subCategoriaRepository.findByEliminadoFalse()).thenReturn(subs);

        List<SubCategoria> resultado = subCategoriaService.listarActivas();

        assertEquals(1, resultado.size());
        verify(subCategoriaRepository, times(1)).findByEliminadoFalse();
    }
}

