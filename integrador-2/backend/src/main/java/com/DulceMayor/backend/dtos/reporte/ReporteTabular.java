package com.DulceMayor.backend.dtos.reporte;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Estructura genérica de reporte tabular para visualización en pantalla y exportación
 * a PDF/Excel según {@code 04-openapi.yaml}.
 */
public record ReporteTabular(
        String titulo,
        LocalDateTime generadoEn,
        LocalDate desde,
        LocalDate hasta,
        List<ColumnaReporte> columnas,
        List<Map<String, Object>> filas
) {
}

