package com.example.carrito.dtos;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DatosPagoDTO {

    private String metodoPago; // "TARJETA" o "TRANSFERENCIA"
    
    // Datos para tarjeta
    private String titular;
    private String numeroTarjeta;
    private String fechaVencimiento;
    private String cvv;
    private int cuotas;

    // Datos para entrega o notas
    private String direccionEntrega;
    private String notas;
}

