package com.DulceMayor.backend.dtos.pedido;

import com.DulceMayor.backend.entities.pedido.EstadoPedido;

/**
 * Evento de dominio publicado cuando un {@code Pedido} cambia de estado (por ejemplo a {@code FACTURADO}
 * o {@code ENTREGADO}) conforme a {@code 03-arquitectura.md} (§4.3).
 *
 * <p><b>Patrón GoF aplicado: Observer (Comportamiento)</b></p>
 * Representa el mensaje de notificación emitido por el sujeto ({@code PedidoService}) hacia los
 * observadores registrados mediante Spring Events.
 */
public record PedidoEstadoCambiadoEvent(
        String pedidoId,
        Long numeroPedido,
        EstadoPedido nuevoEstado,
        String usuario,
        String observacion
) {
}

