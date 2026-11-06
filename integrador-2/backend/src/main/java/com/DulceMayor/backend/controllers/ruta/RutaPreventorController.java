package com.DulceMayor.backend.controllers.ruta;

import com.DulceMayor.backend.dtos.ruta.AvanceRutaResponse;
import com.DulceMayor.backend.dtos.ruta.CompletarParadaPreventorRequest;
import com.DulceMayor.backend.dtos.ruta.RutaPreventorGenerarRequest;
import com.DulceMayor.backend.dtos.ruta.RutaResponse;
import com.DulceMayor.backend.services.ruta.RutaPreventorService;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * Controlador REST para la gestión de rutas diarias de preventores según {@code 04-openapi.yaml}.
 *
 * <p>Expone endpoints para listar itinerarios, generar rutas optimizadas por el vecino más cercano
 * (con coordenadas automáticas de sucursal para OpenStreetMap), consultar el rutero del día en el móvil
 * y marcar paradas completadas utilizando la estrategia de lista cronológica de visitados.</p>
 */
@RestController
@RequiredArgsConstructor
public class RutaPreventorController {

    private final RutaPreventorService rutaPreventorService;

    /**
     * Lista las rutas de preventores con filtros opcionales de responsable, fecha y estado.
     * Utilizado por el panel de supervisión de jefes y administrativos, o por el preventista para ver su historial.
     */
    @GetMapping({"/rutas-preventor", "/api/v1/rutas-preventor"})
    @PreAuthorize("hasAnyRole('JEFE', 'ADMINISTRATIVO', 'PREVENTISTA')")
    public ResponseEntity<List<RutaResponse>> listarRutaPorFechaPreventor(
            @RequestParam(required = false) String responsableId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(required = false) String estado) {
        return ResponseEntity.ok(rutaPreventorService.listarRutas(responsableId, fecha, estado));
    }

    /**
     * Genera la ruta de visitas diaria para un preventor.
     * El punto de inicio geodésico se determina automáticamente a partir de la sucursal asignada.
     */
    @PostMapping({"/rutas-preventor/generar", "/api/v1/rutas-preventor/generar"})
    @PreAuthorize("hasAnyRole('JEFE', 'ADMINISTRATIVO', 'PREVENTISTA')")
    public ResponseEntity<RutaResponse> crearRutaPreventor(@RequestBody RutaPreventorGenerarRequest request) {
        RutaResponse creada = rutaPreventorService.generarRuta(request);
        URI uri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/rutas-preventor/{id}")
                .buildAndExpand(creada.id())
                .toUri();
        return ResponseEntity.created(uri).body(creada);
    }

    /**
     * Devuelve la ruta asignada al preventor autenticado para el día consultado (por defecto hoy).
     * Invocado al abrir la aplicación móvil en la calle para pintar los puntos de visita en el mapa OpenStreetMap.
     */
    @GetMapping({"/rutas-preventor/mi-ruta-del-dia", "/api/v1/rutas-preventor/mi-ruta-del-dia"})
    @PreAuthorize("hasRole('PREVENTISTA')")
    public ResponseEntity<RutaResponse> buscarRutaPreventorDelDia(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(rutaPreventorService.buscarMiRutaDelDia(fecha));
    }

    /**
     * Consulta el detalle de una ruta específica por su identificador.
     */
    @GetMapping({"/rutas-preventor/{id}", "/api/v1/rutas-preventor/{id}"})
    @PreAuthorize("hasAnyRole('JEFE', 'ADMINISTRATIVO', 'PREVENTISTA')")
    public ResponseEntity<RutaResponse> buscarRutaPreventor(@PathVariable String id) {
        return ResponseEntity.ok(rutaPreventorService.buscarPorId(id));
    }

    /**
     * Marca un punto de venta como visitado (agregándolo a la lista de visitados de la ruta)
     * y obtiene la siguiente parada sugerida para centrar el mapa.
     */
    @PostMapping({"/rutas-preventor/{id}/parada-completada", "/api/v1/rutas-preventor/{id}/parada-completada"})
    @PreAuthorize("hasAnyRole('PREVENTISTA', 'JEFE')")
    public ResponseEntity<AvanceRutaResponse> registrarParadaCompletadaPreventor(
            @PathVariable String id,
            @RequestBody CompletarParadaPreventorRequest request) {
        return ResponseEntity.ok(rutaPreventorService.completarParada(id, request));
    }

    /**
     * Cierra la jornada del preventor, cambiando el estado a REALIZADA o INCOMPLETA.
     */
    @PostMapping({"/rutas-preventor/{id}/cerrar", "/api/v1/rutas-preventor/{id}/cerrar"})
    @PreAuthorize("hasAnyRole('PREVENTISTA', 'JEFE', 'ADMINISTRATIVO')")
    public ResponseEntity<RutaResponse> cambiarEstadoRutaPreventor(@PathVariable String id) {
        return ResponseEntity.ok(rutaPreventorService.cerrarRuta(id));
    }
}

