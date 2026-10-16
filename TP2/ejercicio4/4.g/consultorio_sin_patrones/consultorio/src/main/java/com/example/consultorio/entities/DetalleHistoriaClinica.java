package com.example.consultorio.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Data
public class DetalleHistoriaClinica{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private Date fechaHistoria;
    private String detalleHistoria;

    @ManyToOne
    private HistoriaClinica historia;

    @ManyToOne
    private Medico medico;

    private boolean eliminado;

}
