package com.DulceMayor.backend.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO de entrada para la creación y modificación de un país, conforme al schema
 * {@code PaisRequest} de {@code 04-openapi.yaml}.
 */
public record PaisRequest(
        @NotBlank(message = "El nombre del país es obligatorio")
        @Size(max = 100, message = "El nombre del país no puede superar los 100 caracteres")
        String nombre
) {
}

