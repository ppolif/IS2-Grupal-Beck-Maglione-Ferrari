package com.DulceMayor.backend.dtos.direccion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO de entrada para la creación y modificación de una provincia, conforme al schema
 * {@code ProvinciaRequest} de {@code 04-openapi.yaml}.
 */
public record ProvinciaRequest(
        @NotBlank(message = "El nombre de la provincia es obligatorio")
        @Size(max = 100, message = "El nombre de la provincia no puede superar los 100 caracteres")
        String nombre,

        @NotBlank(message = "El id del país es obligatorio")
        String paisId
) {
}

