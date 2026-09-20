package com.example.zero.dto.compraCliente;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * El detalle y el total se derivan de la OrdenCompra asociada; no se cargan a mano.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrearFacturaClienteDTO {
    private String idCliente;
    private String idEmpleado;
    private String idOrdenCompra;
    private String idFormaDePago;
}
