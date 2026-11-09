package com.DulceMayor.backend.dtos.publico;

import java.util.List;

/**
 * Respuesta estructurada del chatbot de presupuestos según {@code 04-openapi.yaml}.
 * <p>
 * El modelo de lenguaje únicamente extrae intenciones de productos y cantidades;
 * los precios unitarios, subtotales, el total general y las advertencias de stock
 * son calculados exclusivamente por el backend.
 * </p>
 */
public record ChatbotPresupuestoResponse(
        String respuesta,
        List<ChatbotItem> items,
        Double total,
        List<String> advertencias
) {
}

