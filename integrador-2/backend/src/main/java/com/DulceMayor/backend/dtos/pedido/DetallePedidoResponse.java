package com.DulceMayor.backend.dtos.pedido;

import com.DulceMayor.backend.dtos.Identificable;

/**
 * DTO de salida para cada ítem de un pedido según {@code 04-openapi.yaml}.
 */
public record DetallePedidoResponse(
        String id,
        String productoId,
        String productoNombre,
        String presentacion,
        Integer cantidad,
        Double precioUnitario,
        Double subtotal
) implements Identificable {
}

