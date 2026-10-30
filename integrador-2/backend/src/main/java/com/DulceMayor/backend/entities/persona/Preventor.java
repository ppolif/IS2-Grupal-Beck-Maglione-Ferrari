package com.DulceMayor.backend.entities.persona;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Entidad que representa a un empleado con rol de Preventor (PREVENTISTA).
 */
@Entity
@Table(name = "preventores")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
public class Preventor extends Empleado {
}

