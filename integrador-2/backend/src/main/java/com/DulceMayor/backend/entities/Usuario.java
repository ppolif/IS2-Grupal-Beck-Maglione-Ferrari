package com.DulceMayor.backend.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Entidad de dominio que representa las credenciales de acceso al sistema.
 * Se utiliza el patrón Builder (vía Lombok) para la construcción segura de instancias.
 */
@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Usuario {

    public static final String CLAVE_POR_DEFECTO = "dulcemayor";
    public static final String ClavePorDefecto = CLAVE_POR_DEFECTO;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36, nullable = false, updatable = false)
    private String id;

    @Column(name = "nombre_usuario", nullable = false, unique = true, length = 100)
    private String nombreUsuario;

    @JsonIgnore
    @ToString.Exclude
    @Column(nullable = false)
    private String clave;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RolUsuario rol;

    @Builder.Default
    @Column(name = "clave_temporal", nullable = false)
    private boolean claveTemporal = true;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "proveedor_auth", nullable = false, length = 30)
    private ProveedorAuth proveedorAuth = ProveedorAuth.LOCAL;

    @Builder.Default
    @Column(nullable = false)
    private boolean eliminado = false;
}

