package com.DulceMayor.backend.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Entidad de dominio que representa las credenciales de acceso al sistema.
 * Hereda id (UUID) y eliminado (borrado lógico) de EntidadBase.
 * Se utiliza el patrón Builder (vía Lombok @SuperBuilder) para la construcción segura de instancias.
 */
@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
public class Usuario extends EntidadBase {

    public static final String CLAVE_POR_DEFECTO = "dulcemayor";
    public static final String ClavePorDefecto = CLAVE_POR_DEFECTO;

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
}
