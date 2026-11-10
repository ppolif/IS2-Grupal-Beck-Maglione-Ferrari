package com.DulceMayor.backend.entities.facturacion;

import com.DulceMayor.backend.entities.EntidadBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Entidad de auditoría e idempotencia para eventos y notificaciones entrantes de pasarelas de pago (Mercado Pago).
 */
@Entity
@Table(name = "webhook_notification_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
public class WebhookNotificationLog extends EntidadBase {

    @Column(name = "id_transaccion_mp", length = 100)
    private String idTransaccionMp;

    @Column(name = "request_id", length = 150)
    private String requestId;

    @Column(length = 60)
    private String action;

    @Column(length = 60)
    private String topic;

    @Column(name = "payload_json", columnDefinition = "TEXT")
    private String payloadJson;

    @Column(name = "x_signature", length = 500)
    private String xSignature;

    @Builder.Default
    @Column(name = "estado_procesamiento", nullable = false, length = 40)
    private String estadoProcesamiento = "RECIBIDO";

    @Builder.Default
    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora = LocalDateTime.now();

    @Column(name = "mensaje_error", length = 1000)
    private String mensajeError;
}
