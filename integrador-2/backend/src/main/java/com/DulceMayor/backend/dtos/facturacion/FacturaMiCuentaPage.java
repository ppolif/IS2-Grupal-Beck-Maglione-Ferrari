package com.DulceMayor.backend.dtos.facturacion;

import java.util.List;

/**
 * Respuesta paginada de facturas del cliente minorista.
 */
public record FacturaMiCuentaPage(
        List<FacturaResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}

