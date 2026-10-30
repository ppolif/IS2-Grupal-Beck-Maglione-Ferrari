package com.DulceMayor.backend.dtos.persona;

import java.util.List;

/**
 * Respuesta paginada para el listado de puntos de venta según {@code 04-openapi.yaml}.
 */
public record PuntoDeVentaPage(
        List<PuntoDeVentaResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}

