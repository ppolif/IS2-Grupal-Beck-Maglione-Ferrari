package com.example.zero.entidades.zona;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Data
@Entity
public class Localidad {
    @Id
    private String id;
    private String nombre;
    private String codigoPostal;
    private boolean eliminado;

    @ManyToOne
    @JoinColumn(name = "departamento_id")
    private Departamento departamento;

    @OneToMany(mappedBy = "localidad")
    private List<Direccion> direcciones;
}
