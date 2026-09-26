package com.example.zero.controllers;

import com.example.zero.entidades.Imagen;
import com.example.zero.services.ImagenService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ImagenController {

    private final ImagenService imagenService;

    public ImagenController(ImagenService imagenService) {
        this.imagenService = imagenService;
    }

    @GetMapping({"/imagen/{id}", "/imagenes/{id}"})
    public ResponseEntity<byte[]> obtenerImagen(@PathVariable("id") String id) {
        try {
            Imagen imagen = imagenService.buscarPorId(id);
            if (imagen.getContenido() == null || imagen.getContenido().length == 0) {
                return ResponseEntity.notFound().build();
            }

            MediaType mediaType;
            try {
                mediaType = MediaType.parseMediaType(imagen.getMime() != null ? imagen.getMime() : "image/jpeg");
            } catch (Exception e) {
                mediaType = MediaType.IMAGE_JPEG;
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(mediaType);
            return new ResponseEntity<>(imagen.getContenido(), headers, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
