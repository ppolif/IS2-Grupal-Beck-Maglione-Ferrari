package com.DulceMayor.backend.dtos.ruta;

/**
 * Evento publicado cuando un distribuidor entrega un pedido y existe un próximo cliente
 * en el recorrido que debe ser avisado de la llegada inminente según {@code 02-reglas-negocio.md} (§6.3).
 *
 * <p><b>Patrón GoF aplicado: Observer (Comportamiento)</b></p>
 */
public record AvisoProximaEntregaEvent(
        String pedidoId,
        Long numeroPedido,
        String puntoDeVentaId,
        String nombreComercio,
        String emailDestinatario
) {
}

