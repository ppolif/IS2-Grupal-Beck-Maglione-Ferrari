package com.DulceMayor.backend.entities.facturacion;

import com.DulceMayor.backend.entities.EntidadBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Entidad de dominio que representa un medio de pago del catálogo según {@code 01-dominio.md}.
 * <p>
 * El atributo {@code observacion} almacena información complementaria del medio de pago,
 * por ejemplo el alias bancario para transferencias.
 * </p>
 */
@Entity
@Table(name = "formas_de_pago")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
public class FormaDePago extends EntidadBase {

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_pago", nullable = false, length = 30)
    private TipoPago tipoPago;

    @Column(length = 255)
    private String observacion;
}

