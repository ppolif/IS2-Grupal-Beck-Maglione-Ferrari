package com.DulceMayor.backend.dtos.facturacion;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Payload deserializado de las notificaciones Webhook de Mercado Pago.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record MercadoPagoWebhookPayload(
        String action,
        @JsonProperty("api_version") String apiVersion,
        Data data,
        @JsonProperty("date_created") String dateCreated,
        Long id,
        @JsonProperty("live_mode") Boolean liveMode,
        String type,
        @JsonProperty("user_id") String userId
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Data(String id) {
    }
}
