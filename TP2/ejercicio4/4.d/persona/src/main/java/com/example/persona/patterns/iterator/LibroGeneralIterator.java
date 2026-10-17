package com.example.persona.patterns.iterator;

import com.example.persona.entities.Libro;

import java.util.List;
import java.util.NoSuchElementException;

public class LibroGeneralIterator implements LibroIterator {
    private final List<Libro> libros;
    private int posicion = 0;

    public LibroGeneralIterator(List<Libro> libros) {
        this.libros = libros;
    }

    @Override
    public boolean hasNext() {
        return libros != null && posicion < libros.size();
    }

    @Override
    public Libro next() {
        if (!hasNext()) {
            throw new NoSuchElementException("No hay más libros disponibles.");
        }
        return libros.get(posicion++);
    }
}

