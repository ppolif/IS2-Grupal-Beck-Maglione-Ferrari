package org.example.controllers;

import org.example.entities.FacturaVenta;
import org.example.services.FacturaVentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/facturas-venta")
public class FacturaVentaController {

    @Autowired
    private FacturaVentaService service;

    @PostMapping
    public ResponseEntity<FacturaVenta> crearFacturaVenta(@RequestBody FacturaVenta factura) {
        return ResponseEntity.ok(service.crear(factura));
    }

    @GetMapping
    public ResponseEntity<List<FacturaVenta>> listarFacturasVenta() {
        return ResponseEntity.ok(service.listarActivas());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarFacturaVenta(@PathVariable String id) {
        if (service.eliminar(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}