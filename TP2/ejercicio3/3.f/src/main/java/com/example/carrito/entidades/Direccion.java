package com.example.carrito.entidades;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entidad que almacena la ubicación geográfica (dirección) de un Proveedor
 * mediante sus coordenadas de latitud y longitud para su visualización en OpenStreetMap.
 */
@Entity
@Table(name = "direcciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Direccion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private String id;

    /**
     * Coordenada de latitud en grados decimales (rango válido: -90.0 a 90.0).
     * Ejemplo para Mendoza: -32.8895
     */
    @Column(nullable = false)
    private Double latitud;

    /**
     * Coordenada de longitud en grados decimales (rango válido: -180.0 a 180.0).
     * Ejemplo para Mendoza: -68.8458
     */
    @Column(nullable = false)
    private Double longitud;

    /**
     * Bandera para baja lógica siguiendo el estándar del resto de las entidades del sistema.
     */
    @Column(nullable = false)
    @Builder.Default
    private boolean eliminado = false;
}

