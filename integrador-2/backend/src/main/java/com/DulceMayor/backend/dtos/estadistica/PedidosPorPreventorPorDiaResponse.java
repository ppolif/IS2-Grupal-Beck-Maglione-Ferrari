package com.DulceMayor.backend.dtos.estadistica;

import java.util.List;

/**
 * Respuesta para el gráfico de barras de pedidos por preventor por día en un mes determinado
 * según {@code 04-openapi.yaml}.
 */
public record PedidosPorPreventorPorDiaResponse(
        Integer anio,
        Integer mes,
        List<SeriePreventorDia> series
) {
}

