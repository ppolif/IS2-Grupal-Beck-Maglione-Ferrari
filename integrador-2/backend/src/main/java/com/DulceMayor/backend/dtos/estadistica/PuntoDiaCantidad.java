package com.DulceMayor.backend.dtos.estadistica;

/**
 * Punto diario (día del mes y cantidad de pedidos) dentro de la serie de un preventor
 * según {@code 04-openapi.yaml}.
 */
public record PuntoDiaCantidad(
        Integer dia,
        Integer cantidad
) {
}

