package org.example.servicios;

import org.example.dtos.AutorRequestDTO;
import org.example.dtos.AutorResponseDTO;
import java.util.List;

public interface AutorServicio {
    AutorResponseDTO crear(AutorRequestDTO autorDTO);
    List<AutorResponseDTO> obtenerTodosActivos();
    AutorResponseDTO obtenerPorId(String id);
    AutorResponseDTO actualizar(String id, AutorRequestDTO autorDTO);
    void darDeBaja(String id);
}