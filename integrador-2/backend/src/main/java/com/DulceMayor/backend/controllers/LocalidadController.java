package com.DulceMayor.backend.controllers;

import com.DulceMayor.backend.dtos.LocalidadRequest;
import com.DulceMayor.backend.dtos.LocalidadResponse;
import com.DulceMayor.backend.services.BaseService;
import com.DulceMayor.backend.services.LocalidadService;
import jakarta.validation.Valid;
import java.util.List;
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
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para la consulta y administración de localidades.
 * Aplica el patrón Template Method heredando de {@link BaseController}.
 */
@RestController
@RequestMapping({"/localidades", "/api/v1/localidades"})
public class LocalidadController extends BaseController<LocalidadRequest, LocalidadResponse> {

    private final LocalidadService localidadService;

    public LocalidadController(LocalidadService localidadService) {
        this.localidadService = localidadService;
    }

    @Override
    protected BaseService<?, LocalidadRequest, LocalidadResponse> getService() {
        return localidadService;
    }

    @Override
    protected String extraerId(LocalidadResponse response) {
        return response.id();
    }

    @GetMapping
    public ResponseEntity<List<LocalidadResponse>> listarLocalidad(
            @RequestParam(required = false) String departamentoId) {
        if (departamentoId == null || departamentoId.isBlank()) {
            return ejecutarListar();
        }
        return ResponseEntity.ok(localidadService.listar(departamentoId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('JEFE', 'ADMINISTRATIVO')")
    public ResponseEntity<LocalidadResponse> crearLocalidad(@Valid @RequestBody LocalidadRequest request) {
        return ejecutarCrear(request);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LocalidadResponse> buscarLocalidad(@PathVariable String id) {
        return ejecutarBuscar(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('JEFE', 'ADMINISTRATIVO')")
    public ResponseEntity<LocalidadResponse> modificarLocalidad(
            @PathVariable String id,
            @Valid @RequestBody LocalidadRequest request) {
        return ejecutarModificar(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('JEFE', 'ADMINISTRATIVO')")
    public ResponseEntity<Void> eliminarLocalidad(@PathVariable String id) {
        return ejecutarEliminar(id);
    }
}

