package org.example.entities;

import org.example.enums.TipoMovimientoStock;
import jakarta.persistence.*;
import lombok.*;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Enumerated(EnumType.STRING)
    private TipoMovimientoStock tipo;

    private Date fechaMovimiento;
    private int cantActual;
    private int umbralReposicion;
    private int cantMax;

    //relacion para saber que producto se repuso/vendio
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id")
    private Producto producto;

    //relacion que permite registrar el detalle que genero cambio en el stock
    @OneToOne
    @JoinColumn(name = "id")
    private Detalle detalle;

}