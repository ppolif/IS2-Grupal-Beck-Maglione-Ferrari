package com.DulceMayor.backend.entities.persona;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Entidad que representa a un empleado de la empresa mayorista.
 * Modela directamente a roles como JEFE y ADMINISTRATIVO, y es superclase
 * de {@link Preventor} y {@link Distribuidor}.
 */
@Entity
@Table(name = "empleados")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
public class Empleado extends Persona {

    @Column(nullable = false, unique = true, length = 50)
    private String legajo;
}

