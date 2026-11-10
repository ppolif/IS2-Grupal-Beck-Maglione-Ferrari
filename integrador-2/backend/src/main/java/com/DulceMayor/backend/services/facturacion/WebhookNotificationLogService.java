package com.DulceMayor.backend.services.facturacion;

import com.DulceMayor.backend.entities.facturacion.WebhookNotificationLog;
import com.DulceMayor.backend.repositories.facturacion.WebhookNotificationLogRepository;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de auditoría e idempotencia para eventos y notificaciones Webhook de pasarelas de pago.
 */
@Service
public class WebhookNotificationLogService {

    private final WebhookNotificationLogRepository webhookNotificationLogRepository;

    public WebhookNotificationLogService(WebhookNotificationLogRepository webhookNotificationLogRepository) {
        this.webhookNotificationLogRepository = webhookNotificationLogRepository;
    }

    @Transactional(readOnly = true)
    public boolean yaFueProcesado(String idTransaccionMp) {
        if (idTransaccionMp == null || idTransaccionMp.isBlank()) {
            return false;
        }
        return webhookNotificationLogRepository.existsByIdTransaccionMpAndEstadoProcesamiento(idTransaccionMp.trim(), "PROCESADO");
    }

    @Transactional
    public WebhookNotificationLog registrarRecibido(
            String idTransaccionMp,
            String requestId,
            String action,
            String topic,
            String xSignature,
            String payloadJson) {
        WebhookNotificationLog logEntry = WebhookNotificationLog.builder()
                .idTransaccionMp(idTransaccionMp)
                .requestId(requestId)
                .action(action)
                .topic(topic)
                .xSignature(xSignature)
                .payloadJson(payloadJson)
                .estadoProcesamiento("RECIBIDO")
                .fechaHora(LocalDateTime.now())
                .eliminado(false)
                .build();
        return webhookNotificationLogRepository.save(logEntry);
    }

    @Transactional
    public void marcarProcesado(WebhookNotificationLog logEntry) {
        if (logEntry != null) {
            logEntry.setEstadoProcesamiento("PROCESADO");
            webhookNotificationLogRepository.save(logEntry);
        }
    }

    @Transactional
    public void marcarFallido(WebhookNotificationLog logEntry, String mensajeError) {
        if (logEntry != null) {
            logEntry.setEstadoProcesamiento("FALLIDO");
            logEntry.setMensajeError(mensajeError);
            webhookNotificationLogRepository.save(logEntry);
        }
    }
}
