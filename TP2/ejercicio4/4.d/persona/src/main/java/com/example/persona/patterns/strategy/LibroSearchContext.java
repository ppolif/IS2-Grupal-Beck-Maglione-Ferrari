package com.example.persona.patterns.strategy;

import com.example.persona.entities.Libro;
import com.example.persona.repositories.LibroRepository;

import java.util.List;

public class LibroSearchContext {
    private LibroSearchStrategy strategy;

    public LibroSearchContext() {
    }

    public LibroSearchContext(LibroSearchStrategy strategy) {
        this.strategy = strategy;
    }

    public void setStrategy(LibroSearchStrategy strategy) {
        this.strategy = strategy;
    }

    public LibroSearchStrategy getStrategy() {
        return this.strategy;
    }

    public List<Libro> ejecutar(String criterio, LibroRepository repository) {
        if (strategy == null) {
            throw new IllegalStateException("No se ha configurado ninguna estrategia de búsqueda.");
        }
        return strategy.buscar(criterio, repository);
    }
}

