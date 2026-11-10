package com.DulceMayor.backend.dtos.facturacion;

import com.DulceMayor.backend.entities.facturacion.TipoPago;

/**
 * Evento de dominio publicado tras la acreditación exitosa de un pago.
 */
public record PagoAprobadoEvent(
        String pagoId,
        String pedidoId,
        String facturaId,
        Double monto,
        TipoPago tipoPago,
        String referencia
) {
}
