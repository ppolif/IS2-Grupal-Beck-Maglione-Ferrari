package com.DulceMayor.backend.services.publico.adapter;

import com.DulceMayor.backend.dtos.publico.ChatbotMensaje;
import java.util.List;

/**
 * Interfaz de dominio (Target del patrón Adapter) para extraer intenciones estructuradas de productos
 * y cantidades a partir de un mensaje en lenguaje natural.
 *
 * <p><b>Patrón GoF aplicado: Adapter (Estructural)</b></p>
 * <ul>
 *   <li><b>Participante GoF:</b> Target (Interfaz objetivo esperada por el dominio).</li>
 *   <li><b>Justificación:</b> Aísla la lógica de presupuestación del SDK específico de inteligencia artificial
 *       (Spring AI / Google Gemini), impidiendo además que el modelo invente precios o existencias.</li>
 * </ul>
 */
public interface ChatbotIntencionPort {

    /**
     * Analiza el mensaje del usuario y extrae la lista de productos mencionados junto con sus cantidades.
     */
    List<IntencionItemPedido> extraerIntenciones(String mensaje, List<ChatbotMensaje> historial);
}

