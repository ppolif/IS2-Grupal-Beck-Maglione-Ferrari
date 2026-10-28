package com.example.persona.services;

import com.example.persona.dtos.LibroDto;
import com.example.persona.entities.Libro;
import com.example.persona.repositories.BaseRepository;
import com.example.persona.repositories.LibroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LibroServiceImpl extends BaseServiceImpl<Libro, LibroDto, Long> implements LibroService {

    @Autowired
    private LibroRepository libroRepository;

    public LibroServiceImpl(BaseRepository<Libro, Long> baseRepository) {
        super(baseRepository, Libro.class, LibroDto.class);
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
    public List<LibroDto> findDisponibles() throws Exception {
        try {
            List<Libro> libros = libroRepository.findDisponibles();
            return libros.stream().map(this::toDto).toList();
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }
}
