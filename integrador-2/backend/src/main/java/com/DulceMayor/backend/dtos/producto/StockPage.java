package com.DulceMayor.backend.dtos.producto;

import java.util.List;

/**
 * DTO de paginación de stock conforme a 04-openapi.yaml.
 */
public record StockPage(
        List<StockResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}

