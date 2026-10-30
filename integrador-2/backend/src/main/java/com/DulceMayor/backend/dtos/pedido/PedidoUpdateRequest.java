package com.DulceMayor.backend.dtos.pedido;

import java.util.List;

/**
 * DTO de entrada para modificar un pedido existente mientras permanece en estado {@code CARGADO}
 * según {@code 04-openapi.yaml}.
 */
public record PedidoUpdateRequest(
        String codigoPromocion,
        List<DetallePedidoRequest> detalles
) {
}

