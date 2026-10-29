package com.DulceMayor.backend.dtos.facturacion;

import com.DulceMayor.backend.entities.facturacion.TipoPago;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO de entrada para la creación y modificación de una forma de pago según {@code 04-openapi.yaml}.
 */
public record FormaDePagoRequest(
        @NotNull(message = "El tipo de pago es obligatorio")
        TipoPago tipoPago,

        @Size(max = 255, message = "La observación no puede superar los 255 caracteres")
        String observacion
) {
}

