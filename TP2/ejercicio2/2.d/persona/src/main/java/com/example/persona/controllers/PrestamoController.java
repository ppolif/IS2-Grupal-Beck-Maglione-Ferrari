package com.example.persona.controllers;

import com.example.persona.dtos.AltaPrestamoDto;
import com.example.persona.dtos.PrestamoDto;
import com.example.persona.services.PrestamoServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(path = "api/v1/prestamos")
public class PrestamoController extends BaseControllerImpl<PrestamoDto, PrestamoServiceImpl> {

    @PostMapping("/alta")
    public ResponseEntity<?> darDeAlta(@RequestBody AltaPrestamoDto altaDto) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(servicio.darDeAlta(altaDto));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    @PostMapping("/{id}/devolver")
    public ResponseEntity<?> devolver(@PathVariable("id") Long id) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(servicio.devolver(id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/activos")
    public ResponseEntity<?> getActivos() {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(servicio.findActivos());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
}

