package com.DulceMayor.backend.dtos.pedido;

import java.util.List;

/**
 * Respuesta paginada para el listado administrativo/preventista de pedidos según {@code 04-openapi.yaml}.
 */
public record PedidoPage(
        List<PedidoResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}

