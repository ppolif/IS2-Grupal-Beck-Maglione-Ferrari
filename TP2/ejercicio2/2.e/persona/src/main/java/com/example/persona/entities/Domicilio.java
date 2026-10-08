package com.example.persona.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

@Entity
@Table(name = "domicilio")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Audited
public class Domicilio extends Base {

    @Column(name="calle")
    private String calle;

    @Column(name = "numero")
    private int numero;

    @ManyToOne(optional = true)
    @JoinColumn(name = "fk_localidad", nullable = true)
    private Localidad localidad;
}
