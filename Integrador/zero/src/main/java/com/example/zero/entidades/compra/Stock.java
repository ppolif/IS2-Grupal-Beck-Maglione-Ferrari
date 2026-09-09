package com.example.zero.entidades.compra;



import com.example.zero.entidades.producto.Producto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.UuidGenerator;

/**
 * la clase Stock
 */
@Entity
@Table(name = "stock")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "producto")
public class Stock {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false, length = 36)
    private String id;

    @Column(nullable = false)
    private int cantidadActual;

    @Column(name = "observacion")
    private String observacion;

    @Column(nullable = false)
    @Builder.Default
    private boolean eliminado = false;

    // 1 registro de Stock por Detalle
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "detalle_id", nullable = false, unique = true)
    private Detalle detalle;
}
