package com.example.zero.entidades.zona;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.UuidGenerator;

@Data
@Entity
public class Direccion {
    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false, length = 36)
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
