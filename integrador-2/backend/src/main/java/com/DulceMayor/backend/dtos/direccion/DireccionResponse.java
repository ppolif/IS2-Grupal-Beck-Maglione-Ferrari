package com.DulceMayor.backend.dtos.direccion;

/**
 * DTO de salida que representa una dirección física con su contexto geográfico, conforme al schema
 * {@code DireccionResponse} de {@code 04-openapi.yaml}.
 */
public record DireccionResponse(
        String id,
        String calle,
        String numeracion,
        String barrio,
        String manzanaPiso,
        String casaDepartamento,
        String referencia,
        Double latitud,
        Double longitud,
        String localidadId,
        String localidadNombre,
        String departamentoId,
        String departamentoNombre,
        String provinciaNombre
) {
}

