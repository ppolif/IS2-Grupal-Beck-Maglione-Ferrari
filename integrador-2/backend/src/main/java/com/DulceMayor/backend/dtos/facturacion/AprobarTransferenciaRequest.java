package com.DulceMayor.backend.dtos.facturacion;

import jakarta.validation.constraints.Size;

/**
 * Solicitud del personal administrativo para confirmar y aprobar una transferencia verificada.
 */
public record AprobarTransferenciaRequest(
        @Size(max = 500, message = "La observación no puede superar los 500 caracteres")
        String observacion
) {
}
