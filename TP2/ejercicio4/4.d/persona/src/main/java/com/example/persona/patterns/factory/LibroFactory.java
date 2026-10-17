package com.example.persona.patterns.factory;

import com.example.persona.entities.Autor;
import com.example.persona.entities.Libro;

import java.time.LocalDate;
import java.util.List;

public interface LibroFactory {
    Libro crearLibro();
    Libro crearLibro(String titulo, int fecha, String genero, int paginas, LocalDate fechaVencimientoDevolucion, List<Autor> autores);
}

