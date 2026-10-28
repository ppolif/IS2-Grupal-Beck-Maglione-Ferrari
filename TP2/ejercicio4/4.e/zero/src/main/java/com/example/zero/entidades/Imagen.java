package com.example.zero.entidades;

import com.example.zero.enums.TipoImagen;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "imagen")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Imagen {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", length = 36)
    private String id;

    private String nombre;
    private String mime;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "contenido", columnDefinition = "LONGBLOB")
    private byte[] contenido;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_imagen")
    private TipoImagen tipoImagen;

    @Builder.Default
    @Column(nullable = false)
    private boolean eliminado = false;
}
