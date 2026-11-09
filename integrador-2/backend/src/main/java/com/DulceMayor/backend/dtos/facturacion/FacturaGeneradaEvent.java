package com.DulceMayor.backend.dtos.facturacion;

/**
 * Evento de aplicación emitido tras la generación exitosa de una factura en el sistema.
 *
 * <p><b>Regla de Arquitectura y Negocio:</b> {@code 02-reglas-negocio.md} (§8) y {@code 03-arquitectura.md} (§4.3).</p>
 * <p>Permite que los listeners de notificación preparen el comprobante en PDF de forma asíncrona
 * y lo envíen adjunto al correo electrónico del cliente sin bloquear la transacción principal.</p>
 *
 * @param facturaId Identificador único de la factura generada.
 * @param numeroFactura Número visible / folio del comprobante.
 * @param numeroPedido Número legible del pedido asociado.
 * @param emailDestinatario Dirección de correo del cliente minorista receptor.
 * @param total Importe total de la factura.
 */
public record FacturaGeneradaEvent(
        String facturaId,
        long numeroFactura,
        long numeroPedido,
        String emailDestinatario,
        double total
) {
}
