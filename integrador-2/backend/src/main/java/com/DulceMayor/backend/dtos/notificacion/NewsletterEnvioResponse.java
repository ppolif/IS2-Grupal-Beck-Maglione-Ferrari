package com.DulceMayor.backend.dtos.notificacion;

/**
 * Respuesta devuelta tras el envío manual o programado del newsletter mensual de ofertas.
 *
 * <p>Mapea el esquema {@code NewsletterEnvioResponse} especificado en {@code docs/04-openapi.yaml}.</p>
 *
 * @param destinatarios Cantidad total de clientes minoristas a los que se les despachó el boletín.
 * @param productosEnOferta Cantidad de productos destacados en oferta incluidos en el correo.
 */
public record NewsletterEnvioResponse(
        int destinatarios,
        int productosEnOferta
) {
}
