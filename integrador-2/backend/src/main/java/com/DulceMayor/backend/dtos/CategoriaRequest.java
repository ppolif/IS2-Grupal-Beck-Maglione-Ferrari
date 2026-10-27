package com.DulceMayor.backend.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO de entrada para la creación y modificación de una categoría, conforme al schema
 * {@code CategoriaRequest} de {@code 04-openapi.yaml}.
 *
 * <p><b>Patrón aplicado: DTO (Data Transfer Object).</b></p>
 * <p><b>Por qué se aplica acá:</b> Encapsula únicamente el dato editable por el cliente
 * ({@code nombre}) con sus restricciones sintácticas de Bean Validation, impidiendo que la
 * petición HTTP inyecte valores en campos internos de la entidad como {@code id} o {@code eliminado}.</p>
 */
public record CategoriaRequest(
        @NotBlank(message = "El nombre de la categoría es obligatorio")
        @Size(max = 100, message = "El nombre de la categoría no puede superar los 100 caracteres")
        String nombre
) {
}

