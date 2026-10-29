package com.DulceMayor.backend.dtos.facturacion;

import java.util.List;

/**
 * Respuesta paginada de facturas según el schema {@code FacturaPage} de {@code 04-openapi.yaml}.
 */
public record FacturaPage(
        List<FacturaResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}

