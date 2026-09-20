package com.example.zero.dto.compraProveedor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * Supuesto del equipo: el UML no modela esta entidad explicitamente, solo el enunciado la menciona.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrdenCompraProveedorDTO {
    private String id;
    private String idProveedor;
    private String razonSocialProveedor;
    private java.time.LocalDate fecha;
    private double total;
    private String estado;
    private List<DetalleOrdenCompraProveedorDTO> detalle;
}
