package com.DulceMayor.backend.services.facturacion.adapter;

import java.time.LocalDateTime;

/**
 * Representa los datos verificados de un pago consultado en la API oficial de Mercado Pago.
 */
public record MercadoPagoPaymentDto(
        String id,
        String status,
        String statusDetail,
        Double transactionAmount,
        String currencyId,
        String externalReference,
        LocalDateTime dateApproved
) {
}
