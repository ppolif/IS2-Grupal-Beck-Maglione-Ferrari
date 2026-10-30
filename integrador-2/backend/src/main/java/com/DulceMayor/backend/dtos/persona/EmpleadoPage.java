package com.DulceMayor.backend.dtos.persona;

import java.util.List;

/**
 * Respuesta paginada para el listado de empleados según {@code 04-openapi.yaml}.
 */
public record EmpleadoPage(
        List<EmpleadoResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}

