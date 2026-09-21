package com.example.zero.entidades.zona;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Data
@Entity
public class Departamento {
    @Id
    private String id;
    private String nombre;
    private boolean eliminado;

    @ManyToOne
    @JoinColumn(name = "provincia_id")
    private Provincia provincia;

}
