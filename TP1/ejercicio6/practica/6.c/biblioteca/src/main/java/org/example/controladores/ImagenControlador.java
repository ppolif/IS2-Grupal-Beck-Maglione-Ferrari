package org.example.controladores;


import org.example.dtos.ImagenResponseDTO;
import org.example.entidades.Imagen;
import org.example.servicios.ImagenServicio;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/imagenes")
public class ImagenControlador {

    private final ImagenServicio imagenServicio;

    public ImagenControlador(ImagenServicio imagenServicio) {
        this.imagenServicio = imagenServicio;
    }

    // Usamos consume "multipart/form-data" para subir archivos físicos
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImagenResponseDTO> cargarImagen(@RequestParam("archivo") MultipartFile archivo) {
        ImagenResponseDTO nuevaImagen = imagenServicio.guardar(archivo);
        return new ResponseEntity<>(nuevaImagen, HttpStatus.CREATED);
    }

    // Retorna JSON con ID, Nombre y Mime
    @GetMapping("/{id}")
    public ResponseEntity<ImagenResponseDTO> obtenerMetadatos(@PathVariable String id) {
        return ResponseEntity.ok(imagenServicio.obtenerMetadatosPorId(id));
    }

    // Retorna el byte[] renderizable en el navegador
    @GetMapping("/{id}/contenido")
    public ResponseEntity<byte[]> obtenerContenidoImagen(@PathVariable String id) {
        Imagen imagen = imagenServicio.obtenerEntidadPorId(id);

        HttpHeaders headers = new HttpHeaders();
        // Seteamos el Content-Type para que el cliente sepa que es una imagen (ej. image/jpeg o image/png)
        headers.setContentType(MediaType.parseMediaType(imagen.getMime()));

        return new ResponseEntity<>(imagen.getContenido(), headers, HttpStatus.OK);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImagenResponseDTO> actualizarImagen(
            @PathVariable String id,
            @RequestParam("archivo") MultipartFile archivo) {
        return ResponseEntity.ok(imagenServicio.actualizar(id, archivo));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarFisicamente(@PathVariable String id) {
        imagenServicio.eliminarFisicamente(id);
        return ResponseEntity.noContent().build();
    }
}