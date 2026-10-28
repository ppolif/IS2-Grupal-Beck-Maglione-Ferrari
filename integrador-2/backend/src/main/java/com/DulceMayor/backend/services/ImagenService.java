package com.DulceMayor.backend.services;

import com.DulceMayor.backend.config.RecursoNoEncontradoException;
import com.DulceMayor.backend.config.ValidacionException;
import com.DulceMayor.backend.dtos.ImagenContenido;
import com.DulceMayor.backend.dtos.ImagenResponse;
import com.DulceMayor.backend.entities.Imagen;
import com.DulceMayor.backend.entities.TipoImagen;
import com.DulceMayor.backend.repositories.ImagenRepository;
import java.io.IOException;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Servicio de negocio para la carga, descarga y administración de imágenes.
 */
@Service
public class ImagenService {

    private static final long MAX_TAMANIO_BYTES = 5L * 1024 * 1024; // 5 MB
    private static final Set<String> MIMES_PERMITIDOS = Set.of(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/webp"
    );

    private final ImagenRepository imagenRepository;

    public ImagenService(ImagenRepository imagenRepository) {
        this.imagenRepository = imagenRepository;
    }

    //si el tipo de imagen viene como string, primero parseamos y dsp subimos
    @Transactional
    public ImagenResponse subirImagen(MultipartFile archivo, String tipoImagenStr) {
        TipoImagen tipoImagen = parsearTipoImagen(tipoImagenStr);
        return subirImagen(archivo, tipoImagen);
    }

    @Transactional
    public ImagenResponse subirImagen(MultipartFile archivo, TipoImagen tipoImagen) {
        validarArchivo(archivo, tipoImagen);

        try {
            String nombreArchivo = archivo.getOriginalFilename() != null && !archivo.getOriginalFilename().isBlank()
                    ? archivo.getOriginalFilename()
                    : "archivo_sin_nombre";
            String mime = archivo.getContentType() != null ? archivo.getContentType().toLowerCase() : "image/jpeg";

            ///patron builder usando lombok
            Imagen imagen = Imagen.builder()
                    .nombre(nombreArchivo)
                    .mime(mime)
                    .contenido(archivo.getBytes())
                    .tipoImagen(tipoImagen)
                    .eliminado(false)
                    .build();

            Imagen guardada = imagenRepository.save(imagen);
            return new ImagenResponse(
                    guardada.getId(),
                    guardada.getNombre(),
                    guardada.getMime(),
                    guardada.getTipoImagen()
            );
        } catch (IOException e) {
            throw new ValidacionException("Error al leer el contenido del archivo multimedia: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public ImagenContenido obtenerContenido(String id) {
        Imagen imagen = obtenerEntidad(id);

        //es solo para mostrar la foto, asi que devolvemos un ImagenContenido (dto) que solo lleva el contenido y el mime
        return new ImagenContenido(imagen.getContenido(), imagen.getMime());
    }

    @Transactional(readOnly = true)
    public ImagenResponse buscar(String id) {
        Imagen imagen = obtenerEntidad(id);
        return new ImagenResponse(
                imagen.getId(),
                imagen.getNombre(),
                imagen.getMime(),
                imagen.getTipoImagen()
        );
    }

    @Transactional
    public void eliminar(String id) {
        Imagen imagen = obtenerEntidad(id);
        imagen.setEliminado(true);
        imagenRepository.save(imagen);
    }

    @Transactional(readOnly = true)
    public Imagen obtenerEntidad(String id) {
        return imagenRepository.findByIdAndEliminadoFalse(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Imagen no encontrada con id: " + id));
    }

    //en caso de que el tipo de la imagen imagen (producto, sucursal, logo) venga como string, acá se convierte en un enum
    private TipoImagen parsearTipoImagen(String tipoImagenStr) {
        if (tipoImagenStr == null || tipoImagenStr.isBlank()) {
            throw new ValidacionException("El tipo de imagen es obligatorio");
        }
        try {
            return TipoImagen.valueOf(tipoImagenStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidacionException("Tipo de imagen inválido: " + tipoImagenStr + ". Valores permitidos: PRODUCTO, SUCURSAL, LOGO_EMPRESA");
        }
    }

    private void validarArchivo(MultipartFile archivo, TipoImagen tipoImagen) {
        if (archivo == null || archivo.isEmpty()) {
            throw new ValidacionException("El archivo es obligatorio");
        }
        if (tipoImagen == null) {
            throw new ValidacionException("El tipo de imagen es obligatorio");
        }
        if (archivo.getSize() > MAX_TAMANIO_BYTES) {
            throw new ValidacionException("El tamaño máximo permitido para la imagen es de 5 MB");
        }
        String contentType = archivo.getContentType();
        if (contentType == null || !MIMES_PERMITIDOS.contains(contentType.toLowerCase())) {
            throw new ValidacionException("Formato de imagen no permitido. Solo se aceptan JPEG, PNG o WEBP");
        }
    }
}
