package com.DulceMayor.backend.dtos.ruta;

/**
 * Solicitud enviada por el preventor al completar una visita en un punto de venta.
 * {@code pedidoId} es opcional: si el cliente compró, se envía; si no realizó compra, viaja null.
 */
public record CompletarParadaPreventorRequest(
        String puntoDeVentaId,
        String pedidoId
) {
}

