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
 * Procesamiento de compra con Tarjeta de Crédito / Débito.
 */
@Service("compraTarjetaCreditoService")
public class CompraTarjetaCreditoService extends ProcesadorCompraTemplate {

    @Override
    protected double calcularTotalFinal(Carrito carrito, DatosPagoDTO datosPago) throws ErrorServicio {
        double subtotal = carrito.getTotal();
        int cuotas = datosPago.getCuotas() > 0 ? datosPago.getCuotas() : 1;

        // Recargo del 5% si es en 3 cuotas, 10% si es en 6 cuotas
        double recargo = 0.0;
        if (cuotas == 3) {
            recargo = subtotal * 0.05;
        } else if (cuotas > 3) {
            recargo = subtotal * 0.10;
        }

        double totalFinal = subtotal + recargo;
        return Math.round(totalFinal * 100.0) / 100.0;
    }

    @Override
    protected ComprobantePago ejecutarTransaccionPago(double monto, DatosPagoDTO datosPago) throws ErrorServicio {
        // Validaciones de tarjeta
        if (datosPago.getNumeroTarjeta() == null || datosPago.getNumeroTarjeta().replaceAll("\\s+", "").length() < 15) {
            throw new ErrorServicio("Número de tarjeta inválido. Debe contener entre 15 y 16 dígitos.");
        }
        if (datosPago.getTitular() == null || datosPago.getTitular().trim().isEmpty()) {
            throw new ErrorServicio("El nombre del titular de la tarjeta es obligatorio.");
        }
        if (datosPago.getCvv() == null || datosPago.getCvv().trim().length() < 3) {
            throw new ErrorServicio("Código de seguridad CVV inválido.");
        }

        // Simulación de pasarela de pago exitosa
        String authCode = "AUTH-TC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        return ComprobantePago.builder()
                .codigoTransaccion(authCode)
                .montoCobrado(monto)
                .recargoODescuento(0.0)
                .metodoPago("TARJETA DE CRÉDITO (" + (datosPago.getCuotas() > 0 ? datosPago.getCuotas() : 1) + " cuota/s)")
                .fechaOperacion(LocalDateTime.now())
                .estado("APROBADA")
                .mensajeResultado("Transacción autorizada correctamente por la pasarela.")
                .build();
    }
}

