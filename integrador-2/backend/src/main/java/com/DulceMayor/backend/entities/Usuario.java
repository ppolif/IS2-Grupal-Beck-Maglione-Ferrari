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

    @Column(name = "nombre_usuario", nullable = false, unique = true, length = 100)
    private String nombreUsuario;

    @JsonIgnore
    @ToString.Exclude
    @Column(nullable = false)
    private String clave;

    /// por qué hay roles y también entidades?
    /// los roles se utilizan para gestionar la seguridad, mientras que las entidades que corresponden a un cierto rol
    /// solo es necesario si tiene datos propios o relaciones con otras entidades
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RolUsuario rol;

    @Builder.Default
    @Column(name = "clave_temporal", nullable = false)
    private boolean claveTemporal = true;

    /// es importante guardar el proveedor de auth ya que esto determina cosas como por ejemplo si un usuario puede o no
    /// modificar ciertos datos (solo puede si el proveedor es LOCAL, es decir, tiene una cuenta creada)
    /// hay que ver el manejo de este atributo, una vez que el usuario se cree una cuenta lo más simple sería que este atributo cambie a
    /// LOCAL y no permita que el usuario ingrese con auth0, solamente con su cuenta local
    /// si no tiene cuenta local supongo que deberiamos hacer que esto cambie a un proveedor distinto cada vez que el usuario ingrese
    /// con una red social distinta
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "proveedor_auth", nullable = false, length = 30)
    private ProveedorAuth proveedorAuth = ProveedorAuth.LOCAL;
}
