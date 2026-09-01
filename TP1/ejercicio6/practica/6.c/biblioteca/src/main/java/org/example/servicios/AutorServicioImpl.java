package org.example.servicios;

import org.example.dtos.AutorRequestDTO;
import org.example.dtos.AutorResponseDTO;
import org.example.entidades.Autor;
import org.example.errores.RecursoNoEncontradoException;
import org.example.repositorios.AutorRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AutorServicioImpl implements AutorServicio {

    private final AutorRepositorio autorRepositorio;

    public AutorServicioImpl(AutorRepositorio autorRepositorio) {
        this.autorRepositorio = autorRepositorio;
    }

    @Override
    @Transactional
    public AutorResponseDTO crear(AutorRequestDTO autorDTO) {
        Autor autor = Autor.builder()
                .nombre(autorDTO.getNombre())
                .alta(true) // Regla de negocio: nace dado de alta
                .build();

        autor = autorRepositorio.save(autor);
        return mapearADTO(autor);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AutorResponseDTO> obtenerTodosActivos() {
        return autorRepositorio.findByAltaTrue().stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AutorResponseDTO obtenerPorId(String id) {
        Autor autor = buscarAutorActivo(id);
        return mapearADTO(autor);
    }

    @Override
    @Transactional
    public AutorResponseDTO actualizar(String id, AutorRequestDTO autorDTO) {
        Autor autor = buscarAutorActivo(id);
        autor.setNombre(autorDTO.getNombre());
        autor = autorRepositorio.save(autor);
        return mapearADTO(autor);
    }

    @Override
    @Transactional
    public void darDeBaja(String id) {
        Autor autor = buscarAutorActivo(id);
        autor.setAlta(false); // Baja lógica
        autorRepositorio.save(autor);
    }

    // Métodos utilitarios privados
    private Autor buscarAutorActivo(String id) {
        return autorRepositorio.findByIdAndAltaTrue(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró un autor activo con el ID: " + id));
    }

    private AutorResponseDTO mapearADTO(Autor autor) {
        return AutorResponseDTO.builder()
                .id(autor.getId())
                .nombre(autor.getNombre())
                .alta(autor.isAlta())
                .build();
    }
}