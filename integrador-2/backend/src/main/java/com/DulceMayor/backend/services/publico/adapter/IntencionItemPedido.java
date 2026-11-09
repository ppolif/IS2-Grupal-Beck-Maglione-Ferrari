package com.DulceMayor.backend.services.publico.adapter;

/**
 * Representa una intención de compra extraída del lenguaje natural (término de búsqueda del producto y cantidad).
 * No incluye precios ni cálculos monetarios, los cuales son responsabilidad exclusiva del backend.
 */
public record IntencionItemPedido(
        String terminoProducto,
        int cantidad
) {
}

