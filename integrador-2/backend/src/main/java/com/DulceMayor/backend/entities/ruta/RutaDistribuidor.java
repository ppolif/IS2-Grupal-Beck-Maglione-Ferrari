package com.DulceMayor.backend.entities.ruta;

import com.DulceMayor.backend.entities.pedido.Pedido;
import com.DulceMayor.backend.entities.persona.Distribuidor;
import jakarta.persistence.DiscriminatorValue;
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
 * Entidad que modela la ruta diaria de entregas de un Distribuidor según {@code 01-dominio.md} (§3.7)
 * y {@code 02-reglas-negocio.md} (§6.2).
 *
 * <p>A diferencia de la preventa, las paradas de distribución nacen a partir de una colección
 * de {@link Pedido} en estado {@code FACTURADO}. El estado de compleción de cada parada se infiere
 * directamente del ciclo de vida del pedido (si su estado es {@code ENTREGADO}, la parada está completada),
 * evitando redundancia de datos.</p>
 */
@Entity
@DiscriminatorValue("DISTRIBUIDOR")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(callSuper = true, exclude = {"pedidos"})
public class RutaDistribuidor extends Ruta {

    /**
     * Lista ordenada de pedidos a entregar durante el día por el distribuidor.
     * El orden representa la secuencia óptima calculada por el algoritmo geodésico de Vecino Más Cercano.
     */
    /// En JPA, si una entidad A tiene un @OneToMany hacia B sin crear una relación bidireccional, JPA requiere
    /// o bien una clave foránea en la tabla pedidos (lo que acopla el pedido a una ruta específica), o bien una tabla
    /// intermedia de unión.

    /// ademas Si un distribuidor sale a repartir y no encuentra al dueño del local o no puede concretar la entrega, la ruta del día se cierra como INCOMPLETA.
    /// Ese mismo pedido sigue en estado pendiente y al día siguiente el jefe lo vuelve a asignar a una nueva ruta de reparto.
    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "rutas_distribuidor_pedidos",
            joinColumns = @JoinColumn(name = "ruta_id"),
            inverseJoinColumns = @JoinColumn(name = "pedido_id")
    )
    @OrderColumn(name = "orden_entrega")
    private List<Pedido> pedidos = new ArrayList<>();

    @Override
    public TipoRuta getTipo() {
        return TipoRuta.DISTRIBUIDOR;
    }

    /**
     * Retorna el {@link Distribuidor} responsable casteado convenientemente.
     *
     * @return instancia de {@link Distribuidor}.
     */
    public Distribuidor getDistribuidor() {
        return (Distribuidor) getResponsable();
    }
}

