package com.DulceMayor.backend.controllers.persona;

import com.DulceMayor.backend.dtos.persona.PuntoDeVentaPage;
import com.DulceMayor.backend.dtos.persona.PuntoDeVentaRequest;
import com.DulceMayor.backend.dtos.persona.PuntoDeVentaResponse;
import com.DulceMayor.backend.services.persona.PuntoDeVentaService;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.data.domain.Pageable;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para la administración de comercios y puntos de venta físicos.
 */
@RestController
@RequestMapping
public class PuntoDeVentaController {

    private final PuntoDeVentaService puntoDeVentaService;

    public PuntoDeVentaController(PuntoDeVentaService puntoDeVentaService) {
        this.puntoDeVentaService = puntoDeVentaService;
    }

    @GetMapping({"/puntos-de-venta", "/api/v1/puntos-de-venta"})
    @PreAuthorize("hasAnyRole('JEFE', 'ADMINISTRATIVO', 'PREVENTISTA')")
    public ResponseEntity<PuntoDeVentaPage> listarPuntoDeVenta(
            @RequestParam(required = false) String minoristaId,
            @RequestParam(required = false) String preventorId,
            @RequestParam(required = false) String q,
            Pageable pageable) {
        return ResponseEntity.ok(puntoDeVentaService.buscarPaginado(minoristaId, preventorId, q, pageable));
    }

    @PostMapping({"/puntos-de-venta", "/api/v1/puntos-de-venta"})
    @PreAuthorize("hasAnyRole('JEFE', 'ADMINISTRATIVO', 'PREVENTISTA')")
    public ResponseEntity<PuntoDeVentaResponse> crearPuntoDeVenta(@Valid @RequestBody PuntoDeVentaRequest request) {
        PuntoDeVentaResponse creado = puntoDeVentaService.crear(request);
        return ResponseEntity.created(URI.create("/puntos-de-venta/" + creado.id())).body(creado);
    }

    @GetMapping({"/puntos-de-venta/{id}", "/api/v1/puntos-de-venta/{id}"})
    @PreAuthorize("hasAnyRole('JEFE', 'ADMINISTRATIVO', 'PREVENTISTA')")
    public ResponseEntity<PuntoDeVentaResponse> buscarPuntoDeVenta(@PathVariable String id) {
        return ResponseEntity.ok(puntoDeVentaService.buscar(id));
    }

    @PutMapping({"/puntos-de-venta/{id}", "/api/v1/puntos-de-venta/{id}"})
    @PreAuthorize("hasAnyRole('JEFE', 'ADMINISTRATIVO', 'PREVENTISTA')")
    public ResponseEntity<PuntoDeVentaResponse> modificarPuntoDeVenta(
            @PathVariable String id,
            @Valid @RequestBody PuntoDeVentaRequest request) {
        return ResponseEntity.ok(puntoDeVentaService.modificar(id, request));
    }

    @DeleteMapping({"/puntos-de-venta/{id}", "/api/v1/puntos-de-venta/{id}"})
    @PreAuthorize("hasAnyRole('JEFE', 'ADMINISTRATIVO')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarPuntoDeVenta(@PathVariable String id) {
        puntoDeVentaService.eliminar(id);
    }
}

