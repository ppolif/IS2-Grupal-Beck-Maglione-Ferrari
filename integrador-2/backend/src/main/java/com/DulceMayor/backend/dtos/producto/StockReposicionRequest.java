package com.DulceMayor.backend.dtos.producto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * DTO de entrada para reposición de stock según 04-openapi.yaml.
 */
public record StockReposicionRequest(
        @NotNull(message = "La cantidad a reponer es obligatoria")
        @Min(value = 1, message = "La cantidad a reponer debe ser al menos 1")
        Integer cantidad,

        String observacion
) {
}

