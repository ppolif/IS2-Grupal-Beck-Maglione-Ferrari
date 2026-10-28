package com.DulceMayor.backend.dtos.direccion;

/**
 * DTO de salida que representa una localidad activa, conforme al schema
 * {@code LocalidadResponse} de {@code 04-openapi.yaml}.
 */
public record LocalidadResponse(
        String id,
        String nombre,
        String codigoPostal,
        String departamentoId
) {
}

