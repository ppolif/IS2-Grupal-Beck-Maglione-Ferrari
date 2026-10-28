package com.DulceMayor.backend.controllers.direccion;

import com.DulceMayor.backend.controllers.BaseController;
import com.DulceMayor.backend.dtos.direccion.PaisRequest;
import com.DulceMayor.backend.dtos.direccion.PaisResponse;
import com.DulceMayor.backend.services.BaseService;
import com.DulceMayor.backend.services.direccion.PaisService;
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
 * Controlador REST para la consulta y administración de países.
 * Aplica el patrón Template Method heredando de {@link BaseController}.
 */
@RestController
@RequestMapping({"/paises", "/api/v1/paises"})
public class PaisController extends BaseController<PaisRequest, PaisResponse> {

    private final PaisService paisService;

    public PaisController(PaisService paisService) {
        this.paisService = paisService;
    }

    @Override
    protected BaseService<?, PaisRequest, PaisResponse> getService() {
        return paisService;
    }

    @Override
    protected String extraerId(PaisResponse response) {
        return response.id();
    }

    @GetMapping
    public ResponseEntity<List<PaisResponse>> listarPais() {
        return ejecutarListar();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('JEFE', 'ADMINISTRATIVO')")
    public ResponseEntity<PaisResponse> crearPais(@Valid @RequestBody PaisRequest request) {
        return ejecutarCrear(request);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaisResponse> buscarPais(@PathVariable String id) {
        return ejecutarBuscar(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('JEFE', 'ADMINISTRATIVO')")
    public ResponseEntity<PaisResponse> modificarPais(
            @PathVariable String id,
            @Valid @RequestBody PaisRequest request) {
        return ejecutarModificar(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('JEFE', 'ADMINISTRATIVO')")
    public ResponseEntity<Void> eliminarPais(@PathVariable String id) {
        return ejecutarEliminar(id);
    }
}

