package com.DulceMayor.backend.services.ruta;

import com.DulceMayor.backend.dtos.ruta.AvisoProximaEntregaEvent;
import com.DulceMayor.backend.services.mail.EmailService;
import com.DulceMayor.backend.services.mail.EmailTemplateHelper;
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
    private static final String ASUNTO_AVISO_ENTREGA = "En breve se realizará la entrega de su pedido Dulce Mayor";

    private final EmailService emailService;

    public AvisoProximaEntregaListener(EmailService emailService) {
        this.emailService = emailService;
    }

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

        String destinatario = event.emailDestinatario();

        if (destinatario == null || destinatario.isBlank()) {
            log.warn(
                    "No se pudo enviar notificación de próxima entrega para el pedido #{}: el cliente '{}' no posee un correo electrónico registrado.",
                    event.numeroPedido(),
                    event.nombreComercio()
            );
            return;
        }

        log.info(
                "Despachando notificación de próxima entrega al comercio '{}' ({}) - Pedido #{}",
                event.nombreComercio(),
                destinatario,
                event.numeroPedido()
        );

        String numeroPedido = event.numeroPedido() != null ? String.valueOf(event.numeroPedido()) : "";
        String cuerpoHtml = EmailTemplateHelper.generarHtmlAvisoEntrega(
                event.nombreComercio(),
                numeroPedido
        );

        emailService.enviarCorreoSimple(destinatario, ASUNTO_AVISO_ENTREGA, cuerpoHtml);
    }
}


