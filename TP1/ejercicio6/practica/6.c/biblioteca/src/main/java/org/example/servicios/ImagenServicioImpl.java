package org.example.servicios;


import org.example.dtos.ImagenResponseDTO;
import org.example.entidades.Imagen;
import org.example.errores.ErrorArchivoException;
import org.example.errores.RecursoNoEncontradoException;
import org.example.repositorios.ImagenRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class ImagenServicioImpl implements ImagenServicio {

    private final ImagenRepositorio imagenRepositorio;

    public ImagenServicioImpl(ImagenRepositorio imagenRepositorio) {
        this.imagenRepositorio = imagenRepositorio;
    }

    @Override
    @Transactional
    public ImagenResponseDTO guardar(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("El archivo no puede ser nulo ni estar vacío");
        }

        try {
            Imagen imagen = Imagen.builder()
                    .nombre(archivo.getOriginalFilename())
                    .mime(archivo.getContentType())
                    .contenido(archivo.getBytes())
                    .build();

            imagen = imagenRepositorio.save(imagen);
            return mapearADTO(imagen);

        } catch (IOException e) {
            throw new ErrorArchivoException("Error al procesar el archivo de imagen", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Imagen obtenerEntidadPorId(String id) {
        return imagenRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la imagen con el ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public ImagenResponseDTO obtenerMetadatosPorId(String id) {
        Imagen imagen = obtenerEntidadPorId(id);
        return mapearADTO(imagen);
    }

    @Override
    @Transactional
    public ImagenResponseDTO actualizar(String id, MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("El archivo no puede ser nulo ni estar vacío");
        }

        try {
            Imagen imagen = obtenerEntidadPorId(id);
            imagen.setNombre(archivo.getOriginalFilename());
            imagen.setMime(archivo.getContentType());
            imagen.setContenido(archivo.getBytes());

            imagen = imagenRepositorio.save(imagen);
            return mapearADTO(imagen);

        } catch (IOException e) {
            throw new ErrorArchivoException("Error al procesar el archivo de imagen para actualizar", e);
        }
    }

    @Override
    @Transactional
    public void eliminarFisicamente(String id) {
        Imagen imagen = obtenerEntidadPorId(id);
        imagenRepositorio.delete(imagen);
    }

    private ImagenResponseDTO mapearADTO(Imagen imagen) {
        return ImagenResponseDTO.builder()
                .id(imagen.getId())
                .nombre(imagen.getNombre())
                .mime(imagen.getMime())
                .build();
    }
}