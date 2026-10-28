package com.example.carrito.servicios;

import com.example.carrito.entidades.Imagen;
import com.example.carrito.excepciones.ErrorServicio;
import com.example.carrito.repositorios.ImagenRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class ImagenServicio {

    @Autowired
    private ImagenRepositorio imagenRepositorio;

    @Transactional
    public Imagen guardar(MultipartFile archivo) throws ErrorServicio {
        if (archivo == null || archivo.isEmpty()) {
            return null;
        }

        try {
            Imagen imagen = Imagen.builder()
                    .nombre(archivo.getOriginalFilename() != null ? archivo.getOriginalFilename() : "imagen")
                    .mime(archivo.getContentType() != null ? archivo.getContentType() : "application/octet-stream")
                    .contenido(archivo.getBytes())
                    .eliminado(false)
                    .build();

            return imagenRepositorio.save(imagen);
        } catch (IOException e) {
            throw new ErrorServicio("Error al procesar el archivo de imagen: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public Imagen buscarPorId(String id) throws ErrorServicio {
        return imagenRepositorio.findByIdAndEliminadoFalse(id)
                .orElseThrow(() -> new ErrorServicio("No se encontró la imagen solicitada"));
    }
}

