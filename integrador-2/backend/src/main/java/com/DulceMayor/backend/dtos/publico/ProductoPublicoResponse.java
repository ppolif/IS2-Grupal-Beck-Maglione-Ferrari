package com.DulceMayor.backend.dtos.publico;

import com.DulceMayor.backend.dtos.Identificable;

/**
 * DTO de salida para un producto en el catálogo público según {@code 04-openapi.yaml}.
 * <p>
 * No expone cantidades físicas de inventario ({@code stockActual}); únicamente se incluyen
 * en el catálogo público aquellos productos cuya existencia actual es mayor a 0.
 * </p>
 */
public record ProductoPublicoResponse(
        String id,
        String codigo,
        String nombre,
        String descripcion,
        String presentacion,
        Integer unidadesPorBulto,
        Boolean enOferta,
        String imagenId,
        String categoriaNombre,
        String subCategoriaNombre,
        Double precio
) implements Identificable {
}

