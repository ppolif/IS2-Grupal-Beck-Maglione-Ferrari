package com.example.zero.dto.reporte;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

///DTO contenedor para el Reporte de Ventas completo (resumen y detalle)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReporteVentasDTO {
    private LocalDate fechaDesde;
    private LocalDate fechaHasta;
    private double totalRecaudado;
    private int cantidadVentas;
    private int totalArticulosVendidos;
    private double ticketPromedio;

    @Builder.Default
    private List<ReporteVentaItemDTO> items = new ArrayList<>();
}
