package com.example.persona.patterns.strategy;

import com.example.persona.entities.Autor;
import com.example.persona.entities.Libro;
import com.example.persona.repositories.LibroRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LibroSearchStrategyTest {

    private LibroRepository libroRepository;
    private BuscarPorTituloStrategy porTitulo;
    private BuscarPorGeneroStrategy porGenero;
    private BuscarPorAutorStrategy porAutor;
    private LibroSearchStrategyFactory factory;

    @BeforeEach
    void setUp() {
        libroRepository = Mockito.mock(LibroRepository.class);
        porTitulo = new BuscarPorTituloStrategy();
        porGenero = new BuscarPorGeneroStrategy();
        porAutor = new BuscarPorAutorStrategy();
        factory = new LibroSearchStrategyFactory(porTitulo, porGenero, porAutor);
    }

    @Test
    @DisplayName("BuscarPorTituloStrategy debe invocar searchByTitulo en el repositorio")
    void testBuscarPorTituloStrategy() {
        Libro libro = new Libro();
        libro.setTitulo("Don Quijote");
        when(libroRepository.searchByTitulo("Quijote")).thenReturn(List.of(libro));

        LibroSearchContext context = new LibroSearchContext(porTitulo);
        List<Libro> resultado = context.ejecutar("Quijote", libroRepository);

        assertEquals(1, resultado.size());
        assertEquals("Don Quijote", resultado.get(0).getTitulo());
        verify(libroRepository, times(1)).searchByTitulo("Quijote");
    }

    @Test
    @DisplayName("BuscarPorGeneroStrategy debe invocar searchByGenero en el repositorio")
    void testBuscarPorGeneroStrategy() {
        Libro libro = new Libro();
        libro.setGenero("Ciencia Ficción");
        when(libroRepository.searchByGenero("Ciencia")).thenReturn(List.of(libro));

        LibroSearchContext context = new LibroSearchContext(porGenero);
        List<Libro> resultado = context.ejecutar("Ciencia", libroRepository);

        assertEquals(1, resultado.size());
        assertEquals("Ciencia Ficción", resultado.get(0).getGenero());
        verify(libroRepository, times(1)).searchByGenero("Ciencia");
    }

    @Test
    @DisplayName("BuscarPorAutorStrategy debe invocar searchByAutor en el repositorio")
    void testBuscarPorAutorStrategy() {
        Libro libro = new Libro();
        Autor autor = new Autor();
        autor.setNombre("Miguel");
        autor.setApellido("de Cervantes");
        libro.setAutores(List.of(autor));

        when(libroRepository.searchByAutor("Cervantes")).thenReturn(List.of(libro));

        LibroSearchContext context = new LibroSearchContext(porAutor);
        List<Libro> resultado = context.ejecutar("Cervantes", libroRepository);

        assertEquals(1, resultado.size());
        verify(libroRepository, times(1)).searchByAutor("Cervantes");
    }

    @Test
    @DisplayName("LibroSearchContext debe permitir cambiar de estrategia en tiempo de ejecución")
    void testCambioDinamicoDeEstrategia() {
        LibroSearchContext context = new LibroSearchContext(porTitulo);
        assertEquals("titulo", context.getStrategy().getNombre());

        context.setStrategy(porGenero);
        assertEquals("genero", context.getStrategy().getNombre());
    }

    @Test
    @DisplayName("LibroSearchStrategyFactory debe resolver las estrategias por su clave")
    void testStrategyFactoryResolucion() {
        assertSame(porTitulo, factory.getStrategy("titulo"));
        assertSame(porGenero, factory.getStrategy("genero"));
        assertSame(porAutor, factory.getStrategy("autor"));

        assertThrows(IllegalArgumentException.class, () -> factory.getStrategy("inexistente"));
        assertThrows(IllegalArgumentException.class, () -> factory.getStrategy(null));
    }
}

