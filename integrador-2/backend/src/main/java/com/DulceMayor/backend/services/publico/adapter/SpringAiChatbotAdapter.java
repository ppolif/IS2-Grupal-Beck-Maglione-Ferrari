package com.DulceMayor.backend.services.publico.adapter;

import com.DulceMayor.backend.dtos.publico.ChatbotMensaje;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Adaptador concreto que integra el modelo de lenguaje de Spring AI ({@link ChatModel})
 * para traducir un pedido en lenguaje natural a una lista de {@link IntencionItemPedido}
 * (sección 7 de {@code 02-reglas-negocio.md} y sección 4.2 de {@code 03-arquitectura.md}).
 *
 * <p><b>Patrón GoF aplicado: Adapter (Estructural)</b></p>
 * <ul>
 *   <li><b>Participante GoF:</b> Adapter (Adaptador concreto) que implementa {@link ChatbotIntencionPort}.</li>
 *   <li><b>Adaptee (Adaptado):</b> {@link ChatModel} de Spring AI (Google GenAI / Gemini).</li>
 *   <li><b>Justificación:</b> Traduce las respuestas textuales/JSON del LLM a estructuras tipadas del dominio
 *       de Dulce Mayor y provee un intérprete léxico de respaldo cuando la API externa de IA no está disponible,
 *       asegurando que la IA jamás calcule importes ni confirme stock ficticio.</li>
 * </ul>
 */
@Component
public class SpringAiChatbotAdapter implements ChatbotIntencionPort {

    private static final String PROMPT_SISTEMA = """
            Sos un extractor de entidades para la distribuidora mayorista de golosinas Dulce Mayor.
            Tu única tarea es extraer los productos y las cantidades numéricas solicitadas por el cliente.
            NO calcules precios, NO inventes stock.
            Respondé ÚNICAMENTE con un arreglo JSON válido con el formato exacto:
            [{"producto": "nombre del producto", "cantidad": 2}]
            Mensaje del cliente:
            """;

    private static final Map<String, Integer> NUMEROS_TEXTO = Map.ofEntries(
            Map.entry("un", 1),
            Map.entry("una", 1),
            Map.entry("uno", 1),
            Map.entry("dos", 2),
            Map.entry("tres", 3),
            Map.entry("cuatro", 4),
            Map.entry("cinco", 5),
            Map.entry("seis", 6),
            Map.entry("siete", 7),
            Map.entry("ocho", 8),
            Map.entry("nueve", 9),
            Map.entry("diez", 10),
            Map.entry("once", 11),
            Map.entry("doce", 12),
            Map.entry("trece", 13),
            Map.entry("catorce", 14),
            Map.entry("quince", 15),
            Map.entry("veinte", 20),
            Map.entry("veinticinco", 25),
            Map.entry("treinta", 30),
            Map.entry("cuarenta", 40),
            Map.entry("cincuenta", 50),
            Map.entry("cien", 100)
    );

    private static final Pattern PATRON_CANTIDAD_AL_INICIO = Pattern.compile(
            "(?i)^(\\d+|un|una|uno|dos|tres|cuatro|cinco|seis|siete|ocho|nueve|diez|once|doce|trece|catorce|quince|veinte|veinticinco|treinta|cuarenta|cincuenta|cien)\\b\\s+"
                    + "(?:(?:cajas?|bultos?|packs?|unidades?|bolsas?|displays?|paquetes?)\\s+(?:de\\s+)?)?([a-záéíóúñ0-9\\s-]{2,80})$"
    );

    private static final Pattern PATRON_CANTIDAD_AL_FINAL = Pattern.compile(
            "(?i)^([a-záéíóúñ0-9\\s-]{2,80}?)\\s*(?:x|\\*)?\\s*(\\d+)$"
    );

    private final Optional<ChatModel> chatModel;
    private final ObjectMapper objectMapper;
    private final String apiKey;

    public SpringAiChatbotAdapter(
            Optional<ChatModel> chatModel,
            @Value("${spring.ai.google.genai.api-key:pendiente-configurar}") String apiKey) {
        this.chatModel = chatModel;
        this.objectMapper = new ObjectMapper();
        this.apiKey = apiKey;
    }

    @Override
    public List<IntencionItemPedido> extraerIntenciones(String mensaje, List<ChatbotMensaje> historial) {
        if (mensaje == null || mensaje.isBlank()) {
            return List.of();
        }

        if (puedeUsarModeloExterno()) {
            try {
                String respuestaRaw = chatModel.get().call(PROMPT_SISTEMA + mensaje.trim());
                List<IntencionItemPedido> desdeJson = parsearRespuestaJson(respuestaRaw);
                if (!desdeJson.isEmpty()) {
                    return desdeJson;
                }
            } catch (Exception ignored) {
                // Si la API externa de IA falla o no hay red, se recurre al extractor NLP determinístico local.
            }
        }

        return extraerConNlpLocal(mensaje);
    }

