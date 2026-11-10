package com.DulceMayor.backend.dtos.facturacion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Solicitud del cliente minorista informando la realización de una transferencia bancaria.
 */
public record InformarTransferenciaRequest(
        @NotBlank(message = "El número de comprobante u operación es obligatorio")
        @Size(max = 100, message = "El comprobante no puede superar los 100 caracteres")
        String comprobante,

        @Size(max = 100, message = "El banco emisor no puede superar los 100 caracteres")
        String bancoEmisor,

        @Size(max = 500, message = "La observación no puede superar los 500 caracteres")
        String observacion
) {
}
