package com.example.carrito.entidades;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "proveedores")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Proveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private String id;

    @Column(nullable = false, unique = true, length = 150)
    private String nombre;

    @Column(nullable = false)
    @Builder.Default
    private boolean eliminado = false;

    /**
     * Relación 1 a 1 con Direccion.
     * CascadeType.ALL permite que al guardar el Proveedor se guarde automáticamente su Direccion.
     * FetchType.EAGER asegura que las coordenadas estén disponibles al cargar los proveedores para el mapa.
     */
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "direccion_id")
    private Direccion direccion;

    /**
     * Se agrega @JsonIgnore para evitar recursión infinita cuando Thymeleaf (th:inline="javascript")
     * serializa los objetos Proveedor a JSON para usarlos en Leaflet.js.
     */
    @JsonIgnore
    @OneToMany(mappedBy = "proveedor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Articulo> articulos = new ArrayList<>();
}

