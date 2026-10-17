package com.example.consultorio.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String clave;

    private boolean eliminado;

}
