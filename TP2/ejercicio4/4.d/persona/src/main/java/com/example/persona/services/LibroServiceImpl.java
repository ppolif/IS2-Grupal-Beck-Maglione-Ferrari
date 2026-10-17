package com.example.persona.services;

import com.example.persona.dtos.LibroDto;
import com.example.persona.entities.Libro;
import com.example.persona.mappers.LibroMapper;
import com.example.persona.patterns.iterator.LibroCollectionImpl;
import com.example.persona.patterns.iterator.LibroIterator;
import com.example.persona.patterns.strategy.LibroSearchContext;
import com.example.persona.patterns.strategy.LibroSearchStrategy;
import com.example.persona.patterns.strategy.LibroSearchStrategyFactory;
import com.example.persona.repositories.BaseRepository;
import com.example.persona.repositories.LibroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class LibroServiceImpl extends BaseServiceImpl<Libro, LibroDto, Long> implements LibroService {

    private final LibroRepository libroRepository;
    private final LibroSearchStrategyFactory strategyFactory;

    @Autowired
    public LibroServiceImpl(LibroRepository libroRepository, LibroMapper libroMapper, LibroSearchStrategyFactory strategyFactory) {
        super(libroRepository, libroMapper);
        this.libroRepository = libroRepository;
        this.strategyFactory = strategyFactory;
    }

    @Override
    public List<LibroDto> search(String filtro) throws Exception {
        try {
            List<Libro> libros = libroRepository.search(filtro);
            return libros.stream().map(this::toDto).toList();
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public Page<LibroDto> search(String filtro, Pageable pageable) throws Exception {
        try {
            Page<Libro> libros = libroRepository.search(filtro, pageable);
            return libros.map(this::toDto);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public List<LibroDto> searchByStrategy(String tipoEstrategia, String criterio) throws Exception {
        try {
            LibroSearchStrategy strategy = strategyFactory.getStrategy(tipoEstrategia);
            LibroSearchContext context = new LibroSearchContext(strategy);
            List<Libro> libros = context.ejecutar(criterio, libroRepository);
            return libros.stream().map(this::toDto).toList();
        } catch (Exception e) {
            throw new Exception("Error en búsqueda por estrategia: " + e.getMessage());
        }
    }

    @Override
    public List<LibroDto> filtrarLibrosPorAutorConIterador(String autor) throws Exception {
        try {
            List<Libro> todosLosLibros = libroRepository.findAll();
            LibroCollectionImpl coleccion = new LibroCollectionImpl(todosLosLibros);
            LibroIterator iterator = coleccion.iteratorPorAutor(autor);

            List<Libro> librosDelAutor = new ArrayList<>();
            while (iterator.hasNext()) {
                librosDelAutor.add(iterator.next());
            }
            return librosDelAutor.stream().map(this::toDto).toList();
        } catch (Exception e) {
            throw new Exception("Error al filtrar libros por autor con iterador: " + e.getMessage());
        }
    }
}

