package com.example.zero.entidades.zona;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Data
@Entity
public class Provincia {
    @Id
    private String id;
    private String nombre;
    private boolean eliminado;

    @ManyToOne
    @JoinColumn(name = "pais_id")
    private Pais pais;

}
