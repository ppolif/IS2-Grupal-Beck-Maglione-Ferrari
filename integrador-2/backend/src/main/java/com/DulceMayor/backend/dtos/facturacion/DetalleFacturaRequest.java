package com.DulceMayor.backend.dtos.facturacion;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO de entrada que representa un ítem al generar una factura.
 */
public record DetalleFacturaRequest(
        @NotBlank(message = "El id del producto es obligatorio")
        String productoId,

        @NotNull(message = "La cantidad es obligatoria")
        @Min(value = 1, message = "La cantidad debe ser al menos 1")
        Integer cantidad,

        Double precioUnitario
) {
}

