package com.example.zero.entidades.persona;


import com.example.zero.entidades.compra.Factura;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

/**
 * Entidad Empleado (paquete "persona" del diagrama), hereda de Persona.
 *
 * Los métodos de negocio del diagrama (crearEmpleado, validar, modificarEmpleado, listarEmpleado,
 * asociarEmpleadoUsuario) corresponden a la capa de servicio (EmpleadoService), no a la entidad.
 */
@Entity
@Table(name = "empleado")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(callSuper = true, exclude = "facturas")
public class Empleado extends Persona {

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_empleado", nullable = false, length = 20)
    private TipoEmpleado tipoEmpleado;

    // Relación con Empresa: cada Empleado pertenece a una Empresa/sucursal.
    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "empresa_id")
    // private Empresa empresa;

    @OneToMany(mappedBy = "empleado", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Factura> facturas = new HashSet<>();
}