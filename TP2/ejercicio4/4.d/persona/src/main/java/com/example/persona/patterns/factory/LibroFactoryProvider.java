package com.example.persona.patterns.factory;

import com.example.persona.entities.Autor;
import com.example.persona.entities.Libro;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class LibroFactoryProvider {

    private final LibroFisicoFactory fisicoFactory;
    private final LibroDigitalFactory digitalFactory;

    public LibroFactoryProvider(LibroFisicoFactory fisicoFactory, LibroDigitalFactory digitalFactory) {
        this.fisicoFactory = fisicoFactory;
        this.digitalFactory = digitalFactory;
    }

    public LibroFactory getFactory(TipoLibro tipo) {
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de libro no puede ser nulo.");
        }
        return switch (tipo) {
            case FISICO -> fisicoFactory;
            case DIGITAL -> digitalFactory;
        };
    }

    public Libro crear(TipoLibro tipo) {
        return getFactory(tipo).crearLibro();
    }

    public Libro crear(TipoLibro tipo, String titulo, int fecha, String genero, int paginas,
                       LocalDate fechaVencimientoDevolucion, List<Autor> autores) {
        return getFactory(tipo).crearLibro(titulo, fecha, genero, paginas, fechaVencimientoDevolucion, autores);
    }
}

