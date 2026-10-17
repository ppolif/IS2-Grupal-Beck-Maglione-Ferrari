package com.example.persona.patterns.iterator;

public interface LibroCollection {
    LibroIterator iterator();
    LibroIterator iteratorPorAutor(String autor);
}

