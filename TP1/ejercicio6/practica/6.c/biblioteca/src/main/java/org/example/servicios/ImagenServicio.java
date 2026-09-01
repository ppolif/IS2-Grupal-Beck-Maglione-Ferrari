package org.example.servicios;

import org.example.dtos.ImagenResponseDTO;
import org.example.entidades.Imagen;
import org.springframework.web.multipart.MultipartFile;

public interface ImagenServicio {
    ImagenResponseDTO guardar(MultipartFile archivo);
    Imagen obtenerEntidadPorId(String id); // Devuelve la entidad completa para poder extraer los bytes
    ImagenResponseDTO obtenerMetadatosPorId(String id);
    ImagenResponseDTO actualizar(String id, MultipartFile archivo);
    void eliminarFisicamente(String id);
}