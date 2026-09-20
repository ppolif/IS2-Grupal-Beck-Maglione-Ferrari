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
public class CrearFacturaProveedorDTO {
    private String idProveedor;
    private String idOrdenCompraProveedor;
    private String idFormaDePago;
}
