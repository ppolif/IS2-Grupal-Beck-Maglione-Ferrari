package com.example.zero.entidades.zona;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Data
@Entity
public class Pais {
    @Id
    private String id;
    private String nombre;
    private boolean eliminado;

}
