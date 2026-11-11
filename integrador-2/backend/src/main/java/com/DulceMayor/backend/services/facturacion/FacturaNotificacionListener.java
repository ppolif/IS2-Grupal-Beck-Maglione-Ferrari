package com.DulceMayor.backend.services.facturacion;

import com.DulceMayor.backend.dtos.facturacion.FacturaGeneradaEvent;
import com.DulceMayor.backend.services.mail.EmailService;
import com.DulceMayor.backend.services.mail.EmailTemplateHelper;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Listener asíncrono observador encargado de despachar el correo con la factura PDF adjunta
 * cuando se emite una nueva factura en el sistema.
 *
 * <p><b>Regla de Arquitectura y Negocio:</b> {@code 02-reglas-negocio.md} (§8) y {@code 03-arquitectura.md} (§4.3).</p>
 * <p>Aplica el patrón <b>Observer</b> con {@link TransactionalEventListener} y {@link Async} para no demorar
 * ni comprometer la transacción principal de facturación si la conexión SMTP tarda o falla, asegurando
 * que el comprobante se despache únicamente tras el commit exitoso en base de datos.</p>
 */
@Component
public class FacturaNotificacionListener {

    private static final Logger log = LoggerFactory.getLogger(FacturaNotificacionListener.class);

    private final FacturaService facturaService;
    private final EmailService emailService;

    public FacturaNotificacionListener(FacturaService facturaService, EmailService emailService) {
        this.facturaService = facturaService;
        this.emailService = emailService;
    }

    /**
     * Reacciona ante {@link FacturaGeneradaEvent} despachando el comprobante PDF por email
     * una vez confirmada (commit) la transacción de persistencia de la factura.
     *
     * @param event Datos de la factura recién persistida.
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void alGenerarFactura(FacturaGeneradaEvent event) {
        if (event == null) {
            return;
        }

        if (event.emailDestinatario() == null || event.emailDestinatario().isBlank()) {
            log.info("Factura #{} emitida sin correo destinatario disponible. Se omite envío.", event.numeroFactura());
            return;
        }

        try {
            log.info(
                    "Despachando correo con factura adjunta #{}: Destinatario '{}', Pedido #{}",
                    event.numeroFactura(),
                    event.emailDestinatario(),
                    event.numeroPedido()
            );

            byte[] pdf = facturaService.generarFacturaPdf(event.facturaId());
            String asunto = "Factura #" + event.numeroFactura() + " - Dulce Mayor";
            String html = EmailTemplateHelper.generarHtmlFactura(
                    String.valueOf(event.numeroPedido()),
                    String.valueOf(event.numeroFactura()),
                    String.format(Locale.US, "%.2f", event.total())
            );

            emailService.enviarCorreoConAdjunto(
                    event.emailDestinatario(),
                    asunto,
                    html,
                    pdf,
                    "factura-" + event.numeroFactura() + ".pdf",
                    "application/pdf"
            );

        } catch (Exception e) {
            log.error("Fallo al enviar correo con factura #{} a '{}': {}",
                    event.numeroFactura(), event.emailDestinatario(), e.getMessage(), e);
        }
    }
}
