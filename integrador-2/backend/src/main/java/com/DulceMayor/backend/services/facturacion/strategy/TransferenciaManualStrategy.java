package com.DulceMayor.backend.services.facturacion.strategy;

import com.DulceMayor.backend.dtos.facturacion.IniciarPagoQrResponse;
import com.DulceMayor.backend.entities.facturacion.TipoPago;
import com.DulceMayor.backend.entities.pedido.Pedido;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

/**
 * Estrategia de cobro mediante Transferencia Bancaria tradicional con verificación administrativa manual.
 *
 * <p><b>Patrón GoF aplicado: Strategy (Comportamiento)</b></p>
 */
@Component
public class TransferenciaManualStrategy implements PagoStrategy {

    private static final int PLAZO_HORAS_TRANSFERENCIA = 24;

    @Override
    public TipoPago getTipoPago() {
        return TipoPago.TRANSFERENCIA;
    }

    @Override
    public IniciarPagoQrResponse iniciarPago(Pedido pedido) {
        LocalDateTime expiracion = LocalDateTime.now().plusHours(PLAZO_HORAS_TRANSFERENCIA);
        pedido.setFechaExpiracionPago(expiracion);
        // Para transferencias no se genera código QR
        return null;
    }
}
