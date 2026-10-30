package com.DulceMayor.backend.dtos.direccion;

import com.DulceMayor.backend.dtos.Identificable;

/**
 * DTO de salida que representa un país activo, conforme al schema
 * {@code PaisResponse} de {@code 04-openapi.yaml}.
 */
public record PaisResponse(
        String id,
        String nombre
) implements Identificable {
}

