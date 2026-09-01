package org.example.servicios;


import org.example.dtos.PrestamoRequestDTO;
import org.example.dtos.PrestamoResponseDTO;
import java.util.List;

public interface PrestamoServicio {
    PrestamoResponseDTO registrarPrestamo(PrestamoRequestDTO dto);
    PrestamoResponseDTO registrarDevolucion(String idPrestamo);
    List<PrestamoResponseDTO> obtenerTodosActivos();
    List<PrestamoResponseDTO> obtenerPrestamosActivosDeUsuario(String idUsuario);
}
