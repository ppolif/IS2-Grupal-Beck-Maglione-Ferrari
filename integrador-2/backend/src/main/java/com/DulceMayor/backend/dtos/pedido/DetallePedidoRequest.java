package com.DulceMayor.backend.dtos.pedido;

/**
 * DTO de entrada para cada ítem de un pedido según {@code 04-openapi.yaml}.
 * El cliente nunca envía precios: el backend consulta la vigencia abierta del producto.
 */
public record DetallePedidoRequest(
        String productoId,
        Integer cantidad
) {
}

