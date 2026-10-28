package com.DulceMayor.backend.controllers;

import com.DulceMayor.backend.dtos.ProductoPage;
import com.DulceMayor.backend.dtos.ProductoRequest;
import com.DulceMayor.backend.dtos.ProductoResponse;
import com.DulceMayor.backend.services.BaseService;
import com.DulceMayor.backend.services.ProductoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
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
 * Controlador REST para la consulta y administración de productos del catálogo.
 */
@RestController
@RequestMapping({"/productos", "/api/v1/productos"})
public class ProductoController extends BaseController<ProductoRequest, ProductoResponse> {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @Override
    protected BaseService<?, ProductoRequest, ProductoResponse> getService() {
        return productoService;
    }

    @Override
    protected String extraerId(ProductoResponse response) {
        return response.id();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('JEFE', 'ADMINISTRATIVO', 'PREVENTISTA')")
    public ResponseEntity<ProductoPage> listarProducto(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String categoriaId,
            @RequestParam(required = false) String subCategoriaId,
            @RequestParam(required = false) Boolean enOferta,
            @RequestParam(required = false) Boolean soloConStock,
            Pageable pageable) {
        return ResponseEntity.ok(productoService.listarPaginado(
                q, categoriaId, subCategoriaId, enOferta, soloConStock, pageable
        ));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('JEFE', 'ADMINISTRATIVO')")
    public ResponseEntity<ProductoResponse> crearProducto(@Valid @RequestBody ProductoRequest request) {
        return ejecutarCrear(request);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('JEFE', 'ADMINISTRATIVO', 'PREVENTISTA')")
    public ResponseEntity<ProductoResponse> buscarProducto(@PathVariable String id) {
        return ejecutarBuscar(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('JEFE', 'ADMINISTRATIVO')")
    public ResponseEntity<ProductoResponse> modificarProducto(
            @PathVariable String id,
            @Valid @RequestBody ProductoRequest request) {
        return ejecutarModificar(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('JEFE', 'ADMINISTRATIVO')")
    public ResponseEntity<Void> eliminarProducto(@PathVariable String id) {
        return ejecutarEliminar(id);
    }
}

