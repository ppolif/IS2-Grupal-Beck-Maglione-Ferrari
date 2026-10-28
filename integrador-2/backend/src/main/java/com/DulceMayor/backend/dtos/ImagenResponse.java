package com.DulceMayor.backend.dtos;

import com.DulceMayor.backend.entities.TipoImagen;

/**
 * DTO de salida para metadatos de una imagen guardada, conforme a 04-openapi.yaml.
 */
public record ImagenResponse(
        String id,
        String nombre,
        String mime,
        TipoImagen tipoImagen
) {
}

