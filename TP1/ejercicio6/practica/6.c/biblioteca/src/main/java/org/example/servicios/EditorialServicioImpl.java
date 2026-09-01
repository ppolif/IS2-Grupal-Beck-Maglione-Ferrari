package org.example.servicios;


import org.example.dtos.EditorialRequestDTO;
import org.example.dtos.EditorialResponseDTO;
import org.example.entidades.Editorial;
import org.example.errores.RecursoNoEncontradoException;
import org.example.repositorios.EditorialRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EditorialServicioImpl implements EditorialServicio {

    private final EditorialRepositorio editorialRepositorio;

    public EditorialServicioImpl(EditorialRepositorio editorialRepositorio) {
        this.editorialRepositorio = editorialRepositorio;
    }

    @Override
    @Transactional
    public EditorialResponseDTO crear(EditorialRequestDTO editorialDTO) {
        Editorial editorial = Editorial.builder()
                .nombre(editorialDTO.getNombre())
                .alta(true) // Regla de negocio: nace dada de alta
                .build();

        editorial = editorialRepositorio.save(editorial);
        return mapearADTO(editorial);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EditorialResponseDTO> obtenerTodasActivas() {
        return editorialRepositorio.findByAltaTrue().stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EditorialResponseDTO obtenerPorId(String id) {
        Editorial editorial = buscarEditorialActiva(id);
        return mapearADTO(editorial);
    }

    @Override
    @Transactional
    public EditorialResponseDTO actualizar(String id, EditorialRequestDTO editorialDTO) {
        Editorial editorial = buscarEditorialActiva(id);
        editorial.setNombre(editorialDTO.getNombre());
        editorial = editorialRepositorio.save(editorial);
        return mapearADTO(editorial);
    }

    @Override
    @Transactional
    public void darDeBaja(String id) {
        Editorial editorial = buscarEditorialActiva(id);
        editorial.setAlta(false); // Baja lógica
        editorialRepositorio.save(editorial);
    }

    // Métodos utilitarios privados
    private Editorial buscarEditorialActiva(String id) {
        return editorialRepositorio.findByIdAndAltaTrue(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró una editorial activa con el ID: " + id));
    }

    private EditorialResponseDTO mapearADTO(Editorial editorial) {
        return EditorialResponseDTO.builder()
                .id(editorial.getId())
                .nombre(editorial.getNombre())
                .alta(editorial.isAlta())
                .build();
    }
}