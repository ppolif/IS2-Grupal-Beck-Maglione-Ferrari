package com.example.zero.dto.compraCliente;

import com.example.zero.enums.EstadoOrdenCompra;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * Para la pantalla de seguimiento de compra del cliente.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeguimientoCompraDTO {
    private String identificadorCompra;
    private java.time.LocalDate fecha;
    private EstadoOrdenCompra estadoOrdenCompra;
    private double total;
    private List<DetalleCompraDTO> detalle;
}
