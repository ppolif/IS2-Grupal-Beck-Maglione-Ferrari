package com.DulceMayor.backend.dtos.direccion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO de entrada para la creación y modificación de una localidad, conforme al schema
 * {@code LocalidadRequest} de {@code 04-openapi.yaml}.
 */
public record LocalidadRequest(
        @NotBlank(message = "El nombre de la localidad es obligatorio")
        @Size(max = 100, message = "El nombre de la localidad no puede superar los 100 caracteres")
        String nombre,

        @NotBlank(message = "El código postal es obligatorio")
        @Size(max = 20, message = "El código postal no puede superar los 20 caracteres")
        String codigoPostal,

        @NotBlank(message = "El id del departamento es obligatorio")
        String departamentoId
) {
}

