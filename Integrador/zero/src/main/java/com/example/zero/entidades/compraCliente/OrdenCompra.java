package com.example.zero.entidades.compraCliente;

import com.example.zero.entidades.persona.Cliente;
import com.example.zero.entidades.persona.Empleado;
import com.example.zero.enums.EstadoOrdenCompra;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "orden_compra")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"cliente", "empleado", "detalles"})
public class OrdenCompra {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private String id;

    @Column(name = "identificador_compra")
    private String identificadorCompra;

    @Temporal(TemporalType.DATE)
    @Column(name = "fecha")
    private Date fecha;

    @Column(name = "total", nullable = false)
    @Builder.Default
    private double total = 0.0;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_orden_compra", nullable = false)
    private EstadoOrdenCompra estadoOrdenCompra;

    @Column(nullable = false)
    @Builder.Default
    private boolean eliminado = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dni_cliente")
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dni_empleado")
    private Empleado empleado;

    @OneToMany(mappedBy = "ordenCompra", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<DetalleCompra> detalles = new ArrayList<>();
}
