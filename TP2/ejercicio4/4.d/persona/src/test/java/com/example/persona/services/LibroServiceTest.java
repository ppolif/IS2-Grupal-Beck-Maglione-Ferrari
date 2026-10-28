package com.example.persona.services;

import com.example.persona.dtos.LibroDto;
import com.example.persona.entities.Autor;
import com.example.persona.entities.Libro;
import com.example.persona.mappers.LibroMapper;
import com.example.persona.patterns.strategy.BuscarPorAutorStrategy;
import com.example.persona.patterns.strategy.BuscarPorGeneroStrategy;
import com.example.persona.patterns.strategy.BuscarPorTituloStrategy;
import com.example.persona.patterns.strategy.LibroSearchStrategyFactory;
import com.example.persona.repositories.LibroRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LibroServiceTest {

    private LibroRepository libroRepository;
    private LibroMapper libroMapper;
    private LibroSearchStrategyFactory strategyFactory;
    private LibroServiceImpl libroService;

    @BeforeEach
    void setUp() {
        libroRepository = Mockito.mock(LibroRepository.class);
        libroMapper = Mockito.mock(LibroMapper.class);

        BuscarPorTituloStrategy porTitulo = new BuscarPorTituloStrategy();
        BuscarPorGeneroStrategy porGenero = new BuscarPorGeneroStrategy();
        BuscarPorAutorStrategy porAutor = new BuscarPorAutorStrategy();
        strategyFactory = new LibroSearchStrategyFactory(porTitulo, porGenero, porAutor);

        libroService = new LibroServiceImpl(libroRepository, libroMapper, strategyFactory);
    }

    @Test
    @DisplayName("searchByStrategy debe ejecutar la estrategia seleccionada y retornar DTOs")
    void testSearchByStrategy() throws Exception {
        Libro libro = new Libro();
        libro.setTitulo("Cien años de soledad");

        LibroDto dto = new LibroDto();
        dto.setTitulo("Cien años de soledad");

        when(libroRepository.searchByTitulo("soledad")).thenReturn(List.of(libro));
        when(libroMapper.toDto(libro)).thenReturn(dto);

        List<LibroDto> resultados = libroService.searchByStrategy("titulo", "soledad");

        assertNotNull(resultados);
        assertEquals(1, resultados.size());
        assertEquals("Cien años de soledad", resultados.get(0).getTitulo());
        verify(libroRepository, times(1)).searchByTitulo("soledad");
    }

    @Test
    @DisplayName("filtrarLibrosPorAutorConIterador debe recorrer todos los libros con LibroAutorIterator")
    void testFiltrarLibrosPorAutorConIterador() throws Exception {
        Autor borges = new Autor("Jorge", "Borges", "Biografía");
        Autor cortazar = new Autor("Julio", "Cortázar", "Biografía");

        Libro l1 = new Libro();
        l1.setTitulo("Ficciones");
        l1.setAutores(List.of(borges));

        Libro l2 = new Libro();
        l2.setTitulo("Rayuela");
        l2.setAutores(List.of(cortazar));

        LibroDto dtoBorges = new LibroDto();
        dtoBorges.setTitulo("Ficciones");

        when(libroRepository.findAll()).thenReturn(Arrays.asList(l1, l2));
        when(libroMapper.toDto(l1)).thenReturn(dtoBorges);

        List<LibroDto> resultados = libroService.filtrarLibrosPorAutorConIterador("Borges");

        assertNotNull(resultados);
        assertEquals(1, resultados.size());
        assertEquals("Ficciones", resultados.get(0).getTitulo());
    }
}
