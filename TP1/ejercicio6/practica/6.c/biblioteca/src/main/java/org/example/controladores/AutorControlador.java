package org.example.controladores;


import org.example.dtos.AutorRequestDTO;
import org.example.dtos.AutorResponseDTO;
import org.example.servicios.AutorServicio;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/autores")
public class AutorControlador {

    private final AutorServicio autorServicio;

    public AutorControlador(AutorServicio autorServicio) {
        this.autorServicio = autorServicio;
    }

    @PostMapping
    public ResponseEntity<AutorResponseDTO> crear(@Valid @RequestBody AutorRequestDTO autorDTO) {
        AutorResponseDTO nuevoAutor = autorServicio.crear(autorDTO);
        return new ResponseEntity<>(nuevoAutor, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<AutorResponseDTO>> obtenerTodosActivos() {
        return ResponseEntity.ok(autorServicio.obtenerTodosActivos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AutorResponseDTO> obtenerPorId(@PathVariable String id) {
        return ResponseEntity.ok(autorServicio.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AutorResponseDTO> actualizar(
            @PathVariable String id,
            @Valid @RequestBody AutorRequestDTO autorDTO) {
        return ResponseEntity.ok(autorServicio.actualizar(id, autorDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> darDeBaja(@PathVariable String id) {
        autorServicio.darDeBaja(id);
        return ResponseEntity.noContent().build();
    }
}