    private boolean puedeUsarModeloExterno() {
        return chatModel.isPresent()
                && apiKey != null
                && !apiKey.isBlank()
                && !"pendiente-configurar".equalsIgnoreCase(apiKey.trim())
                && !"dummy-test-key".equalsIgnoreCase(apiKey.trim())
                && !"reemplazar_con_api_key_de_gemini".equalsIgnoreCase(apiKey.trim());
    }

    List<IntencionItemPedido> parsearRespuestaJson(String respuestaRaw) {
        if (respuestaRaw == null || respuestaRaw.isBlank()) {
            return List.of();
        }
        try {
            int inicio = respuestaRaw.indexOf('[');
            int fin = respuestaRaw.lastIndexOf(']');
            if (inicio < 0 || fin <= inicio) {
                return List.of();
            }
            String jsonLimpio = respuestaRaw.substring(inicio, fin + 1);
            JsonNode arrayNode = objectMapper.readTree(jsonLimpio);
            if (!arrayNode.isArray()) {
                return List.of();
            }
            List<IntencionItemPedido> resultado = new ArrayList<>();
            for (JsonNode item : arrayNode) {
                String prod = item.path("producto").asText("").trim();
                int cant = item.path("cantidad").asInt(1);
                if (!prod.isEmpty() && cant > 0) {
                    resultado.add(new IntencionItemPedido(limpiarTerminoProducto(prod), cant));
                }
            }
            return resultado;
        } catch (Exception e) {
            return List.of();
        }
    }

    List<IntencionItemPedido> extraerConNlpLocal(String mensaje) {
        List<IntencionItemPedido> items = new ArrayList<>();
        String normalizado = mensaje
                .replaceAll("(?i)\\b(hola|buenos\\s+d[ií]as|buenas\\s+tardes|buenas\\s+noches|quiero|quisiera|necesito|me\\s+das|me\\s+pas[aá]s|me\\s+arm[aá]s|hacer\\s+un\\s+pedido\\s+de|un\\s+pedido\\s+de|pedido\\s+de|un\\s+presupuesto\\s+de|presupuesto\\s+de|cotizar|cotizame|pedir|comprar|encargar|dame|por\\s+favor|gracias)\\b", " ")
                .replaceAll("[.,;!?\\n]+", " , ");

        String[] fragmentos = normalizado.split("(?i)\\s+(?:y|e|mas|más|tambien|también|ademas|además)\\s+|\\s*,\\s*");
        for (String fragmento : fragmentos) {
            String fragLimpio = fragmento.replaceAll("\\s+", " ").trim();
            if (fragLimpio.isEmpty()) {
                continue;
            }
            Matcher matcherInicio = PATRON_CANTIDAD_AL_INICIO.matcher(fragLimpio);
            if (matcherInicio.find()) {
                int cantidad = convertirCantidad(matcherInicio.group(1));
                String termino = limpiarTerminoProducto(matcherInicio.group(2));
                if (!termino.isBlank() && cantidad > 0) {
                    items.add(new IntencionItemPedido(termino, cantidad));
                }
                continue;
            }

            Matcher matcherFinal = PATRON_CANTIDAD_AL_FINAL.matcher(fragLimpio);
            if (matcherFinal.find()) {
                String termino = limpiarTerminoProducto(matcherFinal.group(1));
                int cantidad = convertirCantidad(matcherFinal.group(2));
                if (!termino.isBlank() && cantidad > 0) {
                    items.add(new IntencionItemPedido(termino, cantidad));
                }
                continue;
            }

            String termino = limpiarTerminoProducto(fragLimpio);
            if (termino.length() >= 3) {
                items.add(new IntencionItemPedido(termino, 1));
            }
        }
        return items;
    }

    private int convertirCantidad(String token) {
        if (token == null || token.isBlank()) {
            return 1;
        }
        String lower = token.trim().toLowerCase(Locale.ROOT);
        if (NUMEROS_TEXTO.containsKey(lower)) {
            return NUMEROS_TEXTO.get(lower);
        }
        try {
            return Math.max(1, Integer.parseInt(lower));
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private String limpiarTerminoProducto(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.replaceAll("(?i)^(?:cajas?|bultos?|packs?|unidades?|bolsas?|displays?|paquetes?)\\s+de\\s+", "")
                .replaceAll("(?i)^de\\s+", "")
                .replaceAll("\\s+", " ")
                .trim();
    }
}

