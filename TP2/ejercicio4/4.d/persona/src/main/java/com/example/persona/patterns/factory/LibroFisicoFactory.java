package com.example.persona.patterns.factory;

import com.example.persona.entities.Autor;
import com.example.persona.entities.Libro;
import com.example.persona.entities.LibroFisico;
import com.example.persona.patterns.builder.LibroFisicoBuilder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class LibroFisicoFactory implements LibroFactory {

    @Override
    public Libro crearLibro() {
        return new LibroFisicoBuilder()
                .numeroEstante("A-01")
                .pesoGramos(350)
                .build();
    }

    @Override
    public Libro crearLibro(String titulo, int fecha, String genero, int paginas,
                            LocalDate fechaVencimientoDevolucion, List<Autor> autores) {
        return new LibroFisicoBuilder()
                .titulo(titulo)
                .fecha(fecha)
                .genero(genero)
                .paginas(paginas)
                .fechaVencimientoDevolucion(fechaVencimientoDevolucion)
                .autores(autores)
                .numeroEstante("A-01")
                .pesoGramos(350)
                .build();
    }

    public LibroFisico crearLibroFisico(String titulo, int fecha, String genero, int paginas,
                                        LocalDate fechaVencimientoDevolucion, List<Autor> autores,
                                        String numeroEstante, Integer pesoGramos) {
        return new LibroFisicoBuilder()
                .titulo(titulo)
                .fecha(fecha)
                .genero(genero)
                .paginas(paginas)
                .fechaVencimientoDevolucion(fechaVencimientoDevolucion)
                .autores(autores)
                .numeroEstante(numeroEstante)
                .pesoGramos(pesoGramos)
                .build();
    }
}

