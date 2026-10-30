package com.DulceMayor.backend.entities.direccion;

import com.DulceMayor.backend.entities.EntidadBase;
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
 * Entidad de dominio que representa una dirección física asociada a una {@link Localidad}.
 * Hereda id (UUID) y eliminado (borrado lógico) de {@link EntidadBase}.
 */
@Entity
@Table(name = "direcciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(callSuper = true, exclude = "localidad")
public class Direccion extends EntidadBase {

    @Column(nullable = false, length = 150)
    private String calle;

    @Column(name = "numero", nullable = false, length = 50)
    private String numero;

    @Column(name = "manzana_piso", length = 50)
    private String manzanaPiso;

    @Column(name = "casa_departamento", length = 50)
    private String casaDepartamento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "localidad_id")
    private Localidad localidad;
}

