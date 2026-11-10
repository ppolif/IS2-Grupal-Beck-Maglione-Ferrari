package com.DulceMayor.backend.dtos.facturacion;

/**
 * Evento de dominio emitido al rechazarse o expirar un cobro.
 */
public record PagoRechazadoEvent(
        String pedidoId,
        String motivo
) {
}
