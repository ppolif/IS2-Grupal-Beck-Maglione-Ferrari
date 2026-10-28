package com.example.consultorio.rest;

import com.example.consultorio.entities.FotoPaciente;
import com.example.consultorio.entities.Paciente;
import com.example.consultorio.service.FotoService;
import com.example.consultorio.service.PacienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/fotos")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class FotoRestController {

    @Autowired
    private FotoService fotoService;

    @Autowired
    private PacienteService pacienteService;

    @PostMapping(value = "/paciente/{pacienteId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> subirFoto(@PathVariable String pacienteId,
                                       @RequestParam("archivo") MultipartFile archivo) {
        try {
            Paciente paciente = pacienteService.buscarPorId(pacienteId);
            if (paciente == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Paciente no encontrado"));
            }
            FotoPaciente foto = fotoService.asignarNuevaFoto(paciente, archivo);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "id", foto.getId(),
                    "nombre", foto.getNombre(),
                    "mime", foto.getMime(),
                    "url", "/foto/paciente/" + pacienteId
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/paciente/{pacienteId}")
    public ResponseEntity<?> obtenerInfoFoto(@PathVariable String pacienteId) {
        Optional<FotoPaciente> fotoOpt = fotoService.obtenerFotoActivaDePaciente(pacienteId);
        if (fotoOpt.isPresent()) {
            FotoPaciente f = fotoOpt.get();
            return ResponseEntity.ok(Map.of(
                    "id", f.getId(),
                    "nombre", f.getNombre(),
                    "mime", f.getMime(),
                    "url", "/foto/paciente/" + pacienteId
            ));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "El paciente no tiene foto activa"));
    }

    @DeleteMapping("/{fotoId}")
    public ResponseEntity<?> eliminarFoto(@PathVariable String fotoId) {
        try {
            fotoService.eliminarImagen(fotoId);
            return ResponseEntity.ok(Map.of("mensaje", "Foto eliminada"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}

