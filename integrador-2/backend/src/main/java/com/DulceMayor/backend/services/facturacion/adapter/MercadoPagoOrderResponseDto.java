package com.DulceMayor.backend.services.facturacion.adapter;

/**
 * DTO que encapsula la respuesta al generar una orden QR dinámica en Mercado Pago.
 */
public record MercadoPagoOrderResponseDto(
        String orderId,
        String qrData
) {
}
