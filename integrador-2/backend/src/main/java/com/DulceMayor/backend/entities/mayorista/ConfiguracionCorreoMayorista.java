package com.DulceMayor.backend.entities.mayorista;

import com.DulceMayor.backend.entities.EntidadBase;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Entidad de dominio que almacena la configuración de correo saliente SMTP para un {@link Mayorista}.
 */
@Entity
@Table(name = "configuraciones_correo_mayorista")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(callSuper = true, exclude = {"clave", "mayorista"})
public class ConfiguracionCorreoMayorista extends EntidadBase {

    @Column(nullable = false, length = 150)
    private String correo;

    @JsonIgnore
    @Column(nullable = false, length = 255)
    private String clave;

    @Column(nullable = false, length = 10)
    private String puerto;

    @Column(nullable = false, length = 100)
    private String smtp;

    @Column(nullable = false)
    private boolean tls;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mayorista_id", nullable = false, unique = true)
    private Mayorista mayorista;
}

