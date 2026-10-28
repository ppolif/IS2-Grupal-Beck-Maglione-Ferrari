package com.example.consultorio.rest;

import com.example.consultorio.entities.Medico;
import com.example.consultorio.repositories.UsuarioRepository;
import com.example.consultorio.service.MedicoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/medicos")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class MedicoRestController {

    @Autowired
    private MedicoService medicoService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private HttpSession session;

    private void asegurarSesion() {
        if (session.getAttribute("usuariosession") == null) {
            usuarioRepository.findByNombreUsuarioAndEliminadoFalse("admin")
                    .ifPresent(u -> session.setAttribute("usuariosession", u));
        }
    }

    @GetMapping
    public ResponseEntity<?> listar() {
        try {
            List<Medico> lista = medicoService.listarActivos();
            return ResponseEntity.ok(lista);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable String id) {
        Medico medico = medicoService.buscarPorId(id);
        if (medico == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Médico no encontrado"));
        }
        return ResponseEntity.ok(medico);
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Medico medico) {
        try {
            asegurarSesion();
            medico.setId(null);
            medicoService.ejecutarAlta(medico);
            return ResponseEntity.status(HttpStatus.CREATED).body(medico);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable String id, @RequestBody Medico medico) {
        try {
            asegurarSesion();
            medico.setId(id);
            medicoService.ejecutarModificacion(medico);
            return ResponseEntity.ok(medico);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable String id) {
        try {
            asegurarSesion();
            medicoService.ejecutarBaja(id);
            return ResponseEntity.ok(Map.of("mensaje", "Médico dado de baja correctamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
