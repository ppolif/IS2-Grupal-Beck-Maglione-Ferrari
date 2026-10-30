package com.DulceMayor.backend.entities.pedido;

import com.DulceMayor.backend.entities.EntidadBase;
import com.DulceMayor.backend.entities.persona.Distribuidor;
import com.DulceMayor.backend.entities.persona.Preventor;
import com.DulceMayor.backend.entities.persona.PuntoDeVenta;
import com.DulceMayor.backend.entities.producto.Promocion;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
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
 * Entidad transaccional central del dominio que representa un pedido cargado a un punto de venta
 * según {@code 01-dominio.md} y {@code 02-reglas-negocio.md}.
 * <p>
 * Conforme a {@code GENERAL.md} (§7), {@code Pedido} no referencia a {@code Factura} ni a las rutas:
 * es {@code Factura} (y {@link HistorialEstadoPedido}) quien apunta a {@code Pedido}.
 * </p>
 *
 * <p><b>Patrón GoF aplicado: Builder (Creacional)</b></p>
 * <ul>
 *   <li><b>Participante GoF:</b> ConcreteBuilder / Product.</li>
 *   <li><b>Implementación:</b> Vía {@link SuperBuilder} de Lombok (conforme a {@code 03-arquitectura.md}),
 *       permitiendo ensamblar pedidos con número correlativo, estado inicial {@link EstadoPedido#CARGADO},
 *       totales calculados, promoción opcional y colección de {@link DetallePedido} por composición.</li>
 * </ul>
 */
@Entity
@Table(name = "pedidos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(callSuper = true, exclude = {"promocion", "detalles", "puntoDeVenta", "preventor", "distribuidor"})
public class Pedido extends EntidadBase {

    @Column(name = "numero_pedido", nullable = false, unique = true)
    private long numeroPedido;

    @Builder.Default
    @Column(name = "fecha_hora_carga", nullable = false)
    private LocalDateTime fechaHoraCarga = LocalDateTime.now();

    @Column(nullable = false)
    private double subtotal;

    @Column(nullable = false)
    private double descuento;

    @Column(nullable = false)
    private double total;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoPedido estado = EstadoPedido.CARGADO;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "punto_de_venta_id", nullable = false)
    private PuntoDeVenta puntoDeVenta;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "preventor_id", nullable = false)
    private Preventor preventor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "distribuidor_id")
    private Distribuidor distribuidor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promocion_id")
    private Promocion promocion;

    /**
     * Composición 1 — 1..* hacia {@link DetallePedido}: el pedido es dueño de sus líneas de detalle.
     */
    @Builder.Default
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id")
    private List<DetallePedido> detalles = new ArrayList<>();
}

