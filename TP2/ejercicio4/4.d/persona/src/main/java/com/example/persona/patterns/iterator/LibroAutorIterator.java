package com.example.persona.patterns.iterator;

import com.example.persona.entities.Autor;
import com.example.persona.entities.Libro;

import java.util.List;
import java.util.NoSuchElementException;

public class LibroAutorIterator implements LibroIterator {

    private final List<Libro> libros;
    private final String nombreAutor;
    private int posicion = 0;

    public LibroAutorIterator(List<Libro> libros, String nombreAutor) {
        this.libros = libros;
        this.nombreAutor = nombreAutor != null ? nombreAutor.trim().toLowerCase() : "";
    }

    @Override
    public boolean hasNext() {
        if (libros == null || nombreAutor.isEmpty()) {
            return false;
        }
        while (posicion < libros.size()) {
            Libro candidato = libros.get(posicion);
            if (perteneceAlAutor(candidato)) {
                return true;
            }
            posicion++;
        }
        return false;
    }

    @Override
    public Libro next() {
        if (!hasNext()) {
            throw new NoSuchElementException("No hay más libros del autor: " + nombreAutor);
        }
        return libros.get(posicion++);
    }

    private boolean perteneceAlAutor(Libro libro) {
        if (libro == null || libro.getAutores() == null) {
            return false;
        }
        for (Autor autor : libro.getAutores()) {
            if (autor != null) {
                String nombre = autor.getNombre() != null ? autor.getNombre().toLowerCase() : "";
                String apellido = autor.getApellido() != null ? autor.getApellido().toLowerCase() : "";
                if (nombre.contains(nombreAutor) || apellido.contains(nombreAutor)) {
                    return true;
                }
            }
        }
        return false;
    }
}

