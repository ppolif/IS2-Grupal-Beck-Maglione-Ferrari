package com.DulceMayor.backend.dtos.direccion;

/**
 * DTO de salida que representa una dirección física con su contexto geográfico.
 */
public record DireccionResponse(
        String id,
        String calle,
        String numero,
        String manzanaPiso,
        String casaDepartamento,
        String localidadId,
        String localidadNombre,
        String departamentoId,
        String departamentoNombre,
        String provinciaNombre
) {
    public String numeracion() {
        return numero;
    }
}

