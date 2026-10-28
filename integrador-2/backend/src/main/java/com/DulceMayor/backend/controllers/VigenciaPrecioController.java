package com.DulceMayor.backend.controllers;

import com.DulceMayor.backend.dtos.VigenciaPrecioRequest;
import com.DulceMayor.backend.dtos.VigenciaPrecioResponse;
import com.DulceMayor.backend.services.VigenciaPrecioService;
import jakarta.validation.Valid;
import java.net.URI;
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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * Controlador REST para el historial y administración de vigencias de precios.
 */
@RestController
@RequestMapping
public class VigenciaPrecioController {

    private final VigenciaPrecioService vigenciaPrecioService;

    public VigenciaPrecioController(VigenciaPrecioService vigenciaPrecioService) {
        this.vigenciaPrecioService = vigenciaPrecioService;
    }

    @GetMapping({"/productos/{id}/vigencias-precio", "/api/v1/productos/{id}/vigencias-precio"})
    @PreAuthorize("hasAnyRole('JEFE', 'ADMINISTRATIVO')")
    public ResponseEntity<List<VigenciaPrecioResponse>> listarVigenciaPrecio(@PathVariable String id) {
        return ResponseEntity.ok(vigenciaPrecioService.listarPorProducto(id));
    }

    @PostMapping({"/productos/{id}/vigencias-precio", "/api/v1/productos/{id}/vigencias-precio"})
    @PreAuthorize("hasAnyRole('JEFE', 'ADMINISTRATIVO')")
    public ResponseEntity<VigenciaPrecioResponse> crearVigenciaPrecio(
            @PathVariable String id,
            @Valid @RequestBody VigenciaPrecioRequest request) {
        VigenciaPrecioResponse response = vigenciaPrecioService.registrarPrecio(id, request);
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/vigencias-precio/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping({"/vigencias-precio/{id}", "/api/v1/vigencias-precio/{id}"})
    @PreAuthorize("hasAnyRole('JEFE', 'ADMINISTRATIVO')")
    public ResponseEntity<VigenciaPrecioResponse> modificarVigenciaPrecio(
            @PathVariable String id,
            @Valid @RequestBody VigenciaPrecioRequest request) {
        return ResponseEntity.ok(vigenciaPrecioService.modificar(id, request));
    }

    @DeleteMapping({"/vigencias-precio/{id}", "/api/v1/vigencias-precio/{id}"})
    @PreAuthorize("hasAnyRole('JEFE', 'ADMINISTRATIVO')")
    public ResponseEntity<Void> eliminarVigenciaPrecio(@PathVariable String id) {
        vigenciaPrecioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

