package com.example.carrito.entidades;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "detalles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Detalle {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private String id;

    @Column(nullable = false)
    @Builder.Default
    private int cantidad = 1;

    @Column(nullable = false)
    @Builder.Default
    private double subtotal = 0.0;

    @Column(nullable = false)
    @Builder.Default
    private boolean eliminado = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "articulo_id", nullable = false)
    private Articulo articulo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carrito_id", nullable = false)
    private Carrito carrito;

    public void recalcularSubtotal() {
        if (articulo != null && !eliminado) {
            this.subtotal = this.cantidad * this.articulo.getPrecio();
        } else {
            this.subtotal = 0.0;
        }
    }
}

