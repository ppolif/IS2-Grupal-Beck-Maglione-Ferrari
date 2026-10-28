package com.example.carrito.servicios.compra.impl;

import com.example.carrito.dtos.ComprobantePago;
import com.example.carrito.dtos.DatosPagoDTO;
import com.example.carrito.entidades.Carrito;
import com.example.carrito.excepciones.ErrorServicio;
import com.example.carrito.servicios.compra.template.ProcesadorCompraTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Subclase Concreta del Template Method:
 * Procesamiento de compra por Transferencia Bancaria o Efectivo.
 * Aplica un 10% de descuento directo y genera datos de transferencia.
 */
@Service("compraTransferenciaService")
public class CompraTransferenciaService extends ProcesadorCompraTemplate {

    @Override
    protected double calcularTotalFinal(Carrito carrito, DatosPagoDTO datosPago) throws ErrorServicio {
        double subtotal = carrito.getTotal();
        // Beneficio del 10% de descuento por pago directo / transferencia
        double descuento = subtotal * 0.10;
        double totalFinal = subtotal - descuento;
        return Math.round(totalFinal * 100.0) / 100.0;
    }

    @Override
    protected ComprobantePago ejecutarTransaccionPago(double monto, DatosPagoDTO datosPago) throws ErrorServicio {
        String codigoTransferencia = "TRF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        return ComprobantePago.builder()
                .codigoTransaccion(codigoTransferencia)
                .montoCobrado(monto)
                .recargoODescuento(-10.0)
                .metodoPago("TRANSFERENCIA BANCARIA / EFECTIVO (10% OFF)")
                .fechaOperacion(LocalDateTime.now())
                .estado("PENDIENTE_CONFIRMACION")
                .mensajeResultado("Orden registrada. Alias para abonar: BLOOMSHOP.PAGOS. CBU: 0000003100012345678901")
                .build();
    }
}

