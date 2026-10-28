package com.DulceMayor.backend.dtos;

import java.util.List;

/**
 * DTO de paginación de productos conforme al schema ProductoPage de 04-openapi.yaml.
 */
public record ProductoPage(
        List<ProductoResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}

