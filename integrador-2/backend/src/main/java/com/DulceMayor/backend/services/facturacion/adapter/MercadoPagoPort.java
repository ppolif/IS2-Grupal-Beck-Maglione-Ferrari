package com.DulceMayor.backend.services.facturacion.adapter;

import java.util.Optional;

/**
 * Puerto (Target) para desacoplar el dominio de Dulce Mayor de la API REST de Mercado Pago.
 *
 * <p><b>Patrón GoF aplicado: Adapter (Estructural)</b></p>
 */
public interface MercadoPagoPort {

    /**
     * Genera una orden de pago dinámica en Mercado Pago (Orders API) para cobro con QR interoperable.
     *
     * @param externalReference Identificador único del pedido en nuestro sistema.
     * @param titulo Descripción breve del concepto de compra.
     * @param monto Importe total exacto en ARS (con descuentos y promociones aplicadas).
     * @param minutosExpiracion Tiempo de vigencia del QR dinámico (por defecto 15 min).
     * @return {@link MercadoPagoOrderResponseDto} con el código EMVCo interoperable generado.
     */
    MercadoPagoOrderResponseDto crearOrdenQrDinamica(
            String externalReference,
            String titulo,
            double monto,
            int minutosExpiracion
    );

    /**
     * Consulta el estado real y verificado de un pago en la API de Mercado Pago.
     *
     * @param paymentId Identificador de pago devuelto por Mercado Pago.
     * @return {@link Optional} con el detalle del pago si existe.
     */
    Optional<MercadoPagoPaymentDto> consultarPago(String paymentId);

    /**
     * Valida criptográficamente la cabecera x-signature de una notificación Webhook entrante.
     *
     * @param xSignature Encabezado "x-signature" que contiene ts y v1.
     * @param xRequestId Encabezado "x-request-id" único de la petición.
     * @param dataId Identificador de la entidad notificada (ej. data.id del pago).
     * @return {@code true} si la firma es válida y el timestamp no expiró.
     */
    boolean validarFirmaWebhook(String xSignature, String xRequestId, String dataId);
}
