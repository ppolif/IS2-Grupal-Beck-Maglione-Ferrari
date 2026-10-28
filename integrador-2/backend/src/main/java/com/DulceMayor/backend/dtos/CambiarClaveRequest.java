package com.DulceMayor.backend.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO de entrada para el cambio de contraseña del usuario autenticado.
 */
public record CambiarClaveRequest(
        @NotBlank(message = "La clave actual es obligatoria")
        String claveActual,

        @NotBlank(message = "La nueva clave es obligatoria")
        @Size(min = 8, message = "La nueva clave debe tener al menos 8 caracteres")
        String nuevaClave,

        @NotBlank(message = "La confirmación de la clave es obligatoria")
        String confirmarClave
) {
}

