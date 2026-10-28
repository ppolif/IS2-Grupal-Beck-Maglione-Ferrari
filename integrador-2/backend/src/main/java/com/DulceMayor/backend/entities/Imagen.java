package com.DulceMayor.backend.entities;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Entidad de dominio que almacena archivos binarios multimedia (logos, fotos de sucursales o productos)
 * según 01-dominio.md.
 */
@Entity
@Table(name = "imagenes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(callSuper = true, exclude = "contenido")
public class Imagen extends EntidadBase {

    @Column(nullable = false, length = 255)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String mime;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(nullable = false)
    private byte[] contenido;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_imagen", nullable = false, length = 50)
    private TipoImagen tipoImagen;
}

