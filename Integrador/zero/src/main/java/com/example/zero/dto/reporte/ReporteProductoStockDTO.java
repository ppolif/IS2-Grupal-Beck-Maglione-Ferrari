package com.example.zero.dto.reporte;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Representa un producto en el Reporte de Productos con análisis de stock por sucursal
 * y contacto de reposición al proveedor más económico.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReporteProductoStockDTO {
    private String productoId;
    private String codigo;
    private String nombre;
    private String imagenUrl;
    private String talle;
    private String categoria;
    private String sucursal;
    private int stockActual;
    private String estadoStock; // BIEN, REGULAR, MALO
    private int cantidadFaltante;
    private String proveedorId;
    private String proveedorNombre;
    private String proveedorTelefono;
    private Double menorCosto;
    private String urlWhatsAppReposicion;
}

