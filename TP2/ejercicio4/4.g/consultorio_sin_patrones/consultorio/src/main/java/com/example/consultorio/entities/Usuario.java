package com.example.consultorio.entities;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Usuario{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(unique = true)
    private String nombreUsuario;
    private String clave;

    private boolean eliminado;

}
