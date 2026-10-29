package com.DulceMayor.backend.dtos.direccion;

import com.DulceMayor.backend.dtos.Identificable;

/**
 * DTO de salida que representa un departamento activo, conforme al schema
 * {@code DepartamentoResponse} de {@code 04-openapi.yaml}.
 */
public record DepartamentoResponse(
        String id,
        String nombre,
        String provinciaId
) implements Identificable {
}

