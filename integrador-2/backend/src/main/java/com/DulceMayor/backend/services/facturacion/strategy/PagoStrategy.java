package com.DulceMayor.backend.services.facturacion.strategy;

import com.DulceMayor.backend.dtos.facturacion.IniciarPagoQrResponse;
import com.DulceMayor.backend.entities.facturacion.TipoPago;
import com.DulceMayor.backend.entities.pedido.Pedido;

/**
 * Estrategia común para la inicialización y procesamiento de los distintos medios de pago soportados.
 *
 * <p><b>Patrón GoF aplicado: Strategy (Comportamiento)</b></p>
 */
public interface PagoStrategy {

    /**
     * Identificador del medio de pago que implementa esta estrategia.
     */
    TipoPago getTipoPago();

    /**
     * Inicia o prepara el proceso de pago para un pedido específico.
     *
     * @param pedido Entidad del pedido sobre el cual se inicia la operación.
     * @return {@link IniciarPagoQrResponse} si el medio genera datos interactivos (como QR), o {@code null}.
     */
    IniciarPagoQrResponse iniciarPago(Pedido pedido);
}
