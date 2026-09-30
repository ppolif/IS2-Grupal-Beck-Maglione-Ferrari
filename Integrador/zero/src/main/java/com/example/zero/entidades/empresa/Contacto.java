package com.example.zero.entidades.empresa;

import com.example.zero.enums.TipoContacto;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "contacto")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
// Clase de las que heredan el correo y el numero de telefono
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
