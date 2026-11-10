package com.DulceMayor.backend.services.facturacion.strategy;

import com.DulceMayor.backend.dtos.facturacion.IniciarPagoQrResponse;
import com.DulceMayor.backend.entities.facturacion.TipoPago;
import com.DulceMayor.backend.entities.pedido.Pedido;
import org.springframework.stereotype.Component;

/**
 * Estrategia de cobro en Efectivo contra entrega (cobranza física en destino por el distribuidor).
 *
 * <p><b>Patrón GoF aplicado: Strategy (Comportamiento)</b></p>
 */
@Component
public class EfectivoContraEntregaStrategy implements PagoStrategy {

    @Override
    public TipoPago getTipoPago() {
        return TipoPago.EFECTIVO;
    }

    @Override
    public IniciarPagoQrResponse iniciarPago(Pedido pedido) {
        // En efectivo no expira por timer de checkout web
        pedido.setFechaExpiracionPago(null);
        return null;
    }
}
