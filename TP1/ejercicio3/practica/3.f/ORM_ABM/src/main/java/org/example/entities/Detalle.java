package org.example.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Detalle {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private Integer cantidad;

    private Double subtotal;

    @Builder.Default
    private Boolean eliminado = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "facturaID")
    private Factura factura;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "stockID")
    private Stock stock;
}
