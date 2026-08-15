package org.example.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigInteger;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Cliente {

    @Id
    @Column(nullable = false, unique = true)
    private String dni;

    private String nombre;
    private String apellido;

    @Builder.Default
    private boolean eliminado = false;
}