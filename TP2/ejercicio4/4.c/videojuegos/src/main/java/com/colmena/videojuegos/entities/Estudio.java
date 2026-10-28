package com.colmena.videojuegos.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "estudios")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Estudio extends Base {

    private String nombre;

    @JsonIgnore
    @OneToMany(mappedBy = "estudio")
    private List<Videojuego> videojuegos;
}
