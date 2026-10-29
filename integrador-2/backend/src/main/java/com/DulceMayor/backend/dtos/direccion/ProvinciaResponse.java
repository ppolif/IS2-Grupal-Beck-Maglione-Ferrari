package com.DulceMayor.backend.dtos.direccion;

import com.DulceMayor.backend.dtos.Identificable;

/**
 * DTO de salida que representa una provincia activa, conforme al schema
 * {@code ProvinciaResponse} de {@code 04-openapi.yaml}.
 */
public record ProvinciaResponse(
        String id,
        String nombre,
        String paisId
) implements Identificable {
}

