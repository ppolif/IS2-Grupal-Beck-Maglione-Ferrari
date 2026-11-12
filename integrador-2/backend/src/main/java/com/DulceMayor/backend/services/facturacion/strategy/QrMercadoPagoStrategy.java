package com.DulceMayor.backend.services.facturacion.strategy;

import com.DulceMayor.backend.dtos.facturacion.IniciarPagoQrResponse;
import com.DulceMayor.backend.entities.facturacion.TipoPago;
import com.DulceMayor.backend.entities.pedido.Pedido;
import com.DulceMayor.backend.services.facturacion.QrCodeGeneratorHelper;
import com.DulceMayor.backend.services.facturacion.adapter.MercadoPagoOrderResponseDto;
import com.DulceMayor.backend.services.facturacion.adapter.MercadoPagoPort;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Estrategia de cobro mediante Billetera Virtual y Código QR interoperable de Mercado Pago.
 *
 * <p><b>Patrón GoF aplicado: Strategy (Comportamiento)</b></p>
 */
@Component
public class QrMercadoPagoStrategy implements PagoStrategy {

    private final MercadoPagoPort mercadoPagoPort;
    private final int minutosExpiracion;

    public QrMercadoPagoStrategy(
            MercadoPagoPort mercadoPagoPort,
            @Value("${app.mercadopago.qr-expiration-minutes:15}") int minutosExpiracion) {
        this.mercadoPagoPort = mercadoPagoPort;
        this.minutosExpiracion = minutosExpiracion > 0 ? minutosExpiracion : 15;
    }

    @Override
    public TipoPago getTipoPago() {
        return TipoPago.BILLETERA_VIRTUAL;
    }

    @Override
    public IniciarPagoQrResponse iniciarPago(Pedido pedido) {
        LocalDateTime expiracion = LocalDateTime.now().plusMinutes(minutosExpiracion);
        pedido.setFechaExpiracionPago(expiracion);

        String titulo = "Pedido #" + pedido.getNumeroPedido() + " - Dulce Mayor";
        MercadoPagoOrderResponseDto ordenMp = mercadoPagoPort.crearOrdenQrDinamica(
                pedido.getId(),
                titulo,
                pedido.getTotal(),
                minutosExpiracion
        );
        pedido.setQrData(ordenMp.qrData());

        String imagenBase64 = QrCodeGeneratorHelper.generarQrBase64(ordenMp.qrData());
        long segundosRestantes = ChronoUnit.SECONDS.between(LocalDateTime.now(), expiracion);

        return new IniciarPagoQrResponse(
                pedido.getId(),
                pedido.getNumeroPedido(),
                pedido.getTotal(),
                ordenMp.qrData(),
                imagenBase64,
                expiracion,
                Math.max(0, segundosRestantes)
        );
    }
}
