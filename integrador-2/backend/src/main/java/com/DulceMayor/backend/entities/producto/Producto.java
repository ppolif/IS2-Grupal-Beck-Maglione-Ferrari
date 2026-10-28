package com.DulceMayor.backend.entities.producto;

import com.DulceMayor.backend.entities.EntidadBase;
import com.DulceMayor.backend.entities.Imagen;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Entidad de dominio que representa un producto del catálogo mayorista según 01-dominio.md.
 */
@Entity
@Table(name = "productos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(callSuper = true, exclude = {"subCategoria", "imagen"})
public class Producto extends EntidadBase {

    @Column(nullable = false, unique = true, length = 50)
    private String codigo;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    @Column(nullable = false, length = 100)
    private String presentacion;

    @Column(name = "unidades_por_bulto", nullable = false)
    private int unidadesPorBulto;

    @Builder.Default
    @Column(name = "en_oferta", nullable = false)
    private boolean enOferta = false;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subcategoria_id", nullable = false)
    private SubCategoria subCategoria;

    //many to one por borrado logico
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "imagen_id")
    private Imagen imagen;
}

