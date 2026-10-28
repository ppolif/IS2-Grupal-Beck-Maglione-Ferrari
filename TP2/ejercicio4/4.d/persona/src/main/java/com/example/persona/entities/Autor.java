package com.example.persona.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

import com.example.persona.patterns.prototype.Prototype;

@Entity
@Table(name = "autor")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Audited
public class Autor extends Base implements Prototype<Autor>, Cloneable {

    @Column(name="nombre")
    private String nombre;

    @Column(name="apellido")
    private String apellido;

    @Column(name="biografia", length = 1500)
    private String biografia;

    @Override
    public Autor clonar() {
        Autor clon = new Autor();
        clon.setNombre(this.nombre != null ? this.nombre + " - Copia" : " - Copia");
        clon.setApellido(this.apellido);
        clon.setBiografia(this.biografia);
        // id queda nulo para permitir persistir como nueva entidad
        return clon;
    }

    public Autor clonarExacto() {
        Autor clon = new Autor();
        clon.setNombre(this.nombre);
        clon.setApellido(this.apellido);
        clon.setBiografia(this.biografia);
        return clon;
    }

    @Override
    public Autor clone() {
        return this.clonar();
    }
}

