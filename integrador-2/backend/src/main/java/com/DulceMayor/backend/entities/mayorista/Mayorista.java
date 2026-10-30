package com.DulceMayor.backend.entities.mayorista;

import com.DulceMayor.backend.entities.EntidadBase;
import com.DulceMayor.backend.entities.Imagen;
import com.DulceMayor.backend.entities.contacto.Contacto;
import com.DulceMayor.backend.entities.direccion.Direccion;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Entidad de dominio que representa a la empresa dueña del sistema (Dulce Mayor),
 * distinguiendo entre su sede central y sus sucursales.
 */
@Entity
@Table(name = "mayoristas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(callSuper = true, exclude = {"direcciones", "contactos", "imagenes"})
public class Mayorista extends EntidadBase {

    @Column(name = "razon_social", nullable = false, length = 150)
    private String razonSocial;

    @Column(nullable = false, unique = true, length = 20)
    private String cuit;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_empresa", nullable = false, length = 20)
    private TipoEmpresa tipoEmpresa;

    @Builder.Default
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "mayorista_id")
    private List<Direccion> direcciones = new ArrayList<>();

    @Builder.Default
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "mayorista_id")
    private List<Contacto> contactos = new ArrayList<>();

    @Builder.Default
    @ManyToMany
    @JoinTable(
            name = "mayorista_imagenes",
            joinColumns = @JoinColumn(name = "mayorista_id"),
            inverseJoinColumns = @JoinColumn(name = "imagen_id")
    )
    private List<Imagen> imagenes = new ArrayList<>();
}

