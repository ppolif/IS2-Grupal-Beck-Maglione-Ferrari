package com.example.zero.entidades.empresa;

import com.example.zero.enums.TipoContacto;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Contacto {
    @Id
    private String id;

    @Enumerated(EnumType.STRING)
    private TipoContacto tipoContacto;

    private String observacion;
    private boolean eliminado;
}
