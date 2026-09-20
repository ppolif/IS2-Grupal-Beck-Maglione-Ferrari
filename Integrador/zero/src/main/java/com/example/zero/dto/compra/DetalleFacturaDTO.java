package com.example.zero.dto.compra;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetalleFacturaDTO {
    private String id;
    private String idProducto;
    private String nombreProducto;
    private int cantidad;
    private double subtotal;
}
