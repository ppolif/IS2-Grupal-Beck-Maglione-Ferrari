package com.colmena.videojuegos.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "categorias")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Categoria extends Base {

    private String nombre;

    @JsonIgnore
    @OneToMany(mappedBy = "categoria")
    private List<Videojuego> videojuegos;
}
