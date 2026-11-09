package com.DulceMayor.backend.dtos.publico;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Mensaje individual del historial de conversación con el chatbot según {@code 04-openapi.yaml}.
 */
public record ChatbotMensaje(
        @NotBlank(message = "El rol del mensaje es obligatorio")
        @Pattern(regexp = "usuario|asistente", message = "El rol debe ser 'usuario' o 'asistente'")
        String rol,

        @NotBlank(message = "El texto del mensaje es obligatorio")
        String texto
) {
}

