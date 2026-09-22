package com.example.zero.entidades.zona;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.UuidGenerator;
import java.util.List;

@Data
@Entity
public class Pais {
    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false, length = 36)
    private String id;
    private String nombre;
    private boolean eliminado;

}
