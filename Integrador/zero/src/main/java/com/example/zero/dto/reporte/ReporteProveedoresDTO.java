package com.example.zero.dto.reporte;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO contenedor para el Reporte de Proveedores con métricas y listado de menores costos por producto.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReporteProveedoresDTO {
    private int totalProductos;
    private String proveedorMasConveniente;
    private int productosConMejorPrecioProveedor;

    @Builder.Default
    private List<ReporteProveedorPrecioDTO> items = new ArrayList<>();
}
