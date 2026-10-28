package com.example.zero.dto.reporte;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReporteProveedorPrecioDTO {
    private String productoId;
    private String productoCodigo;
    private String productoNombre;
    private String productoDescripcion;
    private String proveedorId;
    private String proveedorRazonSocial;
    private String proveedorCuit;
    private Double menorPrecioCosto;
    private LocalDateTime fechaFacturaReferencia;
    private Long numeroFacturaReferencia;
    private String proveedorTelefono;
    private String urlWhatsAppContacto;
}
