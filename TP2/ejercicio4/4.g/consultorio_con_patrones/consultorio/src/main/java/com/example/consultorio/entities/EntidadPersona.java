package com.example.consultorio.entities;

import jakarta.persistence.*;
import lombok.Data;

@Data
@MappedSuperclass
public abstract class EntidadPersona {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String nombre;
    private String apellido;
    private String dni;
    private boolean eliminado;

}
