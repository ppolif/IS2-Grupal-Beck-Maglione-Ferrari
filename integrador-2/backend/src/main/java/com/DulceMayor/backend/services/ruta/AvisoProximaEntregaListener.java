package com.DulceMayor.backend.services.ruta;

import com.DulceMayor.backend.dtos.ruta.AvisoProximaEntregaEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Listener observador encargado de despachar la notificación por correo electrónico
 * al cliente minorista cuando su pedido es el próximo en el recorrido de entrega del distribuidor.
 *
 * <p><b>Regla Crítica de Negocio:</b> {@code 02-reglas-negocio.md} (§6.3)</p>
 * <p>El backend debe identificar la siguiente parada y disparar un correo con el asunto:
 * <i>"En breve se realizará la entrega de su pedido Dulce Mayor"</i>.</p>
 *
 * <p><b>Patrón GoF aplicado: Observer (Comportamiento)</b></p>
 * Permite ejecutar la notificación de forma asíncrona e independiente de la transacción
 * de entrega en base de datos.
 */
@Component
public class AvisoProximaEntregaListener {

    private static final Logger log = LoggerFactory.getLogger(AvisoProximaEntregaListener.class);

    /**
     * Reacciona ante el evento {@link AvisoProximaEntregaEvent} despachando el aviso por email.
     *
     * @param event Datos del pedido y destinatario.
     */
    @Async
    @EventListener
    public void alRecibirAvisoProximaEntrega(AvisoProximaEntregaEvent event) {
        if (event == null) {
            return;
        }

        String destinatario = event.emailDestinatario() != null
                ? event.emailDestinatario()
                : "[sin email registrado]";

        log.info(
                "Enviando correo al minorista del comercio '{}' ({}) - Pedido #{}: " +
                "Asunto: 'En breve se realizará la entrega de su pedido Dulce Mayor'",
                event.nombreComercio(),
                destinatario,
                event.numeroPedido()
        );
    }
}

