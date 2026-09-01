package org.example.servicios;


import org.example.dtos.LibroRequestDTO;
import org.example.dtos.LibroResponseDTO;
import java.util.List;

public interface LibroServicio {
    LibroResponseDTO crear(LibroRequestDTO libroDTO);
    List<LibroResponseDTO> obtenerTodosActivos();
    LibroResponseDTO obtenerPorIsbn(Long isbn);
    LibroResponseDTO actualizar(Long isbn, LibroRequestDTO libroDTO);
    void darDeBaja(Long isbn);
}
