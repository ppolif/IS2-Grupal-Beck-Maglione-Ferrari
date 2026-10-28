package com.DulceMayor.backend.controllers.producto;

import com.DulceMayor.backend.dtos.producto.StockAjusteRequest;
import com.DulceMayor.backend.dtos.producto.StockPage;
import com.DulceMayor.backend.dtos.producto.StockReposicionRequest;
import com.DulceMayor.backend.dtos.producto.StockResponse;
import com.DulceMayor.backend.services.producto.StockService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para consultas y operaciones de inventario/stock.
 * Este controlador no extiende del controlador base ya que maneja su CRUD a demanda de stock de productos, no tiene uno propio, por lo tanto, su CRUD no es el mismo que el que maneja el controlador base.
 * Su servicio implementa el patron observer
 */
@RestController
@RequestMapping
public class StockController {

    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @GetMapping({"/productos/{id}/stock", "/api/v1/productos/{id}/stock"})
    @PreAuthorize("hasAnyRole('JEFE', 'ADMINISTRATIVO', 'PREVENTISTA')")
    public ResponseEntity<StockResponse> buscarStockPorProducto(@PathVariable String id) {
        return ResponseEntity.ok(stockService.buscarPorProducto(id));
    }

    @PutMapping({"/productos/{id}/stock", "/api/v1/productos/{id}/stock"})
    @PreAuthorize("hasAnyRole('JEFE', 'ADMINISTRATIVO')")
    public ResponseEntity<StockResponse> modificarStock(
            @PathVariable String id,
            @Valid @RequestBody StockAjusteRequest request) {
        return ResponseEntity.ok(stockService.modificarStock(id, request));
    }

    @PostMapping({"/productos/{id}/stock/reposiciones", "/api/v1/productos/{id}/stock/reposiciones"})
    @PreAuthorize("hasAnyRole('JEFE', 'ADMINISTRATIVO')")
    public ResponseEntity<StockResponse> reponerStock(
            @PathVariable String id,
            @Valid @RequestBody StockReposicionRequest request) {
        return ResponseEntity.ok(stockService.reponerStock(id, request));
    }

    @GetMapping({"/stock", "/api/v1/stock"})
    @PreAuthorize("hasAnyRole('JEFE', 'ADMINISTRATIVO')")
    public ResponseEntity<StockPage> listarStock(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Boolean soloSinStock,
            Pageable pageable) {
        return ResponseEntity.ok(stockService.listarStock(q, soloSinStock, pageable));
    }
}

