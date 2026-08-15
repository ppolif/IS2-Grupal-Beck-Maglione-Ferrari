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

    // solo se usa si  el tipo es compra, asi que puede ser null
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proveedor_cuit", nullable = true)
    private Proveedor proveedor;

    // solo si es tipo venta
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_dni", nullable = true)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "forma_pago_id")
    private FormaDePago formaDePago;

    //hacemos bidireccional la relación con detalle (una factura sabe qué detalles tiene y un detalle
    //sabe en que factura se creo)
    @OneToMany(mappedBy = "factura", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Detalle> detalles = new ArrayList<>();
}