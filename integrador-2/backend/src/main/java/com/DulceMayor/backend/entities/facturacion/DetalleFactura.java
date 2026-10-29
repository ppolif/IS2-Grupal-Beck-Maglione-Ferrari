package com.DulceMayor.backend.entities.facturacion;

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
 * Entidad de dominio que representa una línea de detalle de una {@link Factura}
 * según {@code 01-dominio.md} y {@code 04-openapi.yaml}.
 * <p>
 * Se gestiona por composición desde {@link Factura} y mantiene una asociación unidireccional
 * hacia {@link Producto} ({@code DetalleFactura -> Producto}). A su vez, los movimientos de egreso
 * por venta en {@code Stock} se vinculan a esta entidad ({@code Stock -> DetalleFactura -> Producto}).
 * </p>
 */
@Entity
@Table(name = "detalles_factura")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(callSuper = true, exclude = "producto")
public class DetalleFactura extends EntidadBase {

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

