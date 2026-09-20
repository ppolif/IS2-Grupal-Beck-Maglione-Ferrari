package com.example.zero.dto.compraCliente;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgregarItemCarritoDTO {
    private String idProducto;
    private int cantidad;
}
