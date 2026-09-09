package com.example.zero.entidades.producto;


//import com.example.zero.entidades.compra.Detalle;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.UuidGenerator;

import java.util.HashSet;
import java.util.Set;

/**
 * Entidad Producto
 */
@Entity
@Table(name = "producto")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"subCategoria", "vigenciasPrecio", "detalles"})
public class Producto {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false, length = 36)
    private String id;

    @Column(nullable = false, unique = true, length = 50)
    private String codigo;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(length = 1000)
    private String descripcion;

    @Column(length = 20)
    private String talle;

    @Column(name = "en_oferta", nullable = false)
    @Builder.Default
    private boolean enOferta = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean eliminado = false;

    // Relación con Imagen: cada Producto puede tener una imagen asociada.
    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "imagen_id")
    // private Imagen imagen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subcategoria_id", nullable = false)
    private SubCategoria subCategoria;

    // Historial de precios del producto. Cascade ALL + orphanRemoval porque las vigencias
    // de precio no tienen sentido de existir sin su Producto (relación de composición).
    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<VigenciaPrecio> vigenciasPrecio = new HashSet<>();

    // Lado inverso: un Producto puede aparecer en muchos Detalles de factura.
    @OneToMany(mappedBy = "producto", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Detalle> detalles = new HashSet<>();
}