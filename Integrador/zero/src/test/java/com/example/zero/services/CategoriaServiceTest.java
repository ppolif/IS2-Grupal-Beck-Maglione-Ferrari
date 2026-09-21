package com.example.zero.services;

import com.example.zero.entidades.producto.Categoria;
import com.example.zero.entidades.producto.SubCategoria;
import com.example.zero.repositories.CategoriaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private CategoriaService categoriaService;

    @Test
    void crearCategoria_conNombreValido_guardaYRetornaCategoria() {
        // Arrange
        String nombre = "Hombres";
        when(categoriaRepository.findByNombreAndEliminadoFalse("Hombres")).thenReturn(Optional.empty());
        when(categoriaRepository.save(any(Categoria.class))).thenAnswer(invocation -> {
            Categoria c = invocation.getArgument(0);
            c.setId("cat-1");
            return c;
        });

        // Act
        Categoria resultado = categoriaService.crearCategoria(nombre);

        // Assert
        assertNotNull(resultado);
        assertEquals("cat-1", resultado.getId());
        assertEquals("Hombres", resultado.getNombre());
        assertFalse(resultado.isEliminado());
        verify(categoriaRepository, times(1)).save(any(Categoria.class));
    }

    @Test
    void crearCategoria_conNombreVacioONulo_lanzaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> categoriaService.crearCategoria(null));
        assertThrows(IllegalArgumentException.class, () -> categoriaService.crearCategoria("   "));
        verify(categoriaRepository, never()).save(any());
    }

    @Test
    void crearCategoria_conNombreDuplicado_lanzaIllegalArgumentException() {
        // Arrange
        Categoria existente = Categoria.builder().id("cat-existente").nombre("Mujeres").build();
        when(categoriaRepository.findByNombreAndEliminadoFalse("Mujeres")).thenReturn(Optional.of(existente));

        // Act & Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> categoriaService.crearCategoria("Mujeres"));
        assertTrue(ex.getMessage().contains("Ya existe una categoría activa"));
        verify(categoriaRepository, never()).save(any());
    }

    @Test
    void modificarCategoria_conDatosValidos_actualizaYRetorna() {
        // Arrange
        Categoria existente = Categoria.builder().id("cat-1").nombre("Niño").eliminado(false).build();
        when(categoriaRepository.findActive("cat-1")).thenReturn(Optional.of(existente));
        when(categoriaRepository.findByNombreAndEliminadoFalse("Niños")).thenReturn(Optional.empty());
        when(categoriaRepository.save(any(Categoria.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Categoria actualizada = categoriaService.modificarCategoria("cat-1", "Niños");

        // Assert
        assertEquals("Niños", actualizada.getNombre());
        verify(categoriaRepository, times(1)).save(existente);
    }

    @Test
    void modificarCategoria_inexistente_lanzaIllegalArgumentException() {
        when(categoriaRepository.findActive("cat-999")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> categoriaService.modificarCategoria("cat-999", "Nueva"));
        verify(categoriaRepository, never()).save(any());
    }

    @Test
    void modificarCategoria_nombreDuplicadoEnOtraCategoria_lanzaIllegalArgumentException() {
        // Arrange
        Categoria cat1 = Categoria.builder().id("cat-1").nombre("Niños").eliminado(false).build();
        Categoria cat2 = Categoria.builder().id("cat-2").nombre("Niñas").eliminado(false).build();
        when(categoriaRepository.findActive("cat-1")).thenReturn(Optional.of(cat1));
        when(categoriaRepository.findByNombreAndEliminadoFalse("Niñas")).thenReturn(Optional.of(cat2));

        // Act & Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> categoriaService.modificarCategoria("cat-1", "Niñas"));
        assertTrue(ex.getMessage().contains("Ya existe otra categoría"));
        verify(categoriaRepository, never()).save(any());
    }

    @Test
    void eliminarCategoria_existente_marcaEliminadoYCascadeaASubcategorias() {
        // Arrange
        SubCategoria sub1 = SubCategoria.builder().id("sub-1").nombre("Remeras").eliminado(false).build();
        Set<SubCategoria> subs = new HashSet<>();
        subs.add(sub1);

        Categoria categoria = Categoria.builder().id("cat-1").nombre("Hombres").eliminado(false).subCategorias(subs).build();
        when(categoriaRepository.findActive("cat-1")).thenReturn(Optional.of(categoria));

        // Act
        categoriaService.eliminarCategoria("cat-1");

        // Assert
        assertTrue(categoria.isEliminado());
        assertTrue(sub1.isEliminado());
        verify(categoriaRepository, times(1)).save(categoria);
    }

    @Test
    void buscarPorId_existente_retornaCategoria() {
        Categoria categoria = Categoria.builder().id("cat-1").nombre("Hombres").eliminado(false).build();
        when(categoriaRepository.findActive("cat-1")).thenReturn(Optional.of(categoria));

        Categoria encontrada = categoriaService.buscarPorId("cat-1");

        assertNotNull(encontrada);
        assertEquals("Hombres", encontrada.getNombre());
    }

    @Test
    void buscarPorId_inexistente_lanzaIllegalArgumentException() {
        when(categoriaRepository.findActive("cat-inexistente")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> categoriaService.buscarPorId("cat-inexistente"));
    }

    @Test
    void buscarPorNombre_existente_retornaCategoria() {
        Categoria categoria = Categoria.builder().id("cat-1").nombre("Niñas").eliminado(false).build();
        when(categoriaRepository.findByNombreAndEliminadoFalse("Niñas")).thenReturn(Optional.of(categoria));

        Categoria encontrada = categoriaService.buscarPorNombre("Niñas");

        assertNotNull(encontrada);
        assertEquals("Niñas", encontrada.getNombre());
    }

    @Test
    void listarActivas_retornaListaDeCategorias() {
        List<Categoria> categorias = List.of(
                Categoria.builder().id("c1").nombre("Hombres").build(),
                Categoria.builder().id("c2").nombre("Mujeres").build()
        );
        when(categoriaRepository.findByEliminadoFalse()).thenReturn(categorias);

        List<Categoria> activas = categoriaService.listarActivas();

        assertEquals(2, activas.size());
        verify(categoriaRepository, times(1)).findByEliminadoFalse();
    }

    @Test
    void listarTodas_retornaListaCompleta() {
        List<Categoria> categorias = List.of(
                Categoria.builder().id("c1").nombre("Hombres").eliminado(false).build(),
                Categoria.builder().id("c2").nombre("Mujeres").eliminado(true).build()
        );
        when(categoriaRepository.findAll()).thenReturn(categorias);

        List<Categoria> todas = categoriaService.listarTodas();

        assertEquals(2, todas.size());
        verify(categoriaRepository, times(1)).findAll();
    }
}

