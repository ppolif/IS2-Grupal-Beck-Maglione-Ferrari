package com.DulceMayor.backend.dtos;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * DTO de entrada para la creación y modificación de una promoción según 04-openapi.yaml.
 */
public record PromocionRequest(
        @NotBlank(message = "El código de la promoción es obligatorio")
        @Size(max = 50, message = "El código no puede superar los 50 caracteres")
        String codigo,

        @Size(max = 255, message = "La descripción no puede superar los 255 caracteres")
        String descripcion,

        @NotNull(message = "El porcentaje de descuento es obligatorio")
        @DecimalMin(value = "0.0", message = "El porcentaje de descuento no puede ser menor a 0")
        @DecimalMax(value = "100.0", message = "El porcentaje de descuento no puede ser mayor a 100")
        Double porcentajeDescuento,

        @PositiveOrZero(message = "La cantidad mínima debe ser mayor o igual a 0")
        Integer cantidadMinima,

        String productoId,

        @NotNull(message = "La fecha desde es obligatoria")
        LocalDate fechaDesde,

        @NotNull(message = "La fecha hasta es obligatoria")
        LocalDate fechaHasta
) {
}

