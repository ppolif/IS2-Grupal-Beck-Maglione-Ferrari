package com.DulceMayor.backend.controllers;

import com.DulceMayor.backend.config.Auth0TokenValidatorService;
import com.DulceMayor.backend.config.UsuarioPrincipal;
import com.DulceMayor.backend.dtos.AuthResponse;
import com.DulceMayor.backend.dtos.CambiarClaveRequest;
import com.DulceMayor.backend.dtos.LoginRequest;
import com.DulceMayor.backend.dtos.PerfilResponse;
import com.DulceMayor.backend.dtos.SocialLoginRequest;
import com.DulceMayor.backend.dtos.persona.RegistroMinoristaRequest;
import com.DulceMayor.backend.services.UsuarioService;
import com.DulceMayor.backend.services.persona.MinoristaService;
import com.DulceMayor.backend.services.persona.PersonaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST de autenticación y auto-registro público.
 * Expone únicamente DTOs (Patrón DTO) y delega toda lógica de negocio a los servicios de dominio.
 */
@RestController
@RequestMapping({"/auth", "/api/v1/auth"})
public class AuthController {

    private final UsuarioService usuarioService;
    private final PersonaService personaService;
    private final MinoristaService minoristaService;
    private final Auth0TokenValidatorService auth0TokenValidatorService;

    public AuthController(
            UsuarioService usuarioService,
            PersonaService personaService,
            MinoristaService minoristaService,
            Auth0TokenValidatorService auth0TokenValidatorService) {
        this.usuarioService = usuarioService;
        this.personaService = personaService;
        this.minoristaService = minoristaService;
        this.auth0TokenValidatorService = auth0TokenValidatorService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(usuarioService.login(request));
    }

    @PostMapping("/social")
    public ResponseEntity<AuthResponse> loginSocial(@Valid @RequestBody SocialLoginRequest request) {
        Auth0TokenValidatorService.Auth0UserInfo userInfo = auth0TokenValidatorService.validarYExtraerUsuario(request.token());
        AuthResponse response = minoristaService.autenticarORegistrarSocial(
                userInfo.email(),
                userInfo.nombre(),
                userInfo.apellido()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/registro")
    public ResponseEntity<AuthResponse> registrarMinorista(@Valid @RequestBody RegistroMinoristaRequest request) {
        AuthResponse response = minoristaService.registrarEnPortal(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
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
        return ResponseEntity.ok(personaService.obtenerPerfilPorUsuario(principal.getId()));
    }
}
