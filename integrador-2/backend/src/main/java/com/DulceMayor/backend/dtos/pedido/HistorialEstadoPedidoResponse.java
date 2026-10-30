package com.DulceMayor.backend.dtos.pedido;

import com.DulceMayor.backend.dtos.Identificable;
import com.DulceMayor.backend.entities.pedido.EstadoPedido;
import java.time.LocalDateTime;

/**
 * DTO de salida para un registro de trazabilidad de {@code HistorialEstadoPedido} según {@code 04-openapi.yaml}.
 */
public record HistorialEstadoPedidoResponse(
        String id,
        EstadoPedido estado,
        LocalDateTime fechaHora,
        String usuario,
        String observacion
) implements Identificable {
}

