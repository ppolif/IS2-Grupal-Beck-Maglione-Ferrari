package com.DulceMayor.backend.controllers;

import com.DulceMayor.backend.config.ConflictoException;
import com.DulceMayor.backend.config.RecursoNoEncontradoException;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador exclusivo de pruebas para validar autorización (@PreAuthorize),
 * rutas públicas y respuestas uniformes del manejador global de errores.
 */
@RestController
public class SeguridadTestController {

    @GetMapping("/test/autenticado")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> cualquierAutenticado() {
        return ResponseEntity.ok(Map.of("acceso", "AUTENTICADO"));
    }

    @GetMapping("/test/no-encontrado")
    public ResponseEntity<Void> errorNoEncontrado() {
        throw new RecursoNoEncontradoException("Elemento de prueba inexistente");
    }

    @GetMapping("/test/conflicto")
    public ResponseEntity<Void> errorConflicto() {
        throw new ConflictoException("TRANSICION_INVALIDA", "No se permite el cambio de estado solicitado");
    }

    @GetMapping("/test/error-interno")
    public ResponseEntity<Void> errorInterno() {
        throw new RuntimeException("Detalle sensible interno de SQL que no debe filtrarse");
    }

    @GetMapping("/publico/test-catalogo")
    public ResponseEntity<Map<String, String>> publicoCatalogo() {
        return ResponseEntity.ok(Map.of("publico", "ok"));
    }
}
