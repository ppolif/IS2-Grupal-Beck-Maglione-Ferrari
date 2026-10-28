package com.example.zero.entidades.producto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;

/**
 * Entidad VigenciaPrecio
 * Representa el historial de precios de un Producto
 * cuando fechaHasta es null, esa es la vigencia de precio actual.
 */
@Entity
@Table(name = "vigencia_precio")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "producto")
public class VigenciaPrecio {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false, length = 36)
    private String id;

    @Column(name = "fecha_desde", nullable = false)
    private LocalDate fechaDesde;

    // Null == precio vigente actual (ver nota del diagrama original).
    @Column(name = "fecha_hasta")
    private LocalDate fechaHasta;

    @Column(nullable = false)
    private double precio;

    @Column(nullable = false)
    @Builder.Default
    private boolean eliminado = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;
}