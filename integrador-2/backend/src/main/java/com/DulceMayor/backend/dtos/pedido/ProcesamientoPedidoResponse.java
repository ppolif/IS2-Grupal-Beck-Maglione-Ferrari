package com.DulceMayor.backend.dtos.pedido;

import com.DulceMayor.backend.dtos.facturacion.FacturaResponse;
import java.util.List;

/**
 * DTO de salida con el resultado del procesamiento de un pedido ({@code POST /pedidos/{id}/procesar})
 * según {@code 04-openapi.yaml}.
 */
public record ProcesamientoPedidoResponse(
        PedidoResponse pedido,
        FacturaResponse factura,
        List<ItemSinStock> itemsSinStock
) {
}

