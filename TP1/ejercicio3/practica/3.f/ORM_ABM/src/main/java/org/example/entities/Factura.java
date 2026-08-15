package org.example.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.example.enums.EstadoFactura;

import java.util.Date;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Factura {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false)
    private String id;

    @Column(nullable = false)
    private Date fecha;

    @Column(nullable = false)
    private Double totalPagado;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private EstadoFactura estado;

    // En tu clase Factura.java
    @OneToMany(mappedBy = "factura", cascade = CascadeType.ALL, orphanRemoval = true, targetEntity = Detalle.class)
    private List<Detalle> detalles;

    @Column(nullable = false)
    @Builder.Default
    private Boolean eliminado = false;

    //@OnetoOne(cascade = CascadeTyple.ALL)
    //@JoinColumn(name = "formaDePagoId")
    //private FormaDePago formaDePago;

}
