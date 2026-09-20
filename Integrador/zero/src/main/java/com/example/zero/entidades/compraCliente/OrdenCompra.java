package com.example.zero.entidades.compraCliente;

import com.example.zero.entidades.persona.Cliente;
import com.example.zero.entidades.persona.Empleado;
import com.example.zero.enums.EstadoOrdenCompra;
import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;
import java.util.List;

@Data
@Entity
public class OrdenCompra {
    @Id
    private String id;
    private String identificadorCompra;

    @Temporal(TemporalType.DATE)
    private Date fecha;

    private double total;

    @Enumerated(EnumType.STRING)
    private EstadoOrdenCompra estadoOrdenCompra;

    private boolean eliminado;

    @ManyToOne
    @JoinColumn(name = "dni_cliente")
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "dni_empleado")
    private Empleado empleado;

    @OneToMany(mappedBy = "ordenCompra", cascade = CascadeType.ALL)
    private List<DetalleCompra> detalles;
}
