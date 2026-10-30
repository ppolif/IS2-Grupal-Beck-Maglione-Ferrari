package com.DulceMayor.backend.controllers.persona;

import com.DulceMayor.backend.config.UsuarioPrincipal;
import com.DulceMayor.backend.dtos.persona.ActualizarPerfilMinoristaRequest;
import com.DulceMayor.backend.dtos.persona.MinoristaPage;
import com.DulceMayor.backend.dtos.persona.MinoristaRequest;
import com.DulceMayor.backend.dtos.persona.MinoristaResponse;
import com.DulceMayor.backend.services.persona.MinoristaService;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
 * Controlador REST para la gestión de clientes minoristas.
 */
@RestController
@RequestMapping
public class MinoristaController {

    private final MinoristaService minoristaService;

    public MinoristaController(MinoristaService minoristaService) {
        this.minoristaService = minoristaService;
    }

    @GetMapping({"/minoristas", "/api/v1/minoristas"})
    @PreAuthorize("hasAnyRole('JEFE', 'ADMINISTRATIVO', 'PREVENTISTA')")
    public ResponseEntity<MinoristaPage> listarMinorista(
            @RequestParam(required = false) String numeroDocumento,
            @RequestParam(required = false) String q,
            Pageable pageable) {
        return ResponseEntity.ok(minoristaService.buscarPaginado(numeroDocumento, q, pageable));
    }

    @PostMapping({"/minoristas", "/api/v1/minoristas"})
    @PreAuthorize("hasAnyRole('JEFE', 'ADMINISTRATIVO', 'PREVENTISTA')")
    public ResponseEntity<MinoristaResponse> crearMinorista(@Valid @RequestBody MinoristaRequest request) {
        MinoristaResponse creado = minoristaService.crear(request);
        return ResponseEntity.created(URI.create("/minoristas/" + creado.id())).body(creado);
    }

    @GetMapping({"/minoristas/{id}", "/api/v1/minoristas/{id}"})
    @PreAuthorize("hasAnyRole('JEFE', 'ADMINISTRATIVO', 'PREVENTISTA')")
    public ResponseEntity<MinoristaResponse> buscarMinorista(@PathVariable String id) {
        return ResponseEntity.ok(minoristaService.buscar(id));
    }

    @PutMapping({"/minoristas/{id}", "/api/v1/minoristas/{id}"})
    @PreAuthorize("hasAnyRole('JEFE', 'ADMINISTRATIVO', 'PREVENTISTA')")
    public ResponseEntity<MinoristaResponse> modificarMinorista(
            @PathVariable String id,
            @Valid @RequestBody MinoristaRequest request) {
        return ResponseEntity.ok(minoristaService.modificar(id, request));
    }

    @DeleteMapping({"/minoristas/{id}", "/api/v1/minoristas/{id}"})
    @PreAuthorize("hasAnyRole('JEFE', 'ADMINISTRATIVO')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarMinorista(@PathVariable String id) {
        minoristaService.eliminar(id);
    }

    @GetMapping({"/mi-cuenta/perfil", "/api/v1/mi-cuenta/perfil"})
    @PreAuthorize("hasRole('MINORISTA')")
    public ResponseEntity<MinoristaResponse> obtenerMiPerfil(
            @AuthenticationPrincipal UsuarioPrincipal principal) {
        return ResponseEntity.ok(minoristaService.obtenerMiPerfil(principal.getId()));
    }

    @PutMapping({"/mi-cuenta/perfil", "/api/v1/mi-cuenta/perfil"})
    @PreAuthorize("hasRole('MINORISTA')")
    public ResponseEntity<MinoristaResponse> actualizarMiPerfil(
            @AuthenticationPrincipal UsuarioPrincipal principal,
            @Valid @RequestBody ActualizarPerfilMinoristaRequest request) {
        return ResponseEntity.ok(minoristaService.actualizarMiPerfil(principal.getId(), request));
    }

    @DeleteMapping({"/mi-cuenta/perfil", "/api/v1/mi-cuenta/perfil"})
    @PreAuthorize("hasRole('MINORISTA')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarMiCuenta(
            @AuthenticationPrincipal UsuarioPrincipal principal) {
        minoristaService.eliminarMiCuenta(principal.getId());
    }
}

