package com.example.persona.patterns.factory;

import com.example.persona.entities.Autor;
import com.example.persona.entities.Libro;
import com.example.persona.entities.LibroDigital;
import com.example.persona.entities.LibroFisico;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class LibroFactoryTest {

    private final LibroFisicoFactory fisicoFactory = new LibroFisicoFactory();
    private final LibroDigitalFactory digitalFactory = new LibroDigitalFactory();
    private final LibroFactoryProvider provider = new LibroFactoryProvider(fisicoFactory, digitalFactory);

    @Test
    @DisplayName("LibroFisicoFactory debe crear instancias de LibroFisico con valores por defecto o parametrizados")
    void testLibroFisicoFactory() {
        Libro libroDefault = fisicoFactory.crearLibro();
        assertInstanceOf(LibroFisico.class, libroDefault);
        LibroFisico fisicoDefault = (LibroFisico) libroDefault;
        assertEquals("A-01", fisicoDefault.getNumeroEstante());
        assertEquals(350, fisicoDefault.getPesoGramos());

        Autor autor = new Autor("Julio", "Cortázar", "Escritor argentino");
        Libro libroParam = fisicoFactory.crearLibro(
                "Bestiario", 1951, "Cuentos", 165,
                LocalDate.now().plusMonths(1), Collections.singletonList(autor)
        );
        assertInstanceOf(LibroFisico.class, libroParam);
        assertEquals("Bestiario", libroParam.getTitulo());
        assertEquals("A-01", ((LibroFisico) libroParam).getNumeroEstante());
    }

    @Test
    @DisplayName("LibroDigitalFactory debe crear instancias de LibroDigital con valores por defecto o parametrizados")
    void testLibroDigitalFactory() {
        Libro libroDefault = digitalFactory.crearLibro();
        assertInstanceOf(LibroDigital.class, libroDefault);
        LibroDigital digitalDefault = (LibroDigital) libroDefault;
        assertEquals(5.0, digitalDefault.getTamanoMB());
        assertEquals("PDF", digitalDefault.getFormato());

        Autor autor = new Autor("Ernesto", "Sábato", "Físico y escritor");
        LibroDigital digitalCustom = digitalFactory.crearLibroDigital(
                "El Túnel", 1948, "Novela", 140,
                LocalDate.now().plusDays(15), Collections.singletonList(autor),
                2.5, "EPUB", "https://descargas.org/el-tunel.epub"
        );
        assertNotNull(digitalCustom);
        assertEquals("El Túnel", digitalCustom.getTitulo());
        assertEquals(2.5, digitalCustom.getTamanoMB());
        assertEquals("EPUB", digitalCustom.getFormato());
    }

    @Test
    @DisplayName("LibroFactoryProvider debe despachar la fábrica correcta según el TipoLibro")
    void testLibroFactoryProvider() {
        LibroFactory factoryFisico = provider.getFactory(TipoLibro.FISICO);
        assertInstanceOf(LibroFisicoFactory.class, factoryFisico);

        LibroFactory factoryDigital = provider.getFactory(TipoLibro.DIGITAL);
        assertInstanceOf(LibroDigitalFactory.class, factoryDigital);

        Libro libroFisico = provider.crear(TipoLibro.FISICO);
        assertInstanceOf(LibroFisico.class, libroFisico);

        Libro libroDigital = provider.crear(TipoLibro.DIGITAL);
        assertInstanceOf(LibroDigital.class, libroDigital);

        assertThrows(IllegalArgumentException.class, () -> provider.getFactory(null));
    }
}

