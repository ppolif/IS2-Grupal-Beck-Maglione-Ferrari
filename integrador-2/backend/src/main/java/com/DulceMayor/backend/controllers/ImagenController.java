package com.DulceMayor.backend.controllers;

import com.DulceMayor.backend.dtos.ImagenContenido;
import com.DulceMayor.backend.dtos.ImagenResponse;
import com.DulceMayor.backend.services.ImagenService;
import java.net.URI;
import java.util.concurrent.TimeUnit;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * Controlador REST para la carga, descarga y administración de archivos multimedia e imágenes.
 * Este controlador no extiende del controlador base ya que maneja archivos binarios y no JSON, por lo que se requiere un manejo especial de las respuestas HTTP.
 */
@RestController
@RequestMapping({"/imagenes", "/api/v1/imagenes"})
public class ImagenController {

    private final ImagenService imagenService;

    public ImagenController(ImagenService imagenService) {
        this.imagenService = imagenService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('JEFE', 'ADMINISTRATIVO')")
    public ResponseEntity<ImagenResponse> crearImagen(
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam("tipoImagen") String tipoImagen) {
        ImagenResponse response = imagenService.subirImagen(archivo, tipoImagen);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{id}/contenido")
    public ResponseEntity<byte[]> buscarImagen(@PathVariable String id) {
        ImagenContenido contenido = imagenService.obtenerContenido(id);
        MediaType mediaType;
        try {
            mediaType = MediaType.parseMediaType(contenido.mime());
        } catch (Exception e) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .cacheControl(CacheControl.maxAge(1, TimeUnit.DAYS).cachePublic())
                .body(contenido.contenido());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('JEFE', 'ADMINISTRATIVO')")
    public ResponseEntity<Void> eliminarImagen(@PathVariable String id) {
        imagenService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
