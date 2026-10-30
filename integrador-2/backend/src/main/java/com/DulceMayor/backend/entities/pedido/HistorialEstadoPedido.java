package com.DulceMayor.backend.entities.pedido;

import com.DulceMayor.backend.entities.EntidadBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Entidad de dominio que registra cada transición de estado de un {@link Pedido}
 * según {@code 01-dominio.md} (§3.5), {@code 02-reglas-negocio.md} (§1.2) y {@code GENERAL.md}.
 * <p>
 * Sustituye cualquier clase {@code Envio} en el modelo de dominio, proveyendo trazabilidad
 * completa (creación, faltante de stock, facturación, despacho en reparto, entrega y anulación).
 * </p>
 *
 * <p><b>Patrón GoF aplicado: Builder (Creacional)</b></p>
 * <ul>
 *   <li><b>Participante GoF:</b> ConcreteBuilder / Product.</li>
 *   <li><b>Implementación:</b> Vía {@link SuperBuilder} de Lombok.</li>
 * </ul>
 */
@Entity
@Table(name = "historial_estados_pedido")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(callSuper = true, exclude = "pedido")
public class HistorialEstadoPedido extends EntidadBase {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoPedido estado;

    @Builder.Default
    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora = LocalDateTime.now();

    @Column(nullable = false, length = 120)
    private String usuario;

    @Column(length = 500)
    private String observacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;
}

