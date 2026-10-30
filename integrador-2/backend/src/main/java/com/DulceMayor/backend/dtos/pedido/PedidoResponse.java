package com.DulceMayor.backend.dtos.pedido;

import com.DulceMayor.backend.dtos.Identificable;
import com.DulceMayor.backend.entities.pedido.EstadoPedido;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de salida que representa un pedido con sus detalles, totales y el identificador
 * de factura asociada (si corresponde) según {@code 04-openapi.yaml}.
 * <p>
 * Nota: {@code puntoDeVentaId}, {@code preventorId} y {@code distribuidorId} exponen los identificadores
 * provisorios mientras se integran los módulos de Personas y Puntos de Venta.
 * </p>
 */
public record PedidoResponse(
        String id,
        Long numeroPedido,
        LocalDateTime fechaHoraCarga,
        EstadoPedido estado,
        String puntoDeVentaId,
        String preventorId,
        String distribuidorId,
        String codigoPromocion,
        Double subtotal,
        Double descuento,
        Double total,
        List<DetallePedidoResponse> detalles,
        String facturaId
) implements Identificable {
}

