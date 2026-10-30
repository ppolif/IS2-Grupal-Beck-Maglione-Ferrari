package com.DulceMayor.backend.dtos.direccion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO de entrada para la creación y modificación de un departamento, conforme al schema
 * {@code DepartamentoRequest} de {@code 04-openapi.yaml}.
 */
public record DepartamentoRequest(
        @NotBlank(message = "El nombre del departamento es obligatorio")
        @Size(max = 100, message = "El nombre del departamento no puede superar los 100 caracteres")
        String nombre,

        @NotBlank(message = "El id de la provincia es obligatorio")
        String provinciaId
) {
}

