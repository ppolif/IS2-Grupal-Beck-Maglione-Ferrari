package com.DulceMayor.backend.dtos.facturacion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Solicitud del distribuidor o personal administrativo para registrar el cobro en efectivo de un pedido.
 */
public record RegistrarCobroEfectivoRequest(
        @NotBlank(message = "El identificador del pedido es obligatorio")
        String pedidoId,

        @NotNull(message = "El monto cobrado es obligatorio")
        @Positive(message = "El monto cobrado debe ser mayor a cero")
        Double monto,

        @Size(max = 500, message = "La observación no puede superar los 500 caracteres")
        String observacion
) {
}
