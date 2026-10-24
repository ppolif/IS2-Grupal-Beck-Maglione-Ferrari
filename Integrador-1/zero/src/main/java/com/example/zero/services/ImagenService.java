package com.example.zero.services;

import com.example.zero.entidades.Imagen;
import com.example.zero.enums.TipoImagen;
import com.example.zero.repositories.ImagenRepository;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

@Service
public class ImagenService {

    private final ImagenRepository imagenRepository;

    public ImagenService(ImagenRepository imagenRepository) {
        this.imagenRepository = imagenRepository;
    }

    @Transactional
    public Imagen guardarImagen(MultipartFile archivo, TipoImagen tipo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("El archivo de imagen no puede estar vacío");
        }
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de imagen no puede ser nulo");
        }

        try {
            String mime = archivo.getContentType();
            if (mime == null || mime.trim().isEmpty()) {
                mime = "image/jpeg";
            }

            String nombre = archivo.getOriginalFilename();
            if (nombre == null || nombre.trim().isEmpty()) {
                nombre = "imagen";
            }

            Imagen imagen = Imagen.builder()
                    .nombre(nombre)
                    .mime(mime)
                    .contenido(archivo.getBytes())
                    .tipoImagen(tipo)
                    .eliminado(false)
                    .build();

            return imagenRepository.save(imagen);
        } catch (IOException e) {
            throw new RuntimeException("Error al procesar el archivo de imagen: " + e.getMessage(), e);
        }
    }

    @Transactional
    public Imagen guardarMonigoteDefault() {
        byte[] contenido = obtenerBytesMonigoteDefault();
        Imagen monigote = Imagen.builder()
                .nombre("avatar.png")
                .mime("image/png")
                .contenido(contenido)
                .tipoImagen(TipoImagen.PERSONA)
                .eliminado(false)
                .build();

        return imagenRepository.save(monigote);
    }

    public byte[] obtenerBytesMonigoteDefault() {
        try {
            ClassPathResource resource = new ClassPathResource("static/admin/assets/images/avatar.png");
            if (resource.exists()) {
                try (InputStream is = resource.getInputStream()) {
                    return is.readAllBytes();
                }
            }
        } catch (IOException ignored) {
        }
        // Fallback mínimo de 1x1 PNG si no se pudiera cargar el recurso
        return new byte[]{
                (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
                0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52,
                0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01,
                0x08, 0x06, 0x00, 0x00, 0x00, 0x1F, 0x15, (byte) 0xC4,
                (byte) 0x89, 0x00, 0x00, 0x00, 0x0A, 0x49, 0x44, 0x41,
                0x54, 0x78, (byte) 0x9C, 0x63, 0x00, 0x01, 0x00, 0x00,
                0x05, 0x00, 0x01, 0x0D, 0x0A, 0x2D, (byte) 0xB4, 0x00,
                0x00, 0x00, 0x00, 0x49, 0x45, 0x4E, 0x44, (byte) 0xAE,
                0x42, 0x60, (byte) 0x82
        };
    }

    @Transactional(readOnly = true)
    public Imagen buscarPorId(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID de la imagen no puede ser nulo o vacío");
        }
        return imagenRepository.findByIdAndEliminadoFalse(id)
                .or(() -> imagenRepository.findActive(id))
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la imagen con ID: " + id));
    }

    @Transactional
    public void eliminarImagen(String id) {
        Imagen imagen = buscarPorId(id);
        imagen.setEliminado(true);
        imagenRepository.save(imagen);
    }
}
