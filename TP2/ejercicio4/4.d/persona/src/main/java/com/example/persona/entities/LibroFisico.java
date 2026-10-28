package com.example.persona.entities;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

import java.time.LocalDate;
import java.util.List;

@Entity
@DiscriminatorValue("FISICO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Audited
public class LibroFisico extends Libro {

    @Column(name = "numero_estante")
    private String numeroEstante;

    @Column(name = "peso_gramos")
    private Integer pesoGramos;

    public LibroFisico(String titulo, int fecha, String genero, int paginas,
                       LocalDate fechaVencimientoDevolucion, List<Autor> autores,
                       String numeroEstante, Integer pesoGramos) {
        super(titulo, fecha, genero, paginas, fechaVencimientoDevolucion, autores);
        this.numeroEstante = numeroEstante;
        this.pesoGramos = pesoGramos;
    }
}

