package com.DulceMayor.backend.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Entidad de dominio que representa una localidad asociada de forma unidireccional a un {@link Departamento}.
 * Hereda id (UUID) y eliminado (borrado lógico) de {@link EntidadBase}.
 */
@Entity
@Table(name = "localidades")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(callSuper = true, exclude = "departamento")
public class Localidad extends EntidadBase {

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(name = "codigo_postal", nullable = false, length = 20)
    private String codigoPostal;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "departamento_id", nullable = false)
    private Departamento departamento;
}

