package com.DulceMayor.backend.dtos.pedido;

/**
 * DTO de salida que informa un ítem con stock insuficiente al intentar procesar un pedido
 * según {@code 04-openapi.yaml}.
 */
public record ItemSinStock(
        String productoId,
        String productoNombre,
        Integer solicitado,
        Integer disponible
) {
}

