package com.DulceMayor.backend.dtos.mayorista;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO de entrada para configurar el correo saliente SMTP de un {@code Mayorista},
 * según {@code 04-openapi.yaml}.
 */
public record ConfiguracionCorreoMayoristaRequest(
        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El formato del correo electrónico no es válido")
        @Size(max = 150, message = "El correo no puede superar los 150 caracteres")
        String correo,

        @NotBlank(message = "La clave SMTP es obligatoria")
        @Size(max = 255, message = "La clave no puede superar los 255 caracteres")
        String clave,

        @NotBlank(message = "El puerto es obligatorio")
        @Size(max = 10, message = "El puerto no puede superar los 10 caracteres")
        String puerto,

        @NotBlank(message = "El servidor SMTP es obligatorio")
        @Size(max = 100, message = "El servidor SMTP no puede superar los 100 caracteres")
        String smtp,

        @NotNull(message = "La configuración TLS es obligatoria")
        Boolean tls
) {
}

