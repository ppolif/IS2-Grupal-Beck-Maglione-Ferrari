package com.example.carrito.servicios.compra;

import com.example.carrito.excepciones.ErrorServicio;
import com.example.carrito.servicios.compra.template.ProcesadorCompraTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class ProcesadorCompraFactory {

    @Autowired
    @Qualifier("compraTarjetaCreditoService")
    private ProcesadorCompraTemplate compraTarjetaCreditoService;

    @Autowired
    @Qualifier("compraTransferenciaService")
    private ProcesadorCompraTemplate compraTransferenciaService;

    @Autowired
    @Qualifier("compraDirectaService")
    private ProcesadorCompraTemplate compraDirectaService;

    public ProcesadorCompraTemplate obtenerProcesador(String metodoPago) throws ErrorServicio {
        if (metodoPago == null || metodoPago.trim().isEmpty()) {
            return compraDirectaService;
        }

        return switch (metodoPago.toUpperCase().trim()) {
            case "TARJETA" -> compraTarjetaCreditoService;
            case "TRANSFERENCIA" -> compraTransferenciaService;
            case "DIRECTA", "DIRECTO", "EFECTIVO" -> compraDirectaService;
            default -> compraDirectaService;
        };
    }
}
