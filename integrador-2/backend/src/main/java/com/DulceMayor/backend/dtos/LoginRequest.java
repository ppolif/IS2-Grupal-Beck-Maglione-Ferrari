package com.DulceMayor.backend.dtos;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO de entrada para la solicitud de inicio de sesión.
 */
public record LoginRequest(
        @NotBlank(message = "El nombre de usuario es obligatorio")
        String nombreUsuario,

        @NotBlank(message = "La clave es obligatoria")
        String clave
) {
}

