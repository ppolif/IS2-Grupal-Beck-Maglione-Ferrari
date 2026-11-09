package com.DulceMayor.backend.dtos.estadistica;

/**
 * Ítem del gráfico de torta con la cantidad actual en stock por producto
 * según {@code 04-openapi.yaml}.
 */
public record StockPorProductoItem(
        String productoId,
        String productoNombre,
        Integer cantidad
) {
}

