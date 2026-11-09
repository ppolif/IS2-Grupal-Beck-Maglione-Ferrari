package com.DulceMayor.backend.dtos.estadistica;

import java.util.List;

/**
 * Serie de pedidos diarios de un preventor para el gráfico de barras mensual
 * según {@code 04-openapi.yaml}.
 */
public record SeriePreventorDia(
        String preventorId,
        String preventorNombre,
        List<PuntoDiaCantidad> puntos
) {
}

