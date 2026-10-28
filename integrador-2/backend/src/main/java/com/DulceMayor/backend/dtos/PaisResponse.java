package com.DulceMayor.backend.dtos;

/**
 * DTO de salida que representa un país activo, conforme al schema
 * {@code PaisResponse} de {@code 04-openapi.yaml}.
 */
public record PaisResponse(
        String id,
        String nombre
) {
}

