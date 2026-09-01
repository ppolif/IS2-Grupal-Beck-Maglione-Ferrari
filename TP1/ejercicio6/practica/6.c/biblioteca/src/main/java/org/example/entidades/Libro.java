package org.example.entidades;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "libros")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Libro {

    @Id
    @Column(name = "isbn", unique = true, nullable = false)
    private Long isbn; // Sin @GeneratedValue, el usuario lo ingresa

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false)
    private int anio;

    @Column(nullable = false)
    private int ejemplares;

    @Column(nullable = false)
    private int ejemplaresPrestados;

    @Column(nullable = false)
    private int ejemplaresRestantes;

    @Column(nullable = false)
    private boolean alta;

    // Relaciones
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "editorial_id")
    private Editorial editorial;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "libro_autor",
            joinColumns = @JoinColumn(name = "libro_isbn"),
            inverseJoinColumns = @JoinColumn(name = "autor_id")
    )
    private List<Autor> autores;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "imagen_id")
    private Imagen imagen;
}