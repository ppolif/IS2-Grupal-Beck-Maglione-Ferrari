package com.DulceMayor.backend.dtos.persona;

import java.util.List;

/**
 * Respuesta paginada para el listado de minoristas según {@code 04-openapi.yaml}.
 */
public record MinoristaPage(
        List<MinoristaResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}

