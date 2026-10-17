package com.example.carrito.entidades;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "carritos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Carrito {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private String id;

    @Column(nullable = false)
    @Builder.Default
    private double total = 0.0;

    @Column(nullable = false)
    @Builder.Default
    private boolean eliminado = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @OneToMany(mappedBy = "carrito", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Detalle> detalles = new ArrayList<>();

    public void recalcularTotal() {
        if (detalles == null || detalles.isEmpty()) {
            this.total = 0.0;
            return;
        }
        double sumatoria = 0.0;
        for (Detalle d : detalles) {
            if (!d.isEliminado()) {
                d.recalcularSubtotal();
                sumatoria += d.getSubtotal();
            }
        }
        this.total = Math.round(sumatoria * 100.0) / 100.0;
    }

    public void agregarDetalle(Detalle detalle) {
        if (this.detalles == null) {
            this.detalles = new ArrayList<>();
        }
        this.detalles.add(detalle);
        detalle.setCarrito(this);
        recalcularTotal();
    }
}

