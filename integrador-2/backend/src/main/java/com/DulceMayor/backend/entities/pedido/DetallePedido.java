package com.DulceMayor.backend.entities.pedido;

import com.DulceMayor.backend.entities.EntidadBase;
import com.DulceMayor.backend.entities.producto.Producto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Entidad de dominio que representa una línea de detalle de un {@link Pedido}
 * según {@code 01-dominio.md} y {@code 02-reglas-negocio.md}.
 * <p>
 * Se gestiona por composición desde {@link Pedido}. El {@code precioUnitario} se copia
 * de la {@code VigenciaPrecio} abierta del {@link Producto} al momento de cargar o modificar
 * el pedido, garantizando inmutabilidad histórica ante cambios futuros de precios.
 * </p>
 *
 * <p><b>Patrón GoF aplicado: Builder (Creacional)</b></p>
 * <ul>
 *   <li><b>Participante GoF:</b> ConcreteBuilder / Product.</li>
 *   <li><b>Implementación:</b> Vía {@link SuperBuilder} de Lombok.</li>
 * </ul>
 */
@Entity
@Table(name = "detalles_pedido")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(callSuper = true, exclude = "producto")
public class DetallePedido extends EntidadBase {

    @Column(nullable = false)
    private int cantidad;

    @Column(name = "precio_unitario", nullable = false)
    private double precioUnitario;

    @Column(nullable = false)
    private double subtotal;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;
}

