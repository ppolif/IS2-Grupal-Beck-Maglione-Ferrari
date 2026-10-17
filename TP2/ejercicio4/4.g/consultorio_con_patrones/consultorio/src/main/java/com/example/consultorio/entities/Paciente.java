package com.example.consultorio.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
public class Paciente extends EntidadPersona{

    @OneToMany(mappedBy = "paciente", cascade = CascadeType.ALL)
    private List<FotoPaciente> imagenes;

}
