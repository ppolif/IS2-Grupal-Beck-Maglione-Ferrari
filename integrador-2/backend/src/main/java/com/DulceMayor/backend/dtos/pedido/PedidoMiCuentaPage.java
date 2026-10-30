package com.DulceMayor.backend.dtos.pedido;

import java.util.List;

/**
 * Respuesta paginada para el listado de pedidos del portal del minorista ("Mi cuenta") según {@code 04-openapi.yaml}.
 */
public record PedidoMiCuentaPage(
        List<PedidoResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}

