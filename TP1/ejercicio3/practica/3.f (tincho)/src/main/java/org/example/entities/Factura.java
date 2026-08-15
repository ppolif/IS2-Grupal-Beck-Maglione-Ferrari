package org.example.entities;

import org.example.enums.EstadoFactura;
import org.example.enums.TipoFactura;
import jakarta.persistence.*;
import lombok.*;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Factura {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private String id;

    private Date fecha;
    private double totalPagado;

    @Enumerated(EnumType.STRING)
    private EstadoFactura estado;

    @Enumerated(EnumType.STRING)
    private TipoFactura tipo;

    @Builder.Default
    private boolean eliminado = false;

    // Solo se usa si tipo == COMPRA
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proveedor_cuit", nullable = true)
    private Proveedor proveedor;

    // Solo se usa si tipo == VENTA
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_dni", nullable = true)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "forma_pago_id")
    private FormaDePago formaDePago;

    @OneToMany(mappedBy = "factura", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Detalle> detalles = new ArrayList<>();
}