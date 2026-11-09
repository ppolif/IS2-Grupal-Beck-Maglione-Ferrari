package com.DulceMayor.backend.services.publico.adapter;

import com.DulceMayor.backend.dtos.publico.CotizacionDolarResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Adaptador concreto que consume la API pública de cotización del dólar, la traduce al contrato
 * {@link CotizacionDolarResponse} del dominio, mantiene una caché de corta duración y provee
 * un valor de respaldo si la API externa falla (sección 4.2 de {@code 03-arquitectura.md} y
 * sección 5.9 de {@code GENERAL.md}).
 *
 * <p><b>Patrón GoF aplicado: Adapter (Estructural)</b></p>
 * <ul>
 *   <li><b>Participante GoF:</b> Adapter (Adaptador concreto) que implementa {@link CotizacionDolarPort}.</li>
 *   <li><b>Adaptee (Adaptado):</b> Cliente HTTP ({@link HttpClient}) y respuesta JSON del proveedor externo
 *       de cotizaciones ({@code DolarApi}).</li>
 *   <li><b>Justificación:</b> Aísla el dominio de Dulce Mayor frente a cambios en el contrato externo,
 *       caídas de red o latencias, garantizando alta disponibilidad mediante caché y fallback.</li>
 * </ul>
 */
@Component
public class CotizacionDolarAdapter implements CotizacionDolarPort {

    private static final double COMPRA_RESPALDO_INICIAL = 1050.0;
    private static final double VENTA_RESPALDO_INICIAL = 1090.0;
    private static final String FUENTE_POR_DEFECTO = "DolarApi Oficial (BCRA)";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String apiUrl;
    private final boolean externalCallEnabled;
    private final long cacheTtlSeconds;

    private volatile CotizacionDolarResponse ultimoValorConocido;
    private volatile LocalDateTime expiraCacheEn;

    public CotizacionDolarAdapter(
            @Value("${app.dolar.api-url:https://dolarapi.com/v1/dolares/oficial}") String apiUrl,
            @Value("${app.dolar.enabled:true}") boolean externalCallEnabled,
            @Value("${app.dolar.cache-ttl-seconds:300}") long cacheTtlSeconds) {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
        this.objectMapper = new ObjectMapper();
        this.apiUrl = apiUrl;
        this.externalCallEnabled = externalCallEnabled;
        this.cacheTtlSeconds = cacheTtlSeconds;
        this.ultimoValorConocido = new CotizacionDolarResponse(
                "USD",
                COMPRA_RESPALDO_INICIAL,
                VENTA_RESPALDO_INICIAL,
                LocalDateTime.now(),
                FUENTE_POR_DEFECTO,
                true
        );
    }

    @Override
    public CotizacionDolarResponse obtenerCotizacionActual() {
        LocalDateTime ahora = LocalDateTime.now();
        CotizacionDolarResponse cacheActual = this.ultimoValorConocido;
        LocalDateTime vencimiento = this.expiraCacheEn;

        if (vencimiento != null && ahora.isBefore(vencimiento) && cacheActual != null && !cacheActual.esValorRespaldo()) {
            return cacheActual;
        }

        if (!externalCallEnabled) {
            return construirRespuestaRespaldo(cacheActual);
        }

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .timeout(Duration.ofSeconds(3))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300 && response.body() != null) {
                JsonNode root = objectMapper.readTree(response.body());
                double compra = root.path("compra").asDouble(COMPRA_RESPALDO_INICIAL);
                double venta = root.path("venta").asDouble(VENTA_RESPALDO_INICIAL);
                String moneda = root.path("moneda").asText("USD");
                String casa = root.path("nombre").asText("Oficial");

                if (compra > 0 && venta > 0) {
                    CotizacionDolarResponse fresca = new CotizacionDolarResponse(
                            moneda,
                            compra,
                            venta,
                            ahora,
                            "DolarApi - " + casa,
                            false
                    );
                    this.ultimoValorConocido = fresca;
                    this.expiraCacheEn = ahora.plusSeconds(cacheTtlSeconds);
                    return fresca;
                }
            }
        } catch (Exception ignored) {
            // Ante fallo de red, timeout o formato inesperado se devuelve el último valor de respaldo.
        }

        return construirRespuestaRespaldo(cacheActual);
    }

    private CotizacionDolarResponse construirRespuestaRespaldo(CotizacionDolarResponse base) {
        if (base == null) {
            return new CotizacionDolarResponse(
                    "USD",
                    COMPRA_RESPALDO_INICIAL,
                    VENTA_RESPALDO_INICIAL,
                    LocalDateTime.now(),
                    FUENTE_POR_DEFECTO,
                    true
            );
        }
        return new CotizacionDolarResponse(
                base.moneda(),
                base.compra(),
                base.venta(),
                base.fechaActualizacion(),
                base.fuente(),
                true
        );
    }
}

