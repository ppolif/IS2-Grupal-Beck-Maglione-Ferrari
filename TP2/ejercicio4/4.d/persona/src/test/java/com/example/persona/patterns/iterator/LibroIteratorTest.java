package com.example.persona.patterns.iterator;

import com.example.persona.entities.Autor;
import com.example.persona.entities.Libro;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class LibroIteratorTest {

    private List<Libro> catalogo;
    private Autor borges;
    private Autor cortazar;
    private Autor bioy;

    @BeforeEach
    void setUp() {
        borges = new Autor("Jorge Luis", "Borges", "Biografía Borges");
        cortazar = new Autor("Julio", "Cortázar", "Biografía Cortázar");
        bioy = new Autor("Adolfo", "Bioy Casares", "Biografía Bioy");

        Libro l1 = new Libro();
        l1.setTitulo("Ficciones");
        l1.setAutores(List.of(borges));

        Libro l2 = new Libro();
        l2.setTitulo("Rayuela");
        l2.setAutores(List.of(cortazar));

        Libro l3 = new Libro();
        l3.setTitulo("Un modelo para la muerte");
        l3.setAutores(Arrays.asList(borges, bioy)); // Co-autoría

        Libro l4 = new Libro();
        l4.setTitulo("Bestiario");
        l4.setAutores(List.of(cortazar));

        catalogo = Arrays.asList(l1, l2, l3, l4);
    }

    @Test
    @DisplayName("LibroAutorIterator debe iterar y devolver únicamente los libros del autor solicitado")
    void testIterarPorAutor() {
        LibroCollection coleccion = new LibroCollectionImpl(catalogo);
        LibroIterator it = coleccion.iteratorPorAutor("Borges");

        List<String> titulosEncontrados = new ArrayList<>();
        while (it.hasNext()) {
            Libro libro = it.next();
            titulosEncontrados.add(libro.getTitulo());
        }

        assertEquals(2, titulosEncontrados.size());
        assertTrue(titulosEncontrados.contains("Ficciones"));
        assertTrue(titulosEncontrados.contains("Un modelo para la muerte"));
    }

    @Test
    @DisplayName("LibroAutorIterator debe ser insensible a mayúsculas y soportar búsqueda por nombre o apellido")
    void testIterarInsensibleMayusculas() {
        LibroCollection coleccion = new LibroCollectionImpl(catalogo);
        LibroIterator it = coleccion.iteratorPorAutor("julio");

        List<String> titulosEncontrados = new ArrayList<>();
        while (it.hasNext()) {
            titulosEncontrados.add(it.next().getTitulo());
        }

        assertEquals(2, titulosEncontrados.size());
        assertTrue(titulosEncontrados.contains("Rayuela"));
        assertTrue(titulosEncontrados.contains("Bestiario"));
    }

    @Test
    @DisplayName("hasNext() debe retornar false y next() lanzar excepción si el autor no tiene libros")
    void testAutorSinLibros() {
        LibroCollection coleccion = new LibroCollectionImpl(catalogo);
        LibroIterator it = coleccion.iteratorPorAutor("Shakespeare");

        assertFalse(it.hasNext());
        assertThrows(NoSuchElementException.class, it::next);
    }

    @Test
    @DisplayName("LibroGeneralIterator debe recorrer todos los libros en orden secuencial")
    void testIteradorGeneral() {
        LibroCollection coleccion = new LibroCollectionImpl(catalogo);
        LibroIterator it = coleccion.iterator();

        int count = 0;
        while (it.hasNext()) {
            Libro libro = it.next();
            assertNotNull(libro);
            count++;
        }
        assertEquals(4, count);
        assertThrows(NoSuchElementException.class, it::next);
    }
}

