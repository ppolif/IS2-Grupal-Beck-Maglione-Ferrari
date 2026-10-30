package com.DulceMayor.backend.services.pedido;

import com.DulceMayor.backend.dtos.pedido.PedidoEstadoCambiadoEvent;
import com.DulceMayor.backend.entities.pedido.EstadoPedido;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Observador de eventos de cambio de estado de pedidos encargado de gestionar las notificaciones
 * salientes según {@code 03-arquitectura.md} (§4.3).
 *
 * <p><b>Patrón GoF aplicado: Observer (Comportamiento)</b></p>
 * <ul>
 *   <li><b>Participante GoF:</b> ConcreteObserver (Observador Concreto).</li>
 *   <li><b>Sujeto / Publisher:</b> {@link PedidoService} (emite {@link PedidoEstadoCambiadoEvent}).</li>
 *   <li><b>Implementación:</b> Mediante {@link EventListener}, reacciona cuando un pedido pasa a
 *       {@link EstadoPedido#FACTURADO} o {@link EstadoPedido#ENTREGADO} sin acoplar el servicio
 *       transaccional de pedidos a la infraestructura de correo electrónico.</li>
 * </ul>
 */
@Component
public class NotificacionPedidoListener {

    private static final Logger log = LoggerFactory.getLogger(NotificacionPedidoListener.class);

    @EventListener
    public void alCambiarEstadoPedido(PedidoEstadoCambiadoEvent event) {
        if (event == null) {
            return;
        }
        if (event.nuevoEstado() == EstadoPedido.FACTURADO || event.nuevoEstado() == EstadoPedido.ENTREGADO) {
            log.info(
                    "Notificación de pedido Nro. {} (id={}) en estado {}: {}",
                    event.numeroPedido(),
                    event.pedidoId(),
                    event.nuevoEstado(),
                    event.observacion()
            );
        }
    }
}

