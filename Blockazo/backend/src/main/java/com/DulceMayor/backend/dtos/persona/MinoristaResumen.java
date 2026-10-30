package com.DulceMayor.backend.dtos.persona;

/**
 * Resumen de un cliente minorista utilizado en respuestas anidadas de pedidos y puntos de venta.
 */
public record MinoristaResumen(
        String id,
        String nombre,
        String apellido,
        String email
) {
}

