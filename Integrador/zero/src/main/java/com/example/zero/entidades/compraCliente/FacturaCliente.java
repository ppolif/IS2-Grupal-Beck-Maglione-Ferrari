package com.example.zero.entidades.compraCliente;

import com.example.zero.entidades.compra.Factura;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
public class FacturaCliente extends Factura {
    ////??????
    @ManyToOne
    @JoinColumn(name = "orden_compra_id")
    private OrdenCompra ordenCompra;
}
