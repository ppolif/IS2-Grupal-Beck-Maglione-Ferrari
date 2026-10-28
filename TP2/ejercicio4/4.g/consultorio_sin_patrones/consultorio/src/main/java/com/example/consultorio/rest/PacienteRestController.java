package com.example.consultorio.rest;

import com.example.consultorio.entities.Paciente;
import com.example.consultorio.entities.Usuario;
import com.example.consultorio.repositories.UsuarioRepository;
import com.example.consultorio.service.FotoService;
import com.example.consultorio.service.PacienteService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pacientes")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class PacienteRestController {

    @Autowired
    private PacienteService pacienteService;

    @Autowired
    private FotoService fotoService;

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
            List<Paciente> lista = pacienteService.listarActivos();
            return ResponseEntity.ok(lista);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable String id) {
        Paciente paciente = pacienteService.buscarPorId(id);
        if (paciente == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Paciente no encontrado"));
        }
        return ResponseEntity.ok(paciente);
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Paciente paciente) {
        try {
            asegurarSesion();
            paciente.setId(null);
            pacienteService.ejecutarAlta(paciente);
            return ResponseEntity.status(HttpStatus.CREATED).body(paciente);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping(value = "/con-foto", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> crearConFoto(@RequestParam("nombre") String nombre,
                                         @RequestParam("apellido") String apellido,
                                         @RequestParam("dni") String dni,
                                         @RequestParam(value = "archivoFoto", required = false) MultipartFile archivoFoto) {
        try {
            asegurarSesion();
            Paciente paciente = new Paciente();
            paciente.setNombre(nombre);
            paciente.setApellido(apellido);
            paciente.setDni(dni);
            pacienteService.ejecutarAlta(paciente);

            if (archivoFoto != null && !archivoFoto.isEmpty()) {
                fotoService.asignarNuevaFoto(paciente, archivoFoto);
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(paciente);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable String id, @RequestBody Paciente paciente) {
        try {
            asegurarSesion();
            paciente.setId(id);
            pacienteService.ejecutarModificacion(paciente);
            return ResponseEntity.ok(paciente);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable String id) {
        try {
            asegurarSesion();
            pacienteService.ejecutarBaja(id);
            return ResponseEntity.ok(Map.of("mensaje", "Paciente dado de baja correctamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
