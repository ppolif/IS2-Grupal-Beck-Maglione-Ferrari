package org.example.servicios;


import org.example.dtos.EditorialRequestDTO;
import org.example.dtos.EditorialResponseDTO;
import java.util.List;

public interface EditorialServicio {
    EditorialResponseDTO crear(EditorialRequestDTO editorialDTO);
    List<EditorialResponseDTO> obtenerTodasActivas();
    EditorialResponseDTO obtenerPorId(String id);
    EditorialResponseDTO actualizar(String id, EditorialRequestDTO editorialDTO);
    void darDeBaja(String id);
}