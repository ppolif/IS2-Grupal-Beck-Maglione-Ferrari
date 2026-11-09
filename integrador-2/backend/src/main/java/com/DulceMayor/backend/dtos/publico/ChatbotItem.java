package com.DulceMayor.backend.dtos.publico;

/**
 * Ítem presupuestado y valorizado por el backend con precios vigentes reales
 * según {@code 04-openapi.yaml}.
 */
public record ChatbotItem(
        String productoId,
        String productoNombre,
        String presentacion,
        Integer cantidad,
        Double precioUnitario,
        Double subtotal
) {
}

