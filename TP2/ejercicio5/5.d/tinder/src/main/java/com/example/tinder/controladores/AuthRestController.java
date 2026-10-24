package com.example.tinder.controladores;

import com.example.tinder.dto.AuthResponse;
import com.example.tinder.dto.LoginRequest;
import com.example.tinder.entidades.Usuario;
import com.example.tinder.jwt.JwtService;
import com.example.tinder.repositorios.UsuarioRepositorio;
import com.example.tinder.servicios.UsuarioServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(path = {"/auth", "/api/auth"})
public class AuthRestController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UsuarioServicio usuarioServicio;

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    @Autowired
    private JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getClave())
            );

            UserDetails userDetails = usuarioServicio.loadUserByUsername(request.getEmail());
            String token = jwtService.getToken(userDetails);

            Usuario usuario = usuarioRepositorio.buscarPorMail(request.getEmail());
            String rol = (usuario != null && usuario.getRol() != null) ? usuario.getRol().name() : "USER";

            AuthResponse response = new AuthResponse(token, "Login exitoso", request.getEmail(), rol);
            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Credenciales inválidas: correo o clave incorrectos.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/check")
    public ResponseEntity<?> checkToken() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No autenticado");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("authenticated", true);
        data.put("username", auth.getName());
        data.put("authorities", auth.getAuthorities());
        return ResponseEntity.ok(data);
    }
}

