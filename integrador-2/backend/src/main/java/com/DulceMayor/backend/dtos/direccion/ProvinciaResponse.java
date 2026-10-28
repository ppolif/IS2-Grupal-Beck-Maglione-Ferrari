package com.DulceMayor.backend.dtos.direccion;

/**
 * DTO de salida que representa una provincia activa, conforme al schema
 * {@code ProvinciaResponse} de {@code 04-openapi.yaml}.
 */
public record ProvinciaResponse(
        String id,
        String nombre,
        String paisId
) {
}

