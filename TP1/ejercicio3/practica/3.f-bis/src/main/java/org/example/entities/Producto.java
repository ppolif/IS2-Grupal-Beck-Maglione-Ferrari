package org.example.entities;

import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private String id;

    private String nombre;
    private String marca;

    @Builder.Default
    private boolean eliminado = false;


    //producto es el dueño de la relacion con proveedores, como es many to many creamos tabla
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "producto_proveedor",
            joinColumns = @JoinColumn(name = "producto_id"),
            inverseJoinColumns = @JoinColumn(name = "proveedor_cuit")
    )
    @Builder.Default
    private Set<Proveedor> proveedores = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "producto_vigencia", joinColumns = @JoinColumn(name = "producto_id"))
    @Builder.Default
    private List<Vigencia> historicoPrecios = new ArrayList<>();
}