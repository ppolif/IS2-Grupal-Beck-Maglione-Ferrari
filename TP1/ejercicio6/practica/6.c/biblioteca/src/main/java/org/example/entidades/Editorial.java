package org.example.entidades;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "editoriales")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Editorial {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private boolean alta;
}