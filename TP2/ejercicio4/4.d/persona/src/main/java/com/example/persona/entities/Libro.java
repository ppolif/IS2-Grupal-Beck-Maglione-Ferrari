package com.example.persona.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

import java.time.LocalDate;
import java.util.List;

import com.example.persona.patterns.builder.LibroBuilder;

@Entity
@Table(name="libro")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="tipo_libro", discriminatorType = DiscriminatorType.STRING)
@DiscriminatorValue("LIBRO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Audited
public class Libro extends Base {

    public static LibroBuilder builder() {
        return new LibroBuilder();
    }

    @Column(name="titulo")
    private String titulo;

    @Column(name="fecha")
    private int fecha;

    @Column(name="genero")
    private String genero;

    @Column(name="paginas")
    private int paginas;

    @Column(name="fecha_vencimiento_devolucion")
    private LocalDate fechaVencimientoDevolucion;

    @ManyToMany(cascade = CascadeType.REFRESH)
    private List<Autor> autores;
}
