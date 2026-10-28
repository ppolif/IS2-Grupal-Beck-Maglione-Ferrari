package com.DulceMayor.backend.dtos.producto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO de entrada para la creación y modificación de una subcategoría, conforme al schema
 * {@code SubCategoriaRequest} de {@code 04-openapi.yaml}.
 */
public record SubCategoriaRequest(
        @NotBlank(message = "El nombre de la subcategoría es obligatorio")
        @Size(max = 100, message = "El nombre de la subcategoría no puede superar los 100 caracteres")
        String nombre,

        @NotBlank(message = "El id de la categoría es obligatorio")
        String categoriaId
) {
}

