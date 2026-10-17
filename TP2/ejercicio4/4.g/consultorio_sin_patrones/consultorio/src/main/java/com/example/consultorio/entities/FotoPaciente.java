package com.example.consultorio.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @JsonIgnore
    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] contenido;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id")
    private Paciente paciente;

    private boolean eliminado;

}
