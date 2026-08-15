package org.example.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Proveedor {

    @Id
    @Column(nullable = false, unique = true)
    private String cuit;

    private String razonSocial;

    @Builder.Default
    private boolean eliminado = false;

    //bidireccional con producto, un proveedor sabe cuales productos provee
    @ManyToMany(mappedBy = "proveedores")
    @Builder.Default
    private Set<Producto> productos = new HashSet<>();
}
