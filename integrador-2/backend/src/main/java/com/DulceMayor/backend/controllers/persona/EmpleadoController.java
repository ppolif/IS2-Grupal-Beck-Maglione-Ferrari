package com.DulceMayor.backend.controllers.persona;

import com.DulceMayor.backend.dtos.persona.EmpleadoPage;
import com.DulceMayor.backend.dtos.persona.EmpleadoRequest;
import com.DulceMayor.backend.dtos.persona.EmpleadoResponse;
import com.DulceMayor.backend.services.persona.EmpleadoService;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para la administración de empleados (Jefes, Administrativos, Preventores y Distribuidores).
 */
@RestController
@RequestMapping
public class EmpleadoController {

    private final EmpleadoService empleadoService;

    public EmpleadoController(EmpleadoService empleadoService) {
        this.empleadoService = empleadoService;
    }

    @GetMapping({"/empleados", "/api/v1/empleados"})
    @PreAuthorize("hasAnyRole('JEFE', 'ADMINISTRATIVO')")
    public ResponseEntity<EmpleadoPage> listarEmpleado(
            @RequestParam(required = false) String rol,
            @RequestParam(required = false) String legajo,
            @RequestParam(required = false) String q,
            Pageable pageable) {
        return ResponseEntity.ok(empleadoService.buscarPaginado(rol, legajo, q, pageable));
    }

    @PostMapping({"/empleados", "/api/v1/empleados"})
    @PreAuthorize("hasRole('JEFE')")
    public ResponseEntity<EmpleadoResponse> crearEmpleado(@Valid @RequestBody EmpleadoRequest request) {
        EmpleadoResponse creado = empleadoService.crear(request);
        return ResponseEntity.created(URI.create("/empleados/" + creado.id())).body(creado);
    }

    @GetMapping({"/empleados/{id}", "/api/v1/empleados/{id}"})
    @PreAuthorize("hasAnyRole('JEFE', 'ADMINISTRATIVO')")
    public ResponseEntity<EmpleadoResponse> buscarEmpleado(@PathVariable String id) {
        return ResponseEntity.ok(empleadoService.buscar(id));
    }

    @PutMapping({"/empleados/{id}", "/api/v1/empleados/{id}"})
    @PreAuthorize("hasRole('JEFE')")
    public ResponseEntity<EmpleadoResponse> modificarEmpleado(
            @PathVariable String id,
            @Valid @RequestBody EmpleadoRequest request) {
        return ResponseEntity.ok(empleadoService.modificar(id, request));
    }

    @DeleteMapping({"/empleados/{id}", "/api/v1/empleados/{id}"})
    @PreAuthorize("hasRole('JEFE')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarEmpleado(@PathVariable String id) {
        empleadoService.eliminar(id);
    }
}

