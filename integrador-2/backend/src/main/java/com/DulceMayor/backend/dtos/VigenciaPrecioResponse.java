package com.DulceMayor.backend.dtos;

import java.time.LocalDate;

/**
 * DTO de salida para una vigencia de precio de un producto según 04-openapi.yaml.
 */
public record VigenciaPrecioResponse(
        String id,
        String productoId,
        LocalDate fechaDesde,
        LocalDate fechaHasta,
        Double precio
) {
}

