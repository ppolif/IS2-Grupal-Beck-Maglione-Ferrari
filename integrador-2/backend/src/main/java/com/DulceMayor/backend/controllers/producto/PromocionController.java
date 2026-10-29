package com.DulceMayor.backend.controllers.producto;

import com.DulceMayor.backend.controllers.BaseController;
import com.DulceMayor.backend.dtos.producto.PromocionRequest;
import com.DulceMayor.backend.dtos.producto.PromocionResponse;
import com.DulceMayor.backend.services.BaseService;
import com.DulceMayor.backend.services.producto.PromocionService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para la consulta y administración de promociones y códigos de descuento.
 */
@RestController
@RequestMapping({"/promociones", "/api/v1/promociones"})
public class PromocionController extends BaseController<PromocionService, PromocionRequest, PromocionResponse> {

    public PromocionController(PromocionService promocionService) {
        super(promocionService);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('JEFE', 'ADMINISTRATIVO')")
    public ResponseEntity<List<PromocionResponse>> listarPromocion(
            @RequestParam(required = false) Boolean soloVigentes) {
        return ResponseEntity.ok(service.listar(soloVigentes));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('JEFE', 'ADMINISTRATIVO')")
    public ResponseEntity<PromocionResponse> crearPromocion(@Valid @RequestBody PromocionRequest request) {
        return ejecutarCrear(request);
    }

    @GetMapping("/codigo/{codigo}")
    @PreAuthorize("hasAnyRole('JEFE', 'ADMINISTRATIVO', 'PREVENTISTA')")
    public ResponseEntity<PromocionResponse> buscarPromocionVigentePorCodigo(@PathVariable String codigo) {
        return ResponseEntity.ok(service.buscarVigentePorCodigo(codigo));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('JEFE', 'ADMINISTRATIVO')")
    public ResponseEntity<PromocionResponse> buscarPromocion(@PathVariable String id) {
        return ejecutarBuscar(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('JEFE', 'ADMINISTRATIVO')")
    public ResponseEntity<PromocionResponse> modificarPromocion(
            @PathVariable String id,
            @Valid @RequestBody PromocionRequest request) {
        return ejecutarModificar(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('JEFE', 'ADMINISTRATIVO')")
    public ResponseEntity<Void> eliminarPromocion(@PathVariable String id) {
        return ejecutarEliminar(id);
    }
}

