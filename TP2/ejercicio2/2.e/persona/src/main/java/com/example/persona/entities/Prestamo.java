package com.example.persona.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

import java.time.LocalDate;

@Entity
@Table(name = "prestamo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Audited
public class Prestamo extends Base {

    @Column(name = "fecha_prestamo")
    private LocalDate fechaPrestamo;

    @Column(name = "fecha_devolucion")
    private LocalDate fechaDevolucion;

    @Column(name = "estado")
    private String estado; // "ACTIVO", "DEVUELTO"

    @ManyToOne(optional = false)
    @JoinColumn(name = "persona_id")
    private Persona persona;

    @ManyToOne(optional = false)
    @JoinColumn(name = "libro_id")
    private Libro libro;
}
