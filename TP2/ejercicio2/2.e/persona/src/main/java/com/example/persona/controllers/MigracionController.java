package com.example.persona.controllers;

import com.example.persona.dtos.MigracionResultadoDto;
import com.example.persona.services.MigracionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(path = "api/v1/migracion")
public class MigracionController {

    @Autowired
    private MigracionService migracionService;

    @PostMapping("/ejecutar")
    public ResponseEntity<?> ejecutarMigracion(@RequestParam(value = "ruta", required = false) String ruta) {
        try {
            MigracionResultadoDto resultado = migracionService.migrarDesdeArchivo(ruta);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping(value = "/subir", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> subirYMigrar(@RequestParam("archivo") MultipartFile archivo) {
        try {
            MigracionResultadoDto resultado = migracionService.migrarDesdeArchivoSubido(archivo);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
