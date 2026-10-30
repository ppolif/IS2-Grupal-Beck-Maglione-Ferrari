package com.DulceMayor.backend.entities.direccion;

import com.DulceMayor.backend.entities.EntidadBase;
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
 * Entidad de dominio que representa un país dentro del diccionario geográfico.
 * Hereda id (UUID) y eliminado (borrado lógico) de {@link EntidadBase}.
 */
@Entity
@Table(name = "paises")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
public class Pais extends EntidadBase {

    @Column(nullable = false, length = 100)
    private String nombre;
}

