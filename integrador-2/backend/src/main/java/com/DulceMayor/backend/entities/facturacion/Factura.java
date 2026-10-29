package com.DulceMayor.backend.entities.facturacion;

import com.DulceMayor.backend.entities.EntidadBase;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
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
 * Entidad de dominio que representa una factura emitida por el sistema según {@code 01-dominio.md}.
 *
 * <p><b>Patrón GoF aplicado: Builder (Creacional)</b></p>
 * <ul>
 *   <li><b>Participante GoF:</b> ConcreteBuilder / Product.</li>
 *   <li><b>Implementación:</b> Vía {@link SuperBuilder} de Lombok (conforme a {@code 03-arquitectura.md} §4.4).
 *       Permite construir instancias complejas de {@code Factura} paso a paso inicializando número correlativo,
 *       fecha de emisión, estado inicial ({@link EstadoFactura#PENDIENTE_PAGO}), total y colección de
 *       {@link DetalleFactura} por composición sin constructores telescópicos.</li>
 * </ul>
 */
@Entity
@Table(name = "facturas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(callSuper = true, exclude = "detalles")
public class Factura extends EntidadBase {

    @Column(name = "numero_factura", nullable = false, unique = true)
    private long numeroFactura;

    @Builder.Default
    @Column(name = "fecha_factura", nullable = false)
    private LocalDateTime fechaFactura = LocalDateTime.now();

    @Column(nullable = false)
    private double total;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoFactura estado = EstadoFactura.PENDIENTE_PAGO;

    /*
     * Relación con Pedido según 01-dominio.md (Factura -> Pedido, 1 — 0..1):
     * Mientras la entidad Pedido no esté implementada en el sistema, se almacena el identificador
     * pedidoId directamente en la columna pedido_id. Al incorporar Pedido se podrá mapear como:
     *
     * @ManyToOne(fetch = FetchType.LAZY)
     * @JoinColumn(name = "pedido_id")
     * private Pedido pedido;
     */
    @Column(name = "pedido_id", length = 36)
    private String pedidoId;

    /**
     * Composición 1 — 1..* hacia {@link DetalleFactura}: la factura es dueña de sus líneas de detalle.
     */
    @Builder.Default
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "factura_id")
    private List<DetalleFactura> detalles = new ArrayList<>();
}

