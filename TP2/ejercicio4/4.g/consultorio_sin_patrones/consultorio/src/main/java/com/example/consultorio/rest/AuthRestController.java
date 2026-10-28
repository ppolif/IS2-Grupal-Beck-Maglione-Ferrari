package com.example.consultorio.rest;

import com.example.consultorio.entities.Usuario;
import com.example.consultorio.repositories.UsuarioRepository;
import com.example.consultorio.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class AuthRestController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private HttpSession session;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String nombreUsuario = credentials.get("nombreUsuario");
        String clave = credentials.get("clave");

        if (nombreUsuario == null || clave == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Faltan credenciales"));
        }

        Optional<Usuario> usuarioOpt = usuarioRepository.findByNombreUsuarioAndEliminadoFalse(nombreUsuario);
        if (usuarioOpt.isPresent() && passwordEncoder.matches(clave, usuarioOpt.get().getClave())) {
            Usuario usuario = usuarioOpt.get();
            session.setAttribute("usuariosession", usuario);
            return ResponseEntity.ok(Map.of(
                    "id", usuario.getId(),
                    "nombreUsuario", usuario.getNombreUsuario()
            ));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Usuario o contraseña incorrectos"));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> data) {
        String nombreUsuario = data.get("nombreUsuario");
        String clave = data.get("clave");

        try {
            Usuario nuevo = usuarioService.registrarUsuario(nombreUsuario, clave);
            session.setAttribute("usuariosession", nuevo);
            return ResponseEntity.ok(Map.of(
                    "id", nuevo.getId(),
                    "nombreUsuario", nuevo.getNombreUsuario()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        session.removeAttribute("usuariosession");
        try {
            session.invalidate();
        } catch (Exception ignored) {}
        return ResponseEntity.ok(Map.of("mensaje", "Sesión cerrada"));
    }

    @GetMapping("/me")
    public ResponseEntity<?> currentUser() {
        Usuario usuario = (Usuario) session.getAttribute("usuariosession");
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "No autenticado"));
        }
        return ResponseEntity.ok(Map.of(
                "id", usuario.getId(),
                "nombreUsuario", usuario.getNombreUsuario()
        ));
    }
}

