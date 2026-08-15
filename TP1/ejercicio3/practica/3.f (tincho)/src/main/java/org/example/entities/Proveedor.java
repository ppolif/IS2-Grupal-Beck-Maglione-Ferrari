package org.example.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Proveedor {

    @Id
    @EqualsAndHashCode.Include
    private String cuit;

    private String razonSocial;

    @Builder.Default
    private boolean eliminado = false;

    @ManyToMany(mappedBy = "proveedores")
    @Builder.Default
    private Set<Producto> productos = new HashSet<>();
}
