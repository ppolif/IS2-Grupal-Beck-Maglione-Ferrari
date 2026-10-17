package com.example.persona.patterns.factory;

import com.example.persona.entities.Autor;
import com.example.persona.entities.Libro;
import com.example.persona.entities.LibroDigital;
import com.example.persona.patterns.builder.LibroDigitalBuilder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class LibroDigitalFactory implements LibroFactory {

    @Override
    public Libro crearLibro() {
        return new LibroDigitalBuilder()
                .tamanoMB(5.0)
                .formato("PDF")
                .urlDescarga("https://biblioteca.uncuyo.edu.ar/descargas/libro.pdf")
                .build();
    }

    @Override
    public Libro crearLibro(String titulo, int fecha, String genero, int paginas,
                            LocalDate fechaVencimientoDevolucion, List<Autor> autores) {
        return new LibroDigitalBuilder()
                .titulo(titulo)
                .fecha(fecha)
                .genero(genero)
                .paginas(paginas)
                .fechaVencimientoDevolucion(fechaVencimientoDevolucion)
                .autores(autores)
                .tamanoMB(5.0)
                .formato("PDF")
                .urlDescarga("https://biblioteca.uncuyo.edu.ar/descargas/libro.pdf")
                .build();
    }

    public LibroDigital crearLibroDigital(String titulo, int fecha, String genero, int paginas,
                                          LocalDate fechaVencimientoDevolucion, List<Autor> autores,
                                          Double tamanoMB, String formato, String urlDescarga) {
        return new LibroDigitalBuilder()
                .titulo(titulo)
                .fecha(fecha)
                .genero(genero)
                .paginas(paginas)
                .fechaVencimientoDevolucion(fechaVencimientoDevolucion)
                .autores(autores)
                .tamanoMB(tamanoMB)
                .formato(formato)
                .urlDescarga(urlDescarga)
                .build();
    }
}

