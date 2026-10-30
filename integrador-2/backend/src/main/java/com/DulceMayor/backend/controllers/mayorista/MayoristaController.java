package com.DulceMayor.backend.controllers.mayorista;

import com.DulceMayor.backend.dtos.mayorista.ConfiguracionCorreoMayoristaRequest;
import com.DulceMayor.backend.dtos.mayorista.ConfiguracionCorreoMayoristaResponse;
import com.DulceMayor.backend.dtos.mayorista.MayoristaRequest;
import com.DulceMayor.backend.dtos.mayorista.MayoristaResponse;
import com.DulceMayor.backend.services.mayorista.ConfiguracionCorreoMayoristaService;
import com.DulceMayor.backend.services.mayorista.MayoristaService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para la gestión de la empresa mayorista, sus sucursales públicas
 * y la configuración SMTP saliente.
 */
@RestController
@RequestMapping
public class MayoristaController {

    private final MayoristaService mayoristaService;
    private final ConfiguracionCorreoMayoristaService configuracionCorreoMayoristaService;

    public MayoristaController(
            MayoristaService mayoristaService,
            ConfiguracionCorreoMayoristaService configuracionCorreoMayoristaService) {
        this.mayoristaService = mayoristaService;
        this.configuracionCorreoMayoristaService = configuracionCorreoMayoristaService;
    }

    @GetMapping({"/mayoristas", "/api/v1/mayoristas"})
    @PreAuthorize("hasAnyRole('JEFE', 'ADMINISTRATIVO')")
    public ResponseEntity<List<MayoristaResponse>> listarMayorista() {
        return ResponseEntity.ok(mayoristaService.listar());
    }

    @PostMapping({"/mayoristas", "/api/v1/mayoristas"})
    @PreAuthorize("hasRole('JEFE')")
    public ResponseEntity<MayoristaResponse> crearMayorista(@Valid @RequestBody MayoristaRequest request) {
        MayoristaResponse creado = mayoristaService.crear(request);
        return ResponseEntity.created(URI.create("/mayoristas/" + creado.id())).body(creado);
    }

    @GetMapping({"/mayoristas/{id}", "/api/v1/mayoristas/{id}"})
    @PreAuthorize("hasAnyRole('JEFE', 'ADMINISTRATIVO')")
    public ResponseEntity<MayoristaResponse> buscarMayorista(@PathVariable String id) {
        return ResponseEntity.ok(mayoristaService.buscar(id));
    }

    @PutMapping({"/mayoristas/{id}", "/api/v1/mayoristas/{id}"})
    @PreAuthorize("hasRole('JEFE')")
    public ResponseEntity<MayoristaResponse> modificarMayorista(
            @PathVariable String id,
            @Valid @RequestBody MayoristaRequest request) {
        return ResponseEntity.ok(mayoristaService.modificar(id, request));
    }

    @DeleteMapping({"/mayoristas/{id}", "/api/v1/mayoristas/{id}"})
    @PreAuthorize("hasRole('JEFE')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarMayorista(@PathVariable String id) {
        mayoristaService.eliminar(id);
    }

    @GetMapping({"/mayoristas/{id}/configuracion-correo", "/api/v1/mayoristas/{id}/configuracion-correo"})
    @PreAuthorize("hasRole('JEFE')")
    public ResponseEntity<ConfiguracionCorreoMayoristaResponse> buscarConfiguracionCorreoMayoristaPorMayorista(
            @PathVariable String id) {
        return ResponseEntity.ok(configuracionCorreoMayoristaService.buscarPorMayorista(id));
    }

    @PutMapping({"/mayoristas/{id}/configuracion-correo", "/api/v1/mayoristas/{id}/configuracion-correo"})
    @PreAuthorize("hasRole('JEFE')")
    public ResponseEntity<ConfiguracionCorreoMayoristaResponse> modificarConfiguracionCorreoMayorista(
            @PathVariable String id,
            @Valid @RequestBody ConfiguracionCorreoMayoristaRequest request) {
        return ResponseEntity.ok(configuracionCorreoMayoristaService.modificarPorMayorista(id, request));
    }

    @GetMapping({"/publico/sucursales", "/api/v1/publico/sucursales"})
    public ResponseEntity<List<MayoristaResponse>> listarSucursalesPublicas() {
        return ResponseEntity.ok(mayoristaService.listarPublicoSucursales());
    }
}

