package com.DulceMayor.backend.dtos.ruta;

/**
 * Solicitud enviada por el distribuidor al entregar un pedido en destino.
 */
public record EntregaRequest(
        String pedidoId,
        String observacion
) {
}

