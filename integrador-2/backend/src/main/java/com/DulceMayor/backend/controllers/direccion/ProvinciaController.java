package com.DulceMayor.backend.controllers.direccion;

import com.DulceMayor.backend.controllers.BaseController;
import com.DulceMayor.backend.dtos.direccion.ProvinciaRequest;
import com.DulceMayor.backend.dtos.direccion.ProvinciaResponse;
import com.DulceMayor.backend.services.BaseService;
import com.DulceMayor.backend.services.direccion.ProvinciaService;
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
 * Controlador REST para la consulta y administración de provincias.
 * Aplica el patrón Template Method heredando de {@link BaseController}.
 */
@RestController
@RequestMapping({"/provincias", "/api/v1/provincias"})
public class ProvinciaController extends BaseController<ProvinciaService, ProvinciaRequest, ProvinciaResponse> {

    public ProvinciaController(ProvinciaService provinciaService) {
        super(provinciaService);
    }

    @GetMapping
    public ResponseEntity<List<ProvinciaResponse>> listarProvincia(
            @RequestParam(required = false) String paisId) {
        if (paisId == null || paisId.isBlank()) {
            return ejecutarListar();
        }
        return ResponseEntity.ok(service.listar(paisId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('JEFE', 'ADMINISTRATIVO')")
    public ResponseEntity<ProvinciaResponse> crearProvincia(@Valid @RequestBody ProvinciaRequest request) {
        return ejecutarCrear(request);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProvinciaResponse> buscarProvincia(@PathVariable String id) {
        return ejecutarBuscar(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('JEFE', 'ADMINISTRATIVO')")
    public ResponseEntity<ProvinciaResponse> modificarProvincia(
            @PathVariable String id,
            @Valid @RequestBody ProvinciaRequest request) {
        return ejecutarModificar(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('JEFE', 'ADMINISTRATIVO')")
    public ResponseEntity<Void> eliminarProvincia(@PathVariable String id) {
        return ejecutarEliminar(id);
    }
}

