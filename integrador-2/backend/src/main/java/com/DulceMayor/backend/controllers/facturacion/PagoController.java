package com.DulceMayor.backend.controllers.facturacion;

import com.DulceMayor.backend.controllers.BaseController;
import com.DulceMayor.backend.dtos.facturacion.PagoRequest;
import com.DulceMayor.backend.dtos.facturacion.PagoResponse;
import com.DulceMayor.backend.services.facturacion.PagoService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * Controlador REST para el registro, consulta y eliminación lógica de pagos asociados a facturas.
 *
 * <p><b>Patrón GoF aplicado: Template Method (Comportamiento)</b></p>
 * <ul>
 *   <li><b>Participante GoF:</b> ConcreteClass que extiende {@link BaseController}.</li>
 *   <li><b>Implementación:</b> Reutiliza {@link #ejecutarEliminar(String)} para la baja lógica de pagos
 *       en {@code DELETE /pagos/{id}} y expone las rutas anidadas en {@code /facturas/{id}/pagos}.</li>
 * </ul>
 */
@RestController
public class PagoController extends BaseController<PagoService, PagoRequest, PagoResponse> {

    public PagoController(PagoService pagoService) {
        super(pagoService);
    }

    @GetMapping({"/facturas/{id}/pagos", "/api/v1/facturas/{id}/pagos"})
    @PreAuthorize("hasAnyRole('JEFE', 'ADMINISTRATIVO', 'MINORISTA')")
    public ResponseEntity<List<PagoResponse>> listarPagoPorFactura(@PathVariable String id) {
        return ResponseEntity.ok(service.listarPorFactura(id));
    }

    @PostMapping({"/facturas/{id}/pagos", "/api/v1/facturas/{id}/pagos"})
    @PreAuthorize("hasAnyRole('JEFE', 'ADMINISTRATIVO')")
    public ResponseEntity<PagoResponse> registrarPago(
            @PathVariable String id,
            @Valid @RequestBody PagoRequest request) {
        PagoResponse creado = service.registrarPago(id, request);
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/pagos/{pagoId}")
                .buildAndExpand(extraerId(creado))
                .toUri();
        return ResponseEntity.created(location).body(creado);
    }

    @DeleteMapping({"/pagos/{id}", "/api/v1/pagos/{id}"})
    @PreAuthorize("hasRole('JEFE')")
    public ResponseEntity<Void> eliminarPago(@PathVariable String id) {
        return ejecutarEliminar(id);
    }
}

