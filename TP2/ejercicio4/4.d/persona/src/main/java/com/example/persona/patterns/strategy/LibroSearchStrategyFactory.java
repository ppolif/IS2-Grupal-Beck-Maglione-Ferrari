package com.example.persona.patterns.strategy;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class LibroSearchStrategyFactory {

    private final Map<String, LibroSearchStrategy> strategies = new HashMap<>();

    public LibroSearchStrategyFactory(BuscarPorTituloStrategy porTitulo,
                                      BuscarPorGeneroStrategy porGenero,
                                      BuscarPorAutorStrategy porAutor) {
        strategies.put("titulo", porTitulo);
        strategies.put("genero", porGenero);
        strategies.put("autor", porAutor);
    }

    public LibroSearchStrategy getStrategy(String tipo) {
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de estrategia no puede ser nulo.");
        }
        LibroSearchStrategy strategy = strategies.get(tipo.toLowerCase().trim());
        if (strategy == null) {
            throw new IllegalArgumentException("Estrategia no válida: '" + tipo + "'. Estrategias disponibles: titulo, genero, autor.");
        }
        return strategy;
    }
}

