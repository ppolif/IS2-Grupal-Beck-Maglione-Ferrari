package com.example.zero.entidades.compraProveedor;

import com.example.zero.entidades.compra.Factura;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
public class FacturaProveedor extends Factura {
    @ManyToOne
    @JoinColumn(name = "proveedor_id")
    private Proveedor proveedor;
}
