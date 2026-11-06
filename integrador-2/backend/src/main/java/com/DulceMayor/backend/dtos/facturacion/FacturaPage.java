package com.DulceMayor.backend.dtos.facturacion;

import java.util.List;

/**
 * Respuesta paginada de facturas desde la vista del admin.
 */
public record FacturaPage(
        List<FacturaResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}

