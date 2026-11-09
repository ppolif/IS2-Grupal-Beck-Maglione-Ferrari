package com.DulceMayor.backend.controllers.estadistica;

import com.DulceMayor.backend.dtos.estadistica.PedidosPorPreventorPorDiaResponse;
import com.DulceMayor.backend.dtos.estadistica.StockPorProductoItem;
import com.DulceMayor.backend.services.estadistica.EstadisticaLecturaService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para la consulta de estadísticas gerenciales ({@code /estadisticas/**})
 * según {@code 04-openapi.yaml}.
 */
@RestController
@RequestMapping({"/estadisticas", "/api/v1/estadisticas"})
public class EstadisticaController {

    private final EstadisticaLecturaService estadisticaLecturaService;

    public EstadisticaController(EstadisticaLecturaService estadisticaLecturaService) {
        this.estadisticaLecturaService = estadisticaLecturaService;
    }

    @GetMapping("/pedidos-por-preventor-por-dia")
    @PreAuthorize("hasRole('JEFE')")
    public ResponseEntity<PedidosPorPreventorPorDiaResponse> estadisticaPedidosPorPreventorPorDia(
            @RequestParam Integer anio,
            @RequestParam Integer mes) {
        return ResponseEntity.ok(
                estadisticaLecturaService.obtenerPedidosPorPreventorPorDia(anio, mes)
        );
    }

    @GetMapping("/stock-por-producto")
    @PreAuthorize("hasRole('JEFE')")
    public ResponseEntity<List<StockPorProductoItem>> estadisticaStockPorProducto() {
        return ResponseEntity.ok(
                estadisticaLecturaService.obtenerStockPorProducto()
        );
    }
}

