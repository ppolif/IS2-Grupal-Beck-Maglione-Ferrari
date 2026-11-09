package com.DulceMayor.backend.dtos.publico;

import java.util.List;

/**
 * DTO de página para el listado público de productos con stock según {@code 04-openapi.yaml}.
 */
public record ProductoPublicoPage(
        List<ProductoPublicoResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}

