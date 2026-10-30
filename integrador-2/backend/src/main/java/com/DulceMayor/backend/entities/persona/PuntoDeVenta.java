package com.DulceMayor.backend.entities.persona;

import com.DulceMayor.backend.entities.EntidadBase;
import com.DulceMayor.backend.entities.direccion.Direccion;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Entidad que modela un punto de venta (comercio físico) perteneciente a un {@link Minorista},
 * geolocalizado mediante una {@link Direccion} y atendido opcionalmente por un {@link Preventor}.
 */
@Entity
@Table(name = "puntos_de_venta")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(callSuper = true, exclude = {"minorista", "direccion", "preventor"})
public class PuntoDeVenta extends EntidadBase {

    @Column(name = "nombre_comercio", nullable = false, length = 150)
    private String nombreComercio;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "minorista_id", nullable = false)
    private Minorista minorista;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "direccion_id", nullable = false)
    private Direccion direccion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "preventor_id")
    private Preventor preventor;
}

