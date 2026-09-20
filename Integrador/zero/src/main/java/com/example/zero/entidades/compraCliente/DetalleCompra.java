package com.example.zero.entidades.compraCliente;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class DetalleCompra {
    @Id
    private String id;
    private int cantidad;
    private double subtotal;
    private boolean eliminado;

    @ManyToOne
    @JoinColumn(name = "orden_compra_id")
    private OrdenCompra ordenCompra;
}
