package org.example.controladores;


import org.example.dtos.EditorialRequestDTO;
import org.example.dtos.EditorialResponseDTO;
import org.example.servicios.EditorialServicio;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/editoriales")
public class EditorialControlador {

    private final EditorialServicio editorialServicio;

    public EditorialControlador(EditorialServicio editorialServicio) {
        this.editorialServicio = editorialServicio;
    }

    @PostMapping
    public ResponseEntity<EditorialResponseDTO> crear(@Valid @RequestBody EditorialRequestDTO editorialDTO) {
        EditorialResponseDTO nuevaEditorial = editorialServicio.crear(editorialDTO);
        return new ResponseEntity<>(nuevaEditorial, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<EditorialResponseDTO>> obtenerTodasActivas() {
        return ResponseEntity.ok(editorialServicio.obtenerTodasActivas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EditorialResponseDTO> obtenerPorId(@PathVariable String id) {
        return ResponseEntity.ok(editorialServicio.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EditorialResponseDTO> actualizar(
            @PathVariable String id,
            @Valid @RequestBody EditorialRequestDTO editorialDTO) {
        return ResponseEntity.ok(editorialServicio.actualizar(id, editorialDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> darDeBaja(@PathVariable String id) {
        editorialServicio.darDeBaja(id);
        return ResponseEntity.noContent().build();
    }
}