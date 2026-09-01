package org.example.controladores;


import org.example.dtos.PrestamoRequestDTO;
import org.example.dtos.PrestamoResponseDTO;
import org.example.servicios.PrestamoServicio;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/prestamos")
public class PrestamoControlador {


    private final PrestamoServicio prestamoServicio;

    public PrestamoControlador(PrestamoServicio prestamoServicio) {
        this.prestamoServicio = prestamoServicio;
    }

    // Endpoint para registrar un nuevo préstamo (POST)
    @PostMapping
    public ResponseEntity<PrestamoResponseDTO> registrarPrestamo(@Valid @RequestBody PrestamoRequestDTO dto) {
        PrestamoResponseDTO nuevoPrestamo = prestamoServicio.registrarPrestamo(dto);
        return new ResponseEntity<>(nuevoPrestamo, HttpStatus.CREATED);
    }

    // Endpoint para registrar la devolución de un préstamo existente
    @PatchMapping("/{id}/devolucion")
    public ResponseEntity<PrestamoResponseDTO> registrarDevolucion(@PathVariable String id) {
        return ResponseEntity.ok(prestamoServicio.registrarDevolucion(id));
    }

    // Endpoint para ver todo el historial de préstamos
    @GetMapping
    public ResponseEntity<List<PrestamoResponseDTO>> obtenerTodos() {
        return ResponseEntity.ok(prestamoServicio.obtenerTodosActivos());
    }

    // Endpoint para ver los libros que un usuario aún no devolvió
    @GetMapping("/usuario/{idUsuario}/activos")
    public ResponseEntity<List<PrestamoResponseDTO>> obtenerActivosDeUsuario(@PathVariable String idUsuario) {
        return ResponseEntity.ok(prestamoServicio.obtenerPrestamosActivosDeUsuario(idUsuario));
    }
}
