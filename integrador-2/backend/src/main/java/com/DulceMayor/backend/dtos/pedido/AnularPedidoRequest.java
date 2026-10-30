package com.DulceMayor.backend.dtos.pedido;

/**
 * DTO de entrada para la anulación administrativa de un pedido según {@code 04-openapi.yaml}.
 */
public record AnularPedidoRequest(
        String observacion
) {
}

