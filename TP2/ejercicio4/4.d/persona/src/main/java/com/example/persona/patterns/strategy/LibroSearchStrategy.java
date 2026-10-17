package com.example.persona.patterns.strategy;

import com.example.persona.entities.Libro;
import com.example.persona.repositories.LibroRepository;

import java.util.List;

public interface LibroSearchStrategy {
    List<Libro> buscar(String criterio, LibroRepository libroRepository);
    String getNombre();
}

