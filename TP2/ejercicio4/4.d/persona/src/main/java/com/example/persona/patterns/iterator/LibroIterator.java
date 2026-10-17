package com.example.persona.patterns.iterator;

import com.example.persona.entities.Libro;

public interface LibroIterator {
    boolean hasNext();
    Libro next();
}

