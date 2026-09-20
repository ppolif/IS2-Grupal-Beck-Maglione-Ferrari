package com.example.zero.dto.compraProveedor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetalleOrdenCompraProveedorDTO {
    private String idProducto;
    private String nombreProducto;
    private int cantidad;
    private double precioCompra;
    private double subtotal;
}
