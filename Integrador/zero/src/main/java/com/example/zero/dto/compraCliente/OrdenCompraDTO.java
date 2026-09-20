package com.example.zero.dto.compraCliente;

import com.example.zero.enums.EstadoOrdenCompra;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * Segun nota del UML, la OrdenCompra representa el carrito del cliente antes de facturarse.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrdenCompraDTO {
    private String id;
    private String identificadorCompra;
    private java.time.LocalDate fecha;
    private double total;
    private EstadoOrdenCompra estadoOrdenCompra;
    private String idCliente;
    private List<DetalleCompraDTO> detalle;
}
