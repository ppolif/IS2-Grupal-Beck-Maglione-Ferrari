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
 * Procesamiento de compra directa e inmediata desde el carrito sin pantalla intermedia de checkout.
 */
@Service("compraDirectaService")
public class CompraDirectaService extends ProcesadorCompraTemplate {

    @Override
    protected double calcularTotalFinal(Carrito carrito, DatosPagoDTO datosPago) throws ErrorServicio {
        return Math.round(carrito.getTotal() * 100.0) / 100.0;
    }

    @Override
    protected ComprobantePago ejecutarTransaccionPago(double monto, DatosPagoDTO datosPago) throws ErrorServicio {
        String codigoTransaccion = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        return ComprobantePago.builder()
                .codigoTransaccion(codigoTransaccion)
                .montoCobrado(monto)
                .recargoODescuento(0.0)
                .metodoPago("COMPRA DIRECTA")
                .fechaOperacion(LocalDateTime.now())
                .estado("APROBADA")
                .mensajeResultado("Compra registrada de manera directa.")
                .build();
    }
}
