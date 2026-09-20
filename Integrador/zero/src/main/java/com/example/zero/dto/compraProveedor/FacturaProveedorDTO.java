package com.example.zero.dto.compraProveedor;

import com.example.zero.enums.EstadoFactura;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FacturaProveedorDTO {
    private String id;
    private long numeroFactura;
    private java.time.LocalDate fechaFactura;
    private double totalPagado;
    private EstadoFactura estado;
    private String idProveedor;
    private String razonSocialProveedor;
   // private com.zero.ecommerce.dto.pago.FormaDePagoDTO formaDePago;
   // private List<DetalleFacturaDTO> detalle;
}
