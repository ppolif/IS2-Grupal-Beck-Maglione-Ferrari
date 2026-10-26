package com.DulceMayor.backend.controllers;

import com.DulceMayor.backend.config.UsuarioPrincipal;
import com.DulceMayor.backend.dtos.AuthResponse;
import com.DulceMayor.backend.dtos.CambiarClaveRequest;
import com.DulceMayor.backend.dtos.LoginRequest;
import com.DulceMayor.backend.dtos.PerfilResponse;
import com.DulceMayor.backend.services.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST de autenticación.
 * Expone únicamente DTOs (Patrón DTO) y delega toda lógica de negocio a UsuarioService.
 */
@RestController
@RequestMapping({"/auth", "/api/v1/auth"})
public class AuthController {

    private final UsuarioService usuarioService;

    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(usuarioService.login(request));
    }

    @PostMapping("/cambiar-clave")
    public ResponseEntity<Void> modificarClave(
            @AuthenticationPrincipal UsuarioPrincipal principal,
            @Valid @RequestBody CambiarClaveRequest request) {
        usuarioService.modificarClave(principal.getId(), request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/perfil")
    public ResponseEntity<PerfilResponse> perfil(
            @AuthenticationPrincipal UsuarioPrincipal principal) {
        return ResponseEntity.ok(usuarioService.obtenerPerfil(principal.getId()));
    }
}

