package com.DulceMayor.backend.dtos.facturacion;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO de entrada para registrar un pago sobre una factura según {@code 04-openapi.yaml}.
 */
public record PagoRequest(
        @NotBlank(message = "El id de la forma de pago es obligatorio")
        String formaDePagoId,

        @NotNull(message = "El monto es obligatorio")
        @DecimalMin(value = "0.01", message = "El monto debe ser al menos 0.01")
        Double monto,

        @Size(max = 255, message = "La referencia no puede superar los 255 caracteres")
        String referencia,

        @Size(max = 500, message = "La observación no puede superar los 500 caracteres")
        String observacion
) {
}

