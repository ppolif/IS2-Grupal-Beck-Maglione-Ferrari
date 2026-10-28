package com.DulceMayor.backend.entities.producto;

import com.DulceMayor.backend.entities.EntidadBase;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Entidad de dominio que representa el registro histórico (ledger/auditoría) de movimientos de stock.
 * <p>
 * Cada cambio en la cantidad disponible (ingreso inicial por alta, reposición física,
 * ajuste de inventario o egreso por venta) genera un nuevo registro inmutable donde se almacena:
 * <ul>
 *   <li>{@code cantidadActual}: la nueva cantidad física disponible resultante.</li>
 *   <li>{@code fechaHora}: marca temporal precisa en la que ocurrió el movimiento.</li>
 *   <li>{@code observacion}: descripción que categoriza el movimiento como ingreso o egreso.</li>
 * </ul>
 * </p>
 *
 * <p><b>Relación según el nuevo modelo UML:</b></p>
 * En el diagrama UML, {@code Stock} <b>ya no tiene el atributo {@code producto}</b>.
 * Se relaciona únicamente con {@code DetalleFactura}, ya que desde allí se accede al producto
 * ({@code Stock} &rarr; {@code DetalleFactura} &rarr; {@code Producto}).
 * Como la entidad {@code DetalleFactura} aún no ha sido implementada en el sistema,
 * dicha relación se mantiene comentada.
 *
 * <p><b>Patrón GoF aplicado: Builder (Creacional)</b></p>
 * <ul>
 *   <li><b>Participante GoF:</b> ConcreteBuilder / Product.</li>
 *   <li><b>Implementación:</b> Vía {@link SuperBuilder} de Lombok. Separa la construcción de la
 *       instancia compleja de {@code Stock} de su representación interna, facilitando la creación fluida
 *       e inmutable de nuevos registros de movimientos con sus marcas temporales y observaciones.</li>
 * </ul>
 */
@Entity
@Table(name = "stocks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
public class Stock extends EntidadBase {

    /*
     * Relación con DetalleFactura según el nuevo diseño UML:
     * Stock no tiene atributo 'producto' directo; únicamente se relaciona con DetalleFactura,
     * y desde allí navega al Producto correspondiente (Stock -> DetalleFactura -> Producto).
     * Como DetalleFactura todavía no existe en el sistema, esta relación queda comentada:
     *
     * @ManyToOne(fetch = FetchType.LAZY)
     * @JoinColumn(name = "detalle_factura_id")
     * private DetalleFactura detalleFactura;
     */

    /**
     * Nueva cantidad disponible resultante tras registrar este movimiento.
     */
    @Builder.Default
    @Column(name = "cantidad_actual", nullable = false)
    private int cantidadActual = 0;

    /**
     * Fecha y hora exacta en la que se produjo la alteración del stock.
     */
    @Builder.Default
    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora = LocalDateTime.now();

    /**
     * Detalle descriptivo que aclara la causa del movimiento e indica explícitamente
     * si es un ingreso (alta, reposición, ajuste positivo) o un egreso (venta, merma, ajuste negativo).
     */
    @Column(length = 500)
    private String observacion;
}
