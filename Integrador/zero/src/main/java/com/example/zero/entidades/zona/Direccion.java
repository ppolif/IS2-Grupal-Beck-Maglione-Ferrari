package com.example.zero.entidades.zona;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Direccion {
    @Id
    private String id;
    private String calle;
    private String numeracion;
    private String barrio;
    private String manzanaPiso;
    private String casaDepartamento;
    private String referencia;
    private boolean eliminado;

    @ManyToOne
    @JoinColumn(name = "localidad_id")
    private Localidad localidad;
}
