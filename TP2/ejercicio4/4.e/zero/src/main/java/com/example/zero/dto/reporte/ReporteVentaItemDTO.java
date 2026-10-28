package com.example.zero.dto.reporte;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

///epresenta una fila en el detalle del Reporte de Ventas
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReporteVentaItemDTO {
    private String facturaId;
    private String numeroOrden;
    private LocalDateTime fechaCompra;
    private String productoCodigo;
    private String productoNombre;
    private String categoriaNombre;
    private int cantidad;
    private double precioUnitario;
    private double subtotal;
    private String formaPago;
    private String clienteNombre;
    private String clienteIdentificador;
}
