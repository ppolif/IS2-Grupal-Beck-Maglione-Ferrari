package com.DulceMayor.backend.entities.contacto;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Subclase concreta de {@link Contacto} para líneas telefónicas (fijas o celulares).
 *
 * <p>Nota sobre {@code SINGLE_TABLE}: las columnas {@code telefono} y {@code tipo_telefono} son
 * obligatorias en la lógica de negocio para {@code medio = "TELEFONICO"}, pero permanecen anulables
 * a nivel de esquema DDL porque comparten la tabla {@code contactos} con {@link ContactoCorreoElectronico}.</p>
 */
@Entity
@DiscriminatorValue(Contacto.MEDIO_TELEFONICO)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
public class ContactoTelefonico extends Contacto {

    @Column(length = 30)
    private String telefono;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_telefono", length = 20)
    private TipoTelefono tipoTelefono;

    @Override
    public String getMedio() {
        return MEDIO_TELEFONICO;
    }
}

