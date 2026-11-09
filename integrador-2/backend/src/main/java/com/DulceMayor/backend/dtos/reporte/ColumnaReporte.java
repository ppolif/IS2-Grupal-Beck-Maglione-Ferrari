package com.DulceMayor.backend.dtos.reporte;

/**
 * Definición de una columna dentro de un {@link ReporteTabular} según {@code 04-openapi.yaml}.
 */
public record ColumnaReporte(
        String clave,
        String titulo
) {
}

