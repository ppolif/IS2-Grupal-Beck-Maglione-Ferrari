package com.example.persona.patterns.iterator;

import com.example.persona.entities.Libro;

import java.util.Collections;
import java.util.List;

public class LibroCollectionImpl implements LibroCollection {

    private final List<Libro> libros;

    public LibroCollectionImpl(List<Libro> libros) {
        this.libros = libros != null ? libros : Collections.emptyList();
    }

    @Override
    public LibroIterator iterator() {
        return new LibroGeneralIterator(libros);
    }

    @Override
    public LibroIterator iteratorPorAutor(String autor) {
        return new LibroAutorIterator(libros, autor);
    }

    public List<Libro> getLibros() {
        return libros;
    }
}

