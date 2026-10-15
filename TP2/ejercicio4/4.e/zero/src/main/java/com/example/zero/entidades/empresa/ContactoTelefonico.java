package com.example.zero.entidades.empresa;

import com.example.zero.enums.TipoTelefono;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "contacto_telefonico")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ContactoTelefonico extends Contacto {
    private String telefono;

    @Enumerated(EnumType.STRING)
    private TipoTelefono tipoTelefono;
}
