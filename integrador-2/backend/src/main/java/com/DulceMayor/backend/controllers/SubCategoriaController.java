package com.DulceMayor.backend.controllers;

import com.DulceMayor.backend.dtos.SubCategoriaRequest;
import com.DulceMayor.backend.dtos.SubCategoriaResponse;
import com.DulceMayor.backend.services.BaseService;
import com.DulceMayor.backend.services.SubCategoriaService;
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
 * Controlador REST para la consulta y administración de subcategorías del catálogo.
 */
@RestController
@RequestMapping({"/subcategorias", "/api/v1/subcategorias"})
public class SubCategoriaController extends BaseController<SubCategoriaRequest, SubCategoriaResponse> {

    private final SubCategoriaService subCategoriaService;

    public SubCategoriaController(SubCategoriaService subCategoriaService) {
        this.subCategoriaService = subCategoriaService;
    }

    @Override
    protected BaseService<?, SubCategoriaRequest, SubCategoriaResponse> getService() {
        return subCategoriaService;
    }

    @Override
    protected String extraerId(SubCategoriaResponse response) {
        return response.id();
    }

    @GetMapping
    public ResponseEntity<List<SubCategoriaResponse>> listarSubCategoria(
            @RequestParam(required = false) String categoriaId) {
        return ResponseEntity.ok(subCategoriaService.listar(categoriaId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('JEFE', 'ADMINISTRATIVO')")
    public ResponseEntity<SubCategoriaResponse> crearSubCategoria(@Valid @RequestBody SubCategoriaRequest request) {
        return ejecutarCrear(request);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubCategoriaResponse> buscarSubCategoria(@PathVariable String id) {
        return ejecutarBuscar(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('JEFE', 'ADMINISTRATIVO')")
    public ResponseEntity<SubCategoriaResponse> modificarSubCategoria(
            @PathVariable String id,
            @Valid @RequestBody SubCategoriaRequest request) {
        return ejecutarModificar(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('JEFE', 'ADMINISTRATIVO')")
    public ResponseEntity<Void> eliminarSubCategoria(@PathVariable String id) {
        return ejecutarEliminar(id);
    }
}

