package com.DulceMayor.backend.dtos.producto;

import com.DulceMayor.backend.dtos.Identificable;

/**
 * DTO de salida que representa una subcategoría activa del catálogo, conforme al schema
 * {@code SubCategoriaResponse} de {@code 04-openapi.yaml}.
 */
public record SubCategoriaResponse(
        String id,
        String nombre,
        String categoriaId
) implements Identificable {
}

