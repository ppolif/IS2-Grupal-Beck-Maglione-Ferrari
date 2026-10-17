package com.example.consultorio.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
public class Paciente extends EntidadPersona{

    @JsonIgnore
    @OneToMany(mappedBy = "paciente", cascade = CascadeType.ALL)
    private List<FotoPaciente> imagenes;

}
