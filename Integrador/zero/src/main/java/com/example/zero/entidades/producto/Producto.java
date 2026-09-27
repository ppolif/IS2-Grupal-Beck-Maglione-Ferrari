package com.example.zero.entidades.producto;

import com.example.zero.entidades.Imagen;
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
import java.util.List;

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
@ToString(exclude = {"subCategoria", "imagenes"})
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

    @Column(name = "stock", nullable = false)
    @Builder.Default
    private int stock = 0;

    @Column(name = "en_oferta", nullable = false)
    @Builder.Default
    private boolean enOferta = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean eliminado = false;

    // Relación con Imagen: cada Producto puede tener muchas imagenes (por borrado logico).
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id")
    private List<Imagen> imagenes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subcategoria_id", nullable = false)
    private SubCategoria subCategoria;

    @jakarta.persistence.Transient
    private Double precioActual;

    public String getImagenUrl() {
        if (imagenes != null && !imagenes.isEmpty()) {
            for (Imagen img : imagenes) {
                if (img != null && !img.isEliminado() && img.getId() != null) {
                    return "/imagen/" + img.getId();
                }
            }
        }
        return "/shop/img/product/p1.jpg";
    }

    public String getEstadoStock() {
        if (stock > 50) {
            return "BIEN";
        } else if (stock >= 20) {
            return "REGULAR";
        } else {
            return "MALO";
        }
    }

    public int getCantidadFaltanteStock() {
        return Math.max(0, 50 - stock);
    }

    public String getUrlWhatsAppReposicion() {
        int faltante = getCantidadFaltanteStock();
        String mensaje = String.format(
            "Hola! Desde ZERO Tienda Oficial deseamos hacer un pedido de reposición para el producto: %s (Código: %s). Solicitamos %d unidades para alcanzar el nivel óptimo de 50 unidades (Stock actual: %d).",
            nombre != null ? nombre : "Producto",
            codigo != null ? codigo : "",
            faltante,
            stock
        );
        try {
            return "https://web.whatsapp.com/send?phone=5492613072339&text=" + java.net.URLEncoder.encode(mensaje, java.nio.charset.StandardCharsets.UTF_8.toString());
        } catch (Exception e) {
            return "https://web.whatsapp.com/send?phone=5492613072339";
        }
    }
}
