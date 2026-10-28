package com.example.persona.patterns.builder;

import com.example.persona.entities.Autor;
import com.example.persona.entities.Libro;
import com.example.persona.entities.LibroDigital;
import com.example.persona.entities.LibroFisico;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LibroBuilderTest {

    @Test
    @DisplayName("Debe construir un Libro con todos los atributos correctamente")
    void testBuildLibroCompleto() {
        Autor autor = new Autor();
        autor.setNombre("Jorge Luis");
        autor.setApellido("Borges");

        LocalDate fechaVencimiento = LocalDate.of(2026, 12, 31);

        Libro libro = Libro.builder()
                .id(1L)
                .titulo("Ficciones")
                .fecha(1944)
                .genero("Cuentos")
                .paginas(220)
                .fechaVencimientoDevolucion(fechaVencimiento)
                .autor(autor)
                .build();

        assertNotNull(libro);
        assertEquals(1L, libro.getId());
        assertEquals("Ficciones", libro.getTitulo());
        assertEquals(1944, libro.getFecha());
        assertEquals("Cuentos", libro.getGenero());
        assertEquals(220, libro.getPaginas());
        assertEquals(fechaVencimiento, libro.getFechaVencimientoDevolucion());
        assertEquals(1, libro.getAutores().size());
        assertEquals("Jorge Luis", libro.getAutores().get(0).getNombre());
    }

    @Test
    @DisplayName("Debe construir un LibroFisico con sus atributos especializados")
    void testBuildLibroFisico() {
        LibroFisico libroFisico = new LibroFisicoBuilder()
                .titulo("El Aleph")
                .fecha(1949)
                .genero("Ficción")
                .paginas(180)
                .numeroEstante("E-12")
                .pesoGramos(450)
                .build();

        assertNotNull(libroFisico);
        assertEquals("El Aleph", libroFisico.getTitulo());
        assertEquals("E-12", libroFisico.getNumeroEstante());
        assertEquals(450, libroFisico.getPesoGramos());
    }

    @Test
    @DisplayName("Debe construir un LibroDigital con sus atributos especializados")
    void testBuildLibroDigital() {
        LibroDigital libroDigital = new LibroDigitalBuilder()
                .titulo("Rayuela")
                .fecha(1963)
                .genero("Novela")
                .paginas(600)
                .tamanoMB(4.8)
                .formato("EPUB")
                .urlDescarga("https://biblioteca.org/rayuela.epub")
                .build();

        assertNotNull(libroDigital);
        assertEquals("Rayuela", libroDigital.getTitulo());
        assertEquals(4.8, libroDigital.getTamanoMB());
        assertEquals("EPUB", libroDigital.getFormato());
        assertEquals("https://biblioteca.org/rayuela.epub", libroDigital.getUrlDescarga());
    }

    @Test
    @DisplayName("Debe reiniciar los atributos tras llamar a reset()")
    void testBuilderReset() {
        LibroBuilder builder = new LibroBuilder();
        builder.titulo("Temporal").paginas(100);
        builder.reset();

        Libro libro = builder.build();
        assertNull(libro.getTitulo());
        assertEquals(0, libro.getPaginas());
        assertTrue(libro.getAutores().isEmpty());
    }
}

