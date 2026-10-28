package com.DulceMayor.backend.controllers.producto;

import com.DulceMayor.backend.controllers.BaseController;
import com.DulceMayor.backend.dtos.producto.CategoriaRequest;
import com.DulceMayor.backend.dtos.producto.CategoriaResponse;
import com.DulceMayor.backend.services.BaseService;
import com.DulceMayor.backend.services.producto.CategoriaService;
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
 * Controlador REST para la administración y consulta de categorías del catálogo.
 *
 * <p><b>Patrón aplicado: Template Method (Método Plantilla) en el Controlador.</b></p>
 * <p><b>Por qué se aplica acá:</b> Cada endpoint declara explícitamente su ruta HTTP, el nombre de
 * operación ({@code operationId}) exigido por {@code 04-openapi.yaml} y la regla de seguridad
 * {@code @PreAuthorize} correspondiente a {@code x-roles}, y delega la ejecución y el armado de la
 * respuesta HTTP ({@code 200}, {@code 201 + Location}, {@code 204}) en los métodos plantilla
 * heredados de {@link BaseController}.</p>
 *
 * <p><b>Patrón aplicado: DTO (Data Transfer Object).</b></p>
 * <p><b>Por qué se aplica acá:</b> El controlador recibe {@link CategoriaRequest} y devuelve
 * {@link CategoriaResponse}, sin acoplarse nunca a la entidad {@code Categoria} ni a su repositorio.</p>
 */
@RestController
@RequestMapping({"/categorias", "/api/v1/categorias"})
public class CategoriaController extends BaseController<CategoriaRequest, CategoriaResponse> {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @Override
    protected BaseService<?, CategoriaRequest, CategoriaResponse> getService() {
        return categoriaService;
    }

    @Override
    protected String extraerId(CategoriaResponse response) {
        return response.id();
    }

    @GetMapping
    public ResponseEntity<List<CategoriaResponse>> listarCategoria() {
        return ejecutarListar();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('JEFE', 'ADMINISTRATIVO')")
    public ResponseEntity<CategoriaResponse> crearCategoria(@Valid @RequestBody CategoriaRequest request) {
        return ejecutarCrear(request);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoriaResponse> buscarCategoria(@PathVariable String id) {
        return ejecutarBuscar(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('JEFE', 'ADMINISTRATIVO')")
    public ResponseEntity<CategoriaResponse> modificarCategoria(
            @PathVariable String id,
            @Valid @RequestBody CategoriaRequest request) {
        return ejecutarModificar(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('JEFE', 'ADMINISTRATIVO')")
    public ResponseEntity<Void> eliminarCategoria(@PathVariable String id) {
        return ejecutarEliminar(id);
    }
}

