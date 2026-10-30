package com.DulceMayor.backend.dtos.pedido;

import java.util.List;

/**
 * DTO de entrada para cargar un nuevo pedido según {@code 04-openapi.yaml}.
 * {@code preventorId} es opcional cuando lo carga un preventista (se toma del usuario autenticado)
 * y puede informarse explícitamente cuando lo carga un usuario con rol {@code JEFE}.
 */
public record PedidoRequest(
        String puntoDeVentaId,
        String preventorId,
        String codigoPromocion,
        List<DetallePedidoRequest> detalles
) {
}

