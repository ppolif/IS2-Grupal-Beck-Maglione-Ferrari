package com.DulceMayor.backend.entities.ruta;

import com.DulceMayor.backend.entities.EntidadBase;
import com.DulceMayor.backend.entities.persona.Empleado;
import com.DulceMayor.backend.entities.persona.PuntoDeVenta;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Clase base abstracta de las rutas diarias según {@code 01-dominio.md} (§3.7).
 *
 * <p><b>Patrón GoF aplicado: Template Method / Polimorfismo por Herencia (Estructural/Comportamiento)</b></p>
 * Define los atributos comunes de cualquier itinerario diario: la fecha de ejecución, el estado general
 * y un puntero a la {@link #paradaActual} que se está atendiendo en este momento.
 *
 * Se utiliza la estrategia de persistencia {@link InheritanceType#SINGLE_TABLE} discriminada por
 * {@code tipo_ruta} para unificar consultas y auditorías de rutas con alta performance.
 */
@Entity
@Table(name = "rutas")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo_ruta", discriminatorType = DiscriminatorType.STRING, length = 30)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(callSuper = true, exclude = {"paradaActual", "responsable"})
public abstract class Ruta extends EntidadBase {

    @Column(nullable = false)
    private LocalDate fecha;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RutaEstado estado = RutaEstado.PENDIENTE;

    /**
     * Referencia al {@link PuntoDeVenta} que se está atendiendo en curso.
     * Nulo si la ruta finalizó (REALIZADA) o aún no cuenta con paradas pendientes.
     */
    /// es many to one porque las rutas se crean por día
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parada_actual_id")
    private PuntoDeVenta paradaActual;

    /**
     * Empleado asignado a la ruta (un Preventor o un Distribuidor).
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "responsable_id", nullable = false)
    private Empleado responsable;

    /**
     * Retorna el tipo de ruta correspondiente según la subclase polimórfica.
     *
     * @return {@link TipoRuta#PREVENTOR} o {@link TipoRuta#DISTRIBUIDOR}.
     */
    public abstract TipoRuta getTipo();
}

