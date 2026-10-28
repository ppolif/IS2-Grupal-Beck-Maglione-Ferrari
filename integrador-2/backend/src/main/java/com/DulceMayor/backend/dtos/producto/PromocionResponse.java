package com.DulceMayor.backend.dtos.producto;

import java.time.LocalDate;

/**
 * DTO de salida para una promoción según 04-openapi.yaml.
 */
public record PromocionResponse(
        String id,
        String codigo,
        String descripcion,
        Double porcentajeDescuento,
        Integer cantidadMinima,
        String productoId,
        String productoNombre,
        LocalDate fechaDesde,
        LocalDate fechaHasta
) {
}

