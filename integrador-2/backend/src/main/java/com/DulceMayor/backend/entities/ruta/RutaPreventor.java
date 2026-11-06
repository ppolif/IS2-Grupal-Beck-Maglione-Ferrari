package com.DulceMayor.backend.entities.ruta;

import com.DulceMayor.backend.entities.persona.Preventor;
import com.DulceMayor.backend.entities.persona.PuntoDeVenta;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OrderColumn;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Entidad que modela la ruta diaria de visitas de un Preventor comercial a sus Puntos de Venta
 * según {@code 01-dominio.md} (§3.7) y {@code 02-reglas-negocio.md} (§6.1).
 *
 * <p><b>Estrategia de Lista de Visitados:</b></p>
 * En lugar de utilizar una entidad artificial separada o un conjunto sin orden, esta clase
 * implementa una lista {@link #visitadosIds} (mediante {@link ElementCollection} con {@link OrderColumn})
 * para registrar de forma cronológica y determinista los identificadores de los puntos de venta
 * que el preventor fue marcando como visitados a lo largo de su jornada.
 */
@Entity
@DiscriminatorValue("PREVENTOR")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(callSuper = true, exclude = {"puntosDeVenta", "visitadosIds"})
public class RutaPreventor extends Ruta {

    /**
     * Puntos de venta planificados para la jornada del preventor, ordenados geográficamente
     * por el algoritmo del Vecino Más Cercano (Haversine).
     */
    /// es many to many porque las rutas se crean por día.
    /// entonces muchos puntos de ventas pertenecen a una ruta un dia determinado
    /// al dia siguiente pertenecen a la "misma" ruta que en realidad es un objeto nuevo
    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "rutas_preventor_puntos",
            joinColumns = @JoinColumn(name = "ruta_id"),
            inverseJoinColumns = @JoinColumn(name = "punto_de_venta_id")
    )
    @OrderColumn(name = "orden_visita")
    private List<PuntoDeVenta> puntosDeVenta = new ArrayList<>();

    /**
     * Lista cronológica de identificadores (IDs) de los {@link PuntoDeVenta} que el preventor
     * ya marcó como visitados tras su paso por el comercio.
     * Permite saber qué paradas están completadas y en qué orden se realizaron.
     */
    @Builder.Default
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "rutas_preventor_visitados", joinColumns = @JoinColumn(name = "ruta_id"))
    @OrderColumn(name = "orden_ejecucion")
    @Column(name = "punto_de_venta_id", nullable = false)
    private List<String> visitadosIds = new ArrayList<>();

    @Override
    public TipoRuta getTipo() {
        return TipoRuta.PREVENTOR;
    }

    /**
     * Retorna el {@link Preventor} responsable casteado convenientemente a partir de la referencia
     * base de empleado.
     *
     * @return instancia de {@link Preventor}.
     */
    public Preventor getPreventor() {
        return (Preventor) getResponsable();
    }
}

