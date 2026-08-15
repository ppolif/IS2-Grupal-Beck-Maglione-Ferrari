package org.example.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Detalle {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private String id;

    private int cantidad;
    private double subtotal;

    @Builder.Default
    private boolean eliminado = false;

    //dueño de la relacion con factura (porque una factura tiene muchos detalles)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "factura_id")
    private Factura factura;

    //dueño de la relacion con prodcuto (un detalle sabe qué producto es)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id")
    private Producto producto;
}