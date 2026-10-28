package com.DulceMayor.backend.entities.producto;

import com.DulceMayor.backend.entities.EntidadBase;
import jakarta.persistence.*;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Entidad de dominio que representa una promoción o código de descuento,
 * según 01-dominio.md y 02-reglas-negocio.md.
 */
@Entity
@Table(name = "promociones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(callSuper = true, exclude = "producto")
public class Promocion extends EntidadBase {

    @Column(nullable = false, unique = true, length = 50)
    private String codigo;

    @Column(length = 255)
    private String descripcion;

    @Column(name = "porcentaje_descuento", nullable = false)
    private double porcentajeDescuento;

    @Column(name = "cantidad_minima")
    private Integer cantidadMinima;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id")
    private Producto producto;

    @Column(name = "fecha_desde", nullable = false)
    private LocalDate fechaDesde;

    @Column(name = "fecha_hasta", nullable = false)
    private LocalDate fechaHasta;
}

