package org.example.entidades;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "imagenes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Imagen {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String mime;

    @Lob
    @Basic(fetch = FetchType.LAZY) // Evita cargar los bytes a memoria si solo consultamos metadatos
    @Column(columnDefinition = "LONGBLOB", nullable = false)
    private byte[] contenido;
}