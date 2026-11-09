package com.DulceMayor.backend.controllers.publico;

import com.DulceMayor.backend.dtos.publico.ChatbotPresupuestoRequest;
import com.DulceMayor.backend.dtos.publico.ChatbotPresupuestoResponse;
import com.DulceMayor.backend.dtos.publico.CotizacionDolarResponse;
import com.DulceMayor.backend.dtos.publico.ProductoPublicoPage;
import com.DulceMayor.backend.dtos.publico.ProductoPublicoResponse;
import com.DulceMayor.backend.services.publico.SitioPublicoLecturaService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para los endpoints de acceso público del sitio institucional
 * ({@code /publico/**} y {@code /api/v1/publico/**}) según {@code 04-openapi.yaml}.
 */
@RestController
@RequestMapping({"/publico", "/api/v1/publico"})
public class SitioPublicoController {

    private final SitioPublicoLecturaService sitioPublicoLecturaService;

    public SitioPublicoController(SitioPublicoLecturaService sitioPublicoLecturaService) {
        this.sitioPublicoLecturaService = sitioPublicoLecturaService;
    }

    @GetMapping("/catalogo")
    public ResponseEntity<ProductoPublicoPage> listarCatalogoPublico(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String categoriaId,
            @RequestParam(required = false) String subCategoriaId,
            @RequestParam(required = false) Boolean enOferta,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "nombre,asc") String sort) {
        Pageable pageable = crearPageable(page, size, sort);
        return ResponseEntity.ok(
                sitioPublicoLecturaService.listarCatalogoPublico(q, categoriaId, subCategoriaId, enOferta, pageable)
        );
    }

    @GetMapping("/catalogo/{id}")
    public ResponseEntity<ProductoPublicoResponse> buscarProductoPublico(@PathVariable String id) {
        return ResponseEntity.ok(sitioPublicoLecturaService.buscarProductoPublico(id));
    }

    @GetMapping("/cotizacion-dolar")
    public ResponseEntity<CotizacionDolarResponse> buscarCotizacionDolar() {
        return ResponseEntity.ok(sitioPublicoLecturaService.obtenerCotizacionDolar());
    }

    @PostMapping("/chatbot/presupuesto")
    public ResponseEntity<ChatbotPresupuestoResponse> consultarChatbotPresupuesto(
            @Valid @RequestBody ChatbotPresupuestoRequest request) {
        return ResponseEntity.ok(sitioPublicoLecturaService.consultarChatbotPresupuesto(request));
    }

    private Pageable crearPageable(int page, int size, String sortParam) {
        int paginaSegura = Math.max(0, page);
        int tamanoSeguro = Math.min(100, Math.max(1, size));
        if (sortParam == null || sortParam.isBlank()) {
            return PageRequest.of(paginaSegura, tamanoSeguro, Sort.by("nombre").ascending());
        }
        String[] partes = sortParam.split(",");
        String propiedad = partes[0].trim();
        Sort.Direction direccion = (partes.length > 1 && "desc".equalsIgnoreCase(partes[1].trim()))
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        return PageRequest.of(paginaSegura, tamanoSeguro, Sort.by(direccion, propiedad));
    }
}

