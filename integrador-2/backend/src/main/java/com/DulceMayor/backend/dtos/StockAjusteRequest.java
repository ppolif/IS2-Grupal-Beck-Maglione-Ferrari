package com.DulceMayor.backend.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * DTO de entrada para ajuste físico de inventario según 04-openapi.yaml.
 */

/**
 *
 * utilizado por un usuario humano con rol JEFE o ADMINISTRATIVO para hacer un ajuste manual de inventario
 */
public record StockAjusteRequest(
        @NotNull(message = "La cantidad actual es obligatoria")
        @Min(value = 0, message = "La cantidad actual no puede ser negativa")
        Integer cantidadActual,

        String observacion
) {
}

