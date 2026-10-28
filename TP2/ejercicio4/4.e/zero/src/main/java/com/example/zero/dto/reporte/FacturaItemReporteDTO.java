package com.example.zero.dto.reporte;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


// DTO que representa cada linea de producto dentro de la factura PDF.
// compatible con JRBeanCollectionDataSource de JasperReports.

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacturaItemReporteDTO {

    // obtenemos datos del detalle
    private Integer numeroItem;
    private String codigo;
    private String descripcion;
    private String categoria;
    private Integer cantidad;
    private Double precioUnitario;
    private Double subtotal;
}
