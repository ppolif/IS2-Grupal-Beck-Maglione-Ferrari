package com.DulceMayor.backend.dtos.producto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

/**
 * DTO de entrada para crear o modificar una vigencia de precio según 04-openapi.yaml.
 */
public record VigenciaPrecioRequest(
        @NotNull(message = "La fecha desde es obligatoria")
        LocalDate fechaDesde,

        LocalDate fechaHasta,

        @NotNull(message = "El precio es obligatorio")
        @Positive(message = "El precio debe ser un número positivo mayor a cero")
        Double precio
) {
}

