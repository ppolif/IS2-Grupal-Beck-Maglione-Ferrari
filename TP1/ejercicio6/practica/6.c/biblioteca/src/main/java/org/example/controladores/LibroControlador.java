package org.example.controladores;


import org.example.dtos.LibroRequestDTO;
import org.example.dtos.LibroResponseDTO;
import org.example.servicios.LibroServicio;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/libros")
public class LibroControlador {

    private final LibroServicio libroServicio;

    public LibroControlador(LibroServicio libroServicio) {
        this.libroServicio = libroServicio;
    }

    @PostMapping
    public ResponseEntity<LibroResponseDTO> crear(@Valid @RequestBody LibroRequestDTO libroDTO) {
        LibroResponseDTO nuevoLibro = libroServicio.crear(libroDTO);
        return new ResponseEntity<>(nuevoLibro, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<LibroResponseDTO>> obtenerTodosActivos() {
        return ResponseEntity.ok(libroServicio.obtenerTodosActivos());
    }

    @GetMapping("/{isbn}")
    public ResponseEntity<LibroResponseDTO> obtenerPorIsbn(@PathVariable Long isbn) {
        return ResponseEntity.ok(libroServicio.obtenerPorIsbn(isbn));
    }

    @PutMapping("/{isbn}")
    public ResponseEntity<LibroResponseDTO> actualizar(
            @PathVariable Long isbn,
            @Valid @RequestBody LibroRequestDTO libroDTO) {
        return ResponseEntity.ok(libroServicio.actualizar(isbn, libroDTO));
    }

    @DeleteMapping("/{isbn}")
    public ResponseEntity<Void> darDeBaja(@PathVariable Long isbn) {
        libroServicio.darDeBaja(isbn);
        return ResponseEntity.noContent().build();
    }
}
