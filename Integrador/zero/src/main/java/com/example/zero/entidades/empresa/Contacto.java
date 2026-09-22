package com.example.zero.entidades.empresa;

import com.example.zero.enums.TipoContacto;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.UuidGenerator;

@Data
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Contacto {
    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false, length = 36)
    private String id;

    @Enumerated(EnumType.STRING)
    private TipoContacto tipoContacto;

    private String observacion;
    private boolean eliminado;
}
