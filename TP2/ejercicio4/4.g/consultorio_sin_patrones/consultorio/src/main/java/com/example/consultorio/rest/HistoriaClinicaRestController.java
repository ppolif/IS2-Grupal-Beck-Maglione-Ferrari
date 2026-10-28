package com.example.consultorio.rest;

import com.example.consultorio.entities.DetalleHistoriaClinica;
import com.example.consultorio.entities.HistoriaClinica;
import com.example.consultorio.entities.Medico;
import com.example.consultorio.entities.Paciente;
import com.example.consultorio.repositories.UsuarioRepository;
import com.example.consultorio.service.HistoriaClinicaService;
import com.example.consultorio.service.MedicoService;
import com.example.consultorio.service.PacienteService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/historias")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class HistoriaClinicaRestController {

    @Autowired
    private HistoriaClinicaService historiaClinicaService;

    @Autowired
    private PacienteService pacienteService;

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
            List<HistoriaClinica> lista = historiaClinicaService.listarHistoriasActivas();
            return ResponseEntity.ok(lista);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable String id) {
        try {
            HistoriaClinica historia = historiaClinicaService.obtenerHistoriaPorId(id);
            return ResponseEntity.ok(historia);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/paciente/{pacienteId}")
    public ResponseEntity<?> obtenerPorPaciente(@PathVariable String pacienteId) {
        try {
            Paciente paciente = pacienteService.buscarPorId(pacienteId);
            if (paciente == null) {
                paciente = pacienteService.buscarPorDni(pacienteId);
            }
            if (paciente == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Paciente no encontrado"));
            }
            HistoriaClinica historia = historiaClinicaService.obtenerHistoriaDelPaciente(paciente);
            return ResponseEntity.ok(historia);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> registrar(@RequestBody Map<String, String> data) {
        try {
            asegurarSesion();
            String dniPaciente = data.get("dniPaciente");
            String medicoId = data.get("medicoId");
            String detalleHistoria = data.get("detalleHistoria");

            if (dniPaciente == null || dniPaciente.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "El DNI del paciente es obligatorio"));
            }
            if (medicoId == null || medicoId.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "El ID del médico es obligatorio"));
            }

            Medico medico = medicoService.buscarPorId(medicoId);
            if (medico == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Médico no encontrado"));
            }

            DetalleHistoriaClinica detalle = new DetalleHistoriaClinica();
            detalle.setDetalleHistoria(detalleHistoria);
            detalle.setMedico(medico);

            historiaClinicaService.ejecutarRegistro(dniPaciente, detalle);

            // Devolver la historia recién creada
            Paciente paciente = pacienteService.buscarPorDni(dniPaciente);
            HistoriaClinica creada = historiaClinicaService.obtenerHistoriaDelPaciente(paciente);
            return ResponseEntity.status(HttpStatus.CREATED).body(creada);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/detalles")
    public ResponseEntity<?> agregarDetalle(@PathVariable String id, @RequestBody Map<String, String> data) {
        try {
            asegurarSesion();
            String medicoId = data.get("medicoId");
            String detalleTexto = data.get("detalleHistoria");

            HistoriaClinica historia = historiaClinicaService.obtenerHistoriaPorId(id);
            Medico medico = medicoService.buscarPorId(medicoId);
            if (medico == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Médico no encontrado"));
            }

            DetalleHistoriaClinica nuevoDetalle = new DetalleHistoriaClinica();
            nuevoDetalle.setDetalleHistoria(detalleTexto);
            nuevoDetalle.setMedico(medico);

            historiaClinicaService.ejecutarAgregarDetalle(historia.getPaciente().getDni(), nuevoDetalle);

            HistoriaClinica actualizada = historiaClinicaService.obtenerHistoriaPorId(id);
            return ResponseEntity.ok(actualizada);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}/detalles/{detalleId}")
    public ResponseEntity<?> eliminarDetalle(@PathVariable String id, @PathVariable String detalleId) {
        try {
            asegurarSesion();
            historiaClinicaService.ejecutarBajaDetalle(id, detalleId);
            HistoriaClinica actualizada = historiaClinicaService.obtenerHistoriaPorId(id);
            return ResponseEntity.ok(actualizada);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
