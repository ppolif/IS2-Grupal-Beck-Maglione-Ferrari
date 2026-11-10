package com.DulceMayor.backend.dtos.facturacion;

import java.time.LocalDateTime;

/**
 * Respuesta con los datos necesarios para renderizar y pagar con el código QR dinámico de Mercado Pago.
 */
public record IniciarPagoQrResponse(
        String pedidoId,
        Long numeroPedido,
        Double monto,
        String qrData,
        String qrImagenBase64,
        LocalDateTime fechaExpiracion,
        long tiempoRestanteSegundos
) {
    public String estadoPago() {
        return qrData;
    }

    public long expiraEnSegundos() {
        return tiempoRestanteSegundos;
    }
}
