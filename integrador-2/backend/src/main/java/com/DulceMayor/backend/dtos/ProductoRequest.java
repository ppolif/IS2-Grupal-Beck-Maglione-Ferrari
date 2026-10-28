package com.DulceMayor.backend.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * DTO de entrada para la creación y modificación de un producto según 04-openapi.yaml.
 */
public record ProductoRequest(
        @NotBlank(message = "El código del producto es obligatorio")
        @Size(max = 50, message = "El código no puede superar los 50 caracteres")
        String codigo,

        @NotBlank(message = "El nombre del producto es obligatorio")
        @Size(max = 150, message = "El nombre no puede superar los 150 caracteres")
        String nombre,

        @Size(max = 500, message = "La descripción no puede superar los 500 caracteres")
        String descripcion,

        @NotBlank(message = "La presentación es obligatoria")
        @Size(max = 100, message = "La presentación no puede superar los 100 caracteres")
        String presentacion,

        @NotNull(message = "Las unidades por bulto son obligatorias")
        @Min(value = 1, message = "Las unidades por bulto deben ser al menos 1")
        Integer unidadesPorBulto,

        Boolean enOferta,

        String imagenId,

        @NotBlank(message = "El id de la subcategoría es obligatorio")
        String subCategoriaId,

        @Positive(message = "El precio inicial debe ser un número positivo")
        Double precioInicial,

        @Min(value = 0, message = "El stock inicial no puede ser negativo")
        Integer stockInicial
) {
}

