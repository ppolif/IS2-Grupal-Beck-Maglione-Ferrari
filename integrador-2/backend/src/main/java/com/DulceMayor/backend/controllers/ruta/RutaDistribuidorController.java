package com.DulceMayor.backend.controllers.ruta;

import com.DulceMayor.backend.dtos.ruta.AvanceRutaResponse;
import com.DulceMayor.backend.dtos.ruta.EntregaRequest;
import com.DulceMayor.backend.dtos.ruta.RutaDistribuidorRequest;
import com.DulceMayor.backend.dtos.ruta.RutaResponse;
import com.DulceMayor.backend.services.ruta.RutaDistribuidorService;
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
 * Controlador REST para la gestión y seguimiento de rutas de entrega de pedidos según {@code 04-openapi.yaml}.
 *
 * <p>Permite a los administradores armar los repartos diarios a partir de pedidos facturados,
 * y a los distribuidores consultar su itinerario en OpenStreetMap, confirmar entregas y avanzar la ruta.</p>
 */
@RestController
@RequiredArgsConstructor
public class RutaDistribuidorController {

    private final RutaDistribuidorService rutaDistribuidorService;

    /**
     * Lista las rutas de distribuidores existentes. El rol DISTRIBUIDOR solo ve sus propias rutas.
     * Utilizado en la auditoría del módulo de logística del dashboard administrativo.
     */
    @GetMapping({"/rutas-distribuidor", "/api/v1/rutas-distribuidor"})
    @PreAuthorize("hasAnyRole('JEFE', 'ADMINISTRATIVO', 'DISTRIBUIDOR')")
    public ResponseEntity<List<RutaResponse>> listarRutaPorFechaDistribuidor(
            @RequestParam(required = false) String responsableId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(required = false) String estado) {
        return ResponseEntity.ok(rutaDistribuidorService.listarRutas(responsableId, fecha, estado));
    }

    /**
     * Genera automáticamente las rutas de entrega del día para los distribuidores activos.
     * Balancea los pedidos facturados entre los distribuidores disponibles, optimiza
     * el itinerario de cada uno desde la Sede Central y pasa los pedidos a EN_REPARTO.
     */
    @PostMapping({"/rutas-distribuidor", "/api/v1/rutas-distribuidor"})
    @PreAuthorize("hasAnyRole('JEFE', 'ADMINISTRATIVO')")
    public ResponseEntity<List<RutaResponse>> generarRutasDistribuidor(@RequestBody RutaDistribuidorRequest request) {
        List<RutaResponse> creadas = rutaDistribuidorService.crearRutasAutomaticas(request != null ? request.fecha() : null);
        return ResponseEntity.ok(creadas);
    }

    /**
     * Consulta la ruta del día del distribuidor logueado.
     * Utilizado por la app móvil para trazar el itinerario de entrega sobre OpenStreetMap.
     */
    @GetMapping({"/rutas-distribuidor/mi-ruta-del-dia", "/api/v1/rutas-distribuidor/mi-ruta-del-dia"})
    @PreAuthorize("hasRole('DISTRIBUIDOR')")
    public ResponseEntity<RutaResponse> buscarRutaDistribuidorDelDia(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(rutaDistribuidorService.buscarMiRutaDelDia(fecha));
    }

    /**
     * Obtiene el detalle de una ruta de entrega específica.
     */
    @GetMapping({"/rutas-distribuidor/{id}", "/api/v1/rutas-distribuidor/{id}"})
    @PreAuthorize("hasAnyRole('JEFE', 'ADMINISTRATIVO', 'DISTRIBUIDOR')")
    public ResponseEntity<RutaResponse> buscarRutaDistribuidor(@PathVariable String id) {
        return ResponseEntity.ok(rutaDistribuidorService.buscarPorId(id));
    }

    /**
     * Marca un pedido como ENTREGADO, avanza paradaActual, dispara la notificación por correo
     * al próximo cliente del recorrido y devuelve la siguiente parada para el mapa interactivo.
     */
    @PostMapping({"/rutas-distribuidor/{id}/entregas", "/api/v1/rutas-distribuidor/{id}/entregas"})
    @PreAuthorize("hasAnyRole('DISTRIBUIDOR', 'JEFE', 'ADMINISTRATIVO')")
    public ResponseEntity<AvanceRutaResponse> registrarParadaCompletadaDistribuidor(
            @PathVariable String id,
            @RequestBody EntregaRequest request) {
        return ResponseEntity.ok(rutaDistribuidorService.registrarEntrega(id, request));
    }

    /**
     * Cierra la ruta de entrega del día (REALIZADA si se entregó todo, INCOMPLETA si hubo devoluciones o faltantes).
     */
    @PostMapping({"/rutas-distribuidor/{id}/cerrar", "/api/v1/rutas-distribuidor/{id}/cerrar"})
    @PreAuthorize("hasAnyRole('DISTRIBUIDOR', 'JEFE', 'ADMINISTRATIVO')")
    public ResponseEntity<RutaResponse> cambiarEstadoRutaDistribuidor(@PathVariable String id) {
        return ResponseEntity.ok(rutaDistribuidorService.cerrarRuta(id));
    }
}

