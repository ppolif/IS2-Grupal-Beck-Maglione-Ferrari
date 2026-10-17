package com.example.consultorio.entities;

import jakarta.persistence.*;
import lombok.Data;


@Data
@Entity
public class FotoPaciente {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String nombre;
    private String mime;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] contenido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id")
    private Paciente paciente;

    private boolean eliminado;

}
