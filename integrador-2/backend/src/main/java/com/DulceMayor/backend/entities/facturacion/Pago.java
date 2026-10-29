package com.DulceMayor.backend.entities.facturacion;

import com.DulceMayor.backend.entities.EntidadBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
 * Entidad de dominio que representa un cobro registrado contra una {@link Factura}
 * según {@code 01-dominio.md} y {@code 04-openapi.yaml}.
 * <p>
 * Mantiene referencias unidireccionales hacia {@link Factura} ({@code Pago -> Factura}) y
 * hacia {@link FormaDePago} ({@code Pago -> FormaDePago}).
 * </p>
 */
@Entity
@Table(name = "pagos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(callSuper = true, exclude = {"factura", "formaDePago"})
public class Pago extends EntidadBase {

    @Column(nullable = false)
    private double monto;

    @Builder.Default
    @Column(name = "fecha_pago", nullable = false)
    private LocalDateTime fechaPago = LocalDateTime.now();

    @Column(length = 255)
    private String referencia;

    @Column(length = 500)
    private String observacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "forma_de_pago_id", nullable = false)
    private FormaDePago formaDePago;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "factura_id", nullable = false)
    private Factura factura;
}

