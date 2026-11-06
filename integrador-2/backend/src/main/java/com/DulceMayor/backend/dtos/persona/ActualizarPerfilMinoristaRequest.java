package com.DulceMayor.backend.dtos.persona;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO de entrada para la actualización del perfil del cliente minorista autenticado
 * vía {@code PUT /api/v1/mi-cuenta/perfil}.
 * No incluye tipo de documento, número de documento ni fecha de nacimiento porque son datos inmutables.
 */
public record ActualizarPerfilMinoristaRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
        String nombre,

        @NotBlank(message = "El apellido es obligatorio")
        @Size(max = 100, message = "El apellido no puede superar los 100 caracteres")
        String apellido,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El formato del correo electrónico no es válido")
        @Size(max = 150, message = "El correo no puede superar los 150 caracteres")
        String email,

        @Size(max = 30, message = "El teléfono no puede superar los 30 caracteres")
        String telefono
) {
}

