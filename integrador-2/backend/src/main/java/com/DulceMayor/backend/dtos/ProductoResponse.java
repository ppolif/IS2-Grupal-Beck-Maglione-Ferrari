package com.DulceMayor.backend.dtos;

/**
 * DTO de salida que representa un producto del catálogo mayorista según 04-openapi.yaml.
 */
public record ProductoResponse(
        String id,
        String codigo,
        String nombre,
        String descripcion,
        String presentacion,
        Integer unidadesPorBulto,
        Boolean enOferta,
        String imagenId,
        String subCategoriaId,
        String subCategoriaNombre,
        String categoriaId,
        String categoriaNombre,
        Double precioVigente,
        Integer stockActual
) {
}

