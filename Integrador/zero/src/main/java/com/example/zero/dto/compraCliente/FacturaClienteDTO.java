package com.example.zero.dto.compraCliente;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.zero.ecommerce.dto.enums.EstadoFactura;
import com.zero.ecommerce.dto.pago.FormaDePagoDTO;
import java.util.List;

/**
 * 
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FacturaClienteDTO {
    private String id;
    private long numeroFactura;
    private java.time.LocalDate fechaFactura;
    private double totalPagado;
    private EstadoFactura estado;
    private String idCliente;
    private String nombreCliente;
    private String idEmpleado;
    private com.zero.ecommerce.dto.pago.FormaDePagoDTO formaDePago;
    private List<DetalleFacturaDTO> detalle;
    private String idOrdenCompra;
}
