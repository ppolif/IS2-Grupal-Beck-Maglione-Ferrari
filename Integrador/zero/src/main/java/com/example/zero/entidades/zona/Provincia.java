package com.example.zero.entidades.zona;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "provincia")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "pais")
public class Provincia {
    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false, length = 36)
    private String id;
    private String nombre;
    private boolean eliminado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pais_id")
    private Pais pais;
}
