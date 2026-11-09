package com.DulceMayor.backend.dtos.publico;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * Solicitud de armado de presupuesto mediante lenguaje natural en el chatbot público
 * según {@code 04-openapi.yaml}.
 */
public record ChatbotPresupuestoRequest(
        @NotBlank(message = "El mensaje es obligatorio")
        @Size(max = 1000, message = "El mensaje no puede superar los 1000 caracteres")
        String mensaje,

        @Valid
        @Size(max = 20, message = "El historial no puede superar los 20 mensajes")
        List<ChatbotMensaje> historial
) {
}

