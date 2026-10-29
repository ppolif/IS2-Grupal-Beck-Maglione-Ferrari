package com.DulceMayor.backend.controllers.facturacion;

import com.DulceMayor.backend.controllers.BaseController;
import com.DulceMayor.backend.dtos.facturacion.FormaDePagoRequest;
import com.DulceMayor.backend.dtos.facturacion.FormaDePagoResponse;
import com.DulceMayor.backend.services.facturacion.FormaDePagoService;
import jakarta.validation.Valid;
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

/**
 * Controlador REST para la administración y consulta de formas de pago.
 *
 * <p><b>Patrón GoF aplicado: Template Method (Comportamiento)</b></p>
 * <ul>
 *   <li><b>Participante GoF:</b> ConcreteClass (Subclase Concreta) que extiende {@link BaseController}.</li>
 *   <li><b>Implementación:</b> Reutiliza los métodos plantilla {@link #ejecutarListar()},
 *       {@link #ejecutarBuscar(String)}, {@link #ejecutarCrear(Object)},
 *       {@link #ejecutarModificar(String, Object)} y {@link #ejecutarEliminar(String)},
 *       configurando en cada endpoint el contrato de rutas y seguridad de {@code 04-openapi.yaml}.</li>
 * </ul>
 */
@RestController
@RequestMapping({"/formas-de-pago", "/api/v1/formas-de-pago"})
public class FormaDePagoController extends BaseController<FormaDePagoService, FormaDePagoRequest, FormaDePagoResponse> {

    public FormaDePagoController(FormaDePagoService formaDePagoService) {
        super(formaDePagoService);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<FormaDePagoResponse>> listarFormaDePago() {
        return ejecutarListar();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('JEFE', 'ADMINISTRATIVO')")
    public ResponseEntity<FormaDePagoResponse> crearFormaDePago(@Valid @RequestBody FormaDePagoRequest request) {
        return ejecutarCrear(request);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FormaDePagoResponse> buscarFormaDePago(@PathVariable String id) {
        return ejecutarBuscar(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('JEFE', 'ADMINISTRATIVO')")
    public ResponseEntity<FormaDePagoResponse> modificarFormaDePago(
            @PathVariable String id,
            @Valid @RequestBody FormaDePagoRequest request) {
        return ejecutarModificar(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('JEFE', 'ADMINISTRATIVO')")
    public ResponseEntity<Void> eliminarFormaDePago(@PathVariable String id) {
        return ejecutarEliminar(id);
    }
}

