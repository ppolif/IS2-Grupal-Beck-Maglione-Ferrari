package com.DulceMayor.backend.entities.persona;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Entidad que representa a un cliente minorista (comerciante dueño de uno o varios puntos de venta).
 */
@Entity
@Table(name = "minoristas")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
public class Minorista extends Persona {
}

