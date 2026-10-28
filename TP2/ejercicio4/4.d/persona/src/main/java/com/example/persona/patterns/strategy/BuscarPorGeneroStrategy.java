package com.example.persona.patterns.strategy;

import com.example.persona.entities.Libro;
import com.example.persona.repositories.LibroRepository;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class BuscarPorGeneroStrategy implements LibroSearchStrategy {

    @Override
    public List<Libro> buscar(String criterio, LibroRepository libroRepository) {
        if (criterio == null || criterio.isBlank()) {
            return Collections.emptyList();
        }
        return libroRepository.searchByGenero(criterio.trim());
    }

    @Override
    public String getNombre() {
        return "genero";
    }
}

