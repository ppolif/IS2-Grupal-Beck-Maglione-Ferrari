package com.DulceMayor.backend.entities.contacto;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Subclase concreta de {@link Contacto} para direcciones de correo electrónico.
 *
 * <p>Nota sobre {@code SINGLE_TABLE}: la columna {@code email} es obligatoria en la lógica de
 * negocio para {@code medio = "CORREO"}, pero permanece anulable a nivel de esquema DDL porque
 * comparte la tabla {@code contactos} con {@link ContactoTelefonico}.</p>
 */
@Entity
@DiscriminatorValue(Contacto.MEDIO_CORREO)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
public class ContactoCorreoElectronico extends Contacto {

    @Column(length = 150)
    private String email;

    @Override
    public String getMedio() {
        return MEDIO_CORREO;
    }
}

