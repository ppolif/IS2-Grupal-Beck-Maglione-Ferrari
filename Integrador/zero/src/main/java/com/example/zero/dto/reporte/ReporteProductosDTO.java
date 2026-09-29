package com.example.zero.dto.reporte;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO contenedor para el Reporte de Productos (métricas generales de inventario y listado).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReporteProductosDTO {
    private String sucursalNombre;
    private int totalStockUnidades;
    private int stockBienCount;
    private int stockRegularCount;
    private int stockMaloCount;

    @Builder.Default
    private List<ReporteProductoStockDTO> productos = new ArrayList<>();
}
