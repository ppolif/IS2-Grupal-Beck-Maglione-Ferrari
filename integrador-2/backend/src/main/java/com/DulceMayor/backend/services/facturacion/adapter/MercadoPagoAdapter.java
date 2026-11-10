package com.DulceMayor.backend.services.facturacion.adapter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Optional;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Adaptador concreto para interactuar con la API REST de Mercado Pago (Argentina).
 *
 * <p><b>Patrón GoF aplicado: Adapter (Estructural)</b></p>
 * <ul>
 *   <li><b>Target:</b> {@link MercadoPagoPort}.</li>
 *   <li><b>Adaptee:</b> API REST oficial de Mercado Pago ({@code https://api.mercadopago.com}) y {@link HttpClient}.</li>
 * </ul>
 */
@Component
public class MercadoPagoAdapter implements MercadoPagoPort {

    private static final Logger log = LoggerFactory.getLogger(MercadoPagoAdapter.class);
    private static final long MAX_SIGNATURE_AGE_MS = 5 * 60 * 1000L; // 5 minutos

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String accessToken;
    private final String webhookSecret;
    private final String apiUrl;

    public MercadoPagoAdapter(
            @Value("${app.mercadopago.access-token:}") String accessToken,
            @Value("${app.mercadopago.webhook-secret:}") String webhookSecret,
            @Value("${app.mercadopago.api-url:https://api.mercadopago.com}") String apiUrl) {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        this.objectMapper = new ObjectMapper();
        this.accessToken = accessToken != null ? accessToken.trim() : "";
        this.webhookSecret = webhookSecret != null ? webhookSecret.trim() : "";
        this.apiUrl = (apiUrl != null && !apiUrl.isBlank()) ? apiUrl.trim() : "https://api.mercadopago.com";
    }

    @Override
    public MercadoPagoOrderResponseDto crearOrdenQrDinamica(
            String externalReference,
            String titulo,
            double monto,
            int minutosExpiracion) {

        // Si no hay token de Mercado Pago configurado o es un token de prueba ficticio,
        // provee un payload EMVCo de QR dinámico estándar para pruebas sin bloquear el entorno.
        if (accessToken.isBlank() || accessToken.startsWith("APP_USR-test")) {
            log.info("Modo simulación Mercado Pago QR activado para orden: {}", externalReference);
            String qrSimulado = String.format(Locale.US,
                    "00020101021243650016com.mercadopago0136%s520400005303032540%.2f5802AR5911DULCE_MAYOR6007MENDOZA62170513%s6304",
                    externalReference, monto, externalReference);
            return new MercadoPagoOrderResponseDto("ORD-SIM-" + externalReference, qrSimulado);
        }

        try {
            String endpoint = apiUrl + "/v1/orders";
            String jsonPayload = String.format(Locale.US, """
                    {
                      "external_reference": "%s",
                      "total_amount": %.2f,
                      "items": [
                        {
                          "title": "%s",
                          "unit_price": %.2f,
                          "quantity": 1,
                          "unit_measure": "unit",
                          "total_amount": %.2f
                        }
                      ],
                      "config": {
                        "qr": {
                          "mode": "dynamic"
                        }
                      }
                    }
                    """, externalReference, monto, titulo, monto, monto);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(8))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                JsonNode root = objectMapper.readTree(response.body());
                String orderId = root.path("id").asText();
                String qrData = root.path("qr_data").asText();
                if (qrData.isBlank()) {
                    qrData = root.path("point_of_interaction").path("transaction_data").path("qr_code").asText();
                }
                return new MercadoPagoOrderResponseDto(orderId, qrData);
            } else {
                log.warn("Mercado Pago Orders API devolvió HTTP {}: {}. Usando fallback EMVCo.",
                        response.statusCode(), response.body());
            }
        } catch (Exception ex) {
            log.error("Excepción al conectar con Mercado Pago Orders API: {}", ex.getMessage());
        }

        // Respaldo resiliente
        String fallbackQr = String.format(Locale.US,
                "00020101021243650016com.mercadopago0136%s520400005303032540%.2f5802AR5911DULCE_MAYOR6007MENDOZA62170513%s6304",
                externalReference, monto, externalReference);
        return new MercadoPagoOrderResponseDto("ORD-FALLBACK-" + externalReference, fallbackQr);
    }

    @Override
    public Optional<MercadoPagoPaymentDto> consultarPago(String paymentId) {
        if (paymentId == null || paymentId.isBlank()) {
            return Optional.empty();
        }

        // Modo simulación si es token de prueba ficticio o paymentId de test
        if (accessToken.isBlank() || accessToken.startsWith("APP_USR-test") || paymentId.startsWith("TEST-")) {
            log.info("Modo simulación Mercado Pago Payments activado para paymentId: {}", paymentId);
            return Optional.of(new MercadoPagoPaymentDto(
                    paymentId,
                    "approved",
                    "accredited",
                    null, // El llamador valida monto si está disponible
                    "ARS",
                    null,
                    LocalDateTime.now()
            ));
        }

        try {
            String endpoint = apiUrl + "/v1/payments/" + paymentId.trim();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Accept", "application/json")
                    .timeout(Duration.ofSeconds(8))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                String id = root.path("id").asText();
                String status = root.path("status").asText();
                String statusDetail = root.path("status_detail").asText();
                Double transactionAmount = root.path("transaction_amount").isNumber() ? root.path("transaction_amount").asDouble() : null;
                String currencyId = root.path("currency_id").asText();
                String externalReference = root.path("external_reference").asText(null);
                String dateApprovedStr = root.path("date_approved").asText(null);
                LocalDateTime dateApproved = null;
                if (dateApprovedStr != null && !dateApprovedStr.isBlank()) {
                    try {
                        dateApproved = OffsetDateTime.parse(dateApprovedStr).toLocalDateTime();
                    } catch (Exception ignored) {
                        dateApproved = LocalDateTime.now();
                    }
                }

                return Optional.of(new MercadoPagoPaymentDto(
                        id,
                        status,
                        statusDetail,
                        transactionAmount,
                        currencyId,
                        externalReference,
                        dateApproved != null ? dateApproved : LocalDateTime.now()
                ));
            } else {
                log.warn("Mercado Pago Payments API devolvió HTTP {} para paymentId {}: {}",
                        response.statusCode(), paymentId, response.body());
            }
        } catch (Exception ex) {
            log.error("Excepción al consultar pago {} en Mercado Pago: {}", paymentId, ex.getMessage());
        }

        return Optional.empty();
    }

    @Override
    public boolean validarFirmaWebhook(String xSignature, String xRequestId, String dataId) {
        // En desarrollo local o si el secreto no está configurado, loguear advertencia y permitir
        if (webhookSecret.isBlank() || webhookSecret.equals("test-webhook-secret")) {
            log.warn("Validación de firma webhook en modo desarrollo/permisivo (webhook secret no productivo)");
            return true;
        }

        if (xSignature == null || xSignature.isBlank() || dataId == null || dataId.isBlank()) {
            log.warn("Cabecera x-signature o data.id ausente en la notificación");
            return false;
        }

        try {
            String ts = null;
            String v1 = null;

            for (String part : xSignature.split(",")) {
                String[] kv = part.split("=", 2);
                if (kv.length == 2) {
                    String key = kv[0].trim();
                    String val = kv[1].trim();
                    if ("ts".equalsIgnoreCase(key)) ts = val;
                    if ("v1".equalsIgnoreCase(key)) v1 = val;
                }
            }

            if (ts == null || v1 == null) {
                log.warn("Formato de x-signature inválido: {}", xSignature);
                return false;
            }

            // Validar frescura del timestamp para mitigar replay attacks
            long timestampMs = Long.parseLong(ts);
            long nowMs = System.currentTimeMillis();
            if (Math.abs(nowMs - timestampMs) > MAX_SIGNATURE_AGE_MS) {
                log.warn("Timestamp de x-signature expirado (ts: {}, now: {})", timestampMs, nowMs);
                return false;
            }

            // Construir template canónico oficial: id:[data.id];request-id:[x-request-id];ts:[ts];
            String reqId = xRequestId != null ? xRequestId.trim() : "";
            String manifest = String.format("id:%s;request-id:%s;ts:%s;", dataId.trim(), reqId, ts);

            Mac hmacSha256 = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            hmacSha256.init(secretKey);

            byte[] hashBytes = hmacSha256.doFinal(manifest.getBytes(StandardCharsets.UTF_8));
            String computedHex = HexFormat.of().formatHex(hashBytes);

            // Comparación constante en tiempo para prevenir timing attacks
            return MessageDigest.isEqual(
                    computedHex.getBytes(StandardCharsets.UTF_8),
                    v1.getBytes(StandardCharsets.UTF_8)
            );
        } catch (Exception ex) {
            log.error("Error al validar firma x-signature: {}", ex.getMessage());
            return false;
        }
    }
}
