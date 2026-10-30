package com.DulceMayor.backend.dtos.pedido;

import com.DulceMayor.backend.dtos.Identificable;
import com.DulceMayor.backend.dtos.persona.EmpleadoResumen;
import com.DulceMayor.backend.dtos.persona.PuntoDeVentaResponse;
import com.DulceMayor.backend.entities.pedido.EstadoPedido;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de salida que representa un pedido con sus detalles, totales, comercio,
 * preventor/distribuidor asignados y el identificador de factura asociada
 * según {@code 04-openapi.yaml}.
 */
public record PedidoResponse(
        String id,
        Long numeroPedido,
        LocalDateTime fechaHoraCarga,
        EstadoPedido estado,
        PuntoDeVentaResponse puntoDeVenta,
        EmpleadoResumen preventor,
        EmpleadoResumen distribuidor,
        String codigoPromocion,
        Double subtotal,
        Double descuento,
        Double total,
        List<DetallePedidoResponse> detalles,
        String facturaId
) implements Identificable {
}
