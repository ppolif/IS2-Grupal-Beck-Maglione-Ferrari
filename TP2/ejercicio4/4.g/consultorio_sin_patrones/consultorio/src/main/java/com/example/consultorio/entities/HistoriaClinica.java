package com.example.consultorio.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class HistoriaClinica{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    private Paciente paciente;

    @ManyToOne
    private Usuario usuario;

    @OneToMany(mappedBy = "historia", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleHistoriaClinica> detalles = new ArrayList<>();

    private boolean eliminado;

}
