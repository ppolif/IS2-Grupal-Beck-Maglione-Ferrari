package org.example.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

import java.math.BigInteger;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Cliente {

    @Id
    @EqualsAndHashCode.Include
    private String dni;

    private String nombre;
    private String apellido;

    @Builder.Default
    private boolean eliminado = false;
}