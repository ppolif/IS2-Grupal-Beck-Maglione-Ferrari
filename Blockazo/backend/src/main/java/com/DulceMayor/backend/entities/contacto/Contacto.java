package com.DulceMayor.backend.entities.contacto;

import com.DulceMayor.backend.entities.EntidadBase;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Entidad base abstracta que modela un medio de contacto (correo electrónico o teléfono)
 * asociado a una {@code Persona} o a un {@code Mayorista}, según {@code 01-dominio.md}.
 *
 * <p><b>Patrón GRASP / POO aplicado: Polimorfismo + Herencia Single Table JPA.</b></p>
 * <p><b>Por qué se aplica acá:</b> Permite que {@code Persona} y {@code Mayorista} mantengan
 * una única colección polimórfica {@code List<Contacto>} en una sola tabla física ({@code contactos})
 * discriminada por la columna {@code medio} ({@code "CORREO"} o {@code "TELEFONICO"}), evitando
 * joins adicionales al recuperar los datos de contacto de clientes, empleados y sucursales.</p>
 */
@Entity
@Table(name = "contactos")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "medio", discriminatorType = DiscriminatorType.STRING, length = 20)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
public abstract class Contacto extends EntidadBase {

    public static final String MEDIO_CORREO = "CORREO";
    public static final String MEDIO_TELEFONICO = "TELEFONICO";

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_contacto", nullable = false, length = 20)
    private TipoContacto tipoContacto;

    @Column(length = 255)
    private String observacion;

    /**
     * Devuelve el discriminador polimórfico que identifica a la subclase concreta
     * ({@link #MEDIO_CORREO} o {@link #MEDIO_TELEFONICO}).
     */
    public abstract String getMedio();
}

