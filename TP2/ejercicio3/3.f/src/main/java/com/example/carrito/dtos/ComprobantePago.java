package com.example.carrito.dtos;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComprobantePago {

    private String codigoTransaccion;
    private double montoCobrado;
    private double recargoODescuento;
    private String metodoPago;
    private LocalDateTime fechaOperacion;
    private String estado;
    private String mensajeResultado;
}

