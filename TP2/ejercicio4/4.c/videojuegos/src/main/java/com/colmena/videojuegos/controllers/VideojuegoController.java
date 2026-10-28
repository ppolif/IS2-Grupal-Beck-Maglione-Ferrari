package com.colmena.videojuegos.controllers;

import com.colmena.videojuegos.entities.Videojuego;
import com.colmena.videojuegos.services.FileStorageService;
import com.colmena.videojuegos.services.ServicioVideojuego;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/v1/videojuegos")
public class VideojuegoController extends BaseControllerImpl<Videojuego, ServicioVideojuego> {

    @Autowired
    private FileStorageService fileStorageService;

    @GetMapping("/search")
    public ResponseEntity<?> searchByTitle(@RequestParam("query") String query) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(servicio.findByTitle(query));
        } catch (Exception e) {
            return buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/activos")
    public ResponseEntity<?> getActivos() {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(servicio.findAllByActivo());
        } catch (Exception e) {
            return buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/activos/{id}")
    public ResponseEntity<?> getActivoById(@PathVariable("id") Long id) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(servicio.findByIdAndActivo(id));
        } catch (Exception e) {
            return buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PostMapping("/{id}/imagen")
    public ResponseEntity<?> uploadImagen(@PathVariable("id") Long id, @RequestParam("archivo") MultipartFile archivo) {
        try {
            if (archivo == null || archivo.isEmpty()) {
                return buildErrorResponse(HttpStatus.BAD_REQUEST, "El archivo de imagen no puede estar vacío");
            }
            if (!fileStorageService.validarExtension(archivo)) {
                return buildErrorResponse(HttpStatus.BAD_REQUEST, "Formato de archivo no válido como imagen");
            }
            if (archivo.getSize() >= 15000000) {
                return buildErrorResponse(HttpStatus.BAD_REQUEST, "El peso de la imagen excede 15MB");
            }

            Videojuego videojuego = servicio.findById(id);
            if (videojuego == null) {
                return buildErrorResponse(HttpStatus.NOT_FOUND, "No existe el videojuego con ID " + id);
            }

            String nombreFoto = fileStorageService.guardarArchivo(archivo, videojuego.getImagen());
            videojuego.setImagen(nombreFoto);
            Videojuego updated = servicio.updateOne(videojuego, id);
            return ResponseEntity.status(HttpStatus.OK).body(updated);
        } catch (Exception e) {
            return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
}
