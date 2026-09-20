package com.example.zero.entidades.empresa;

import com.example.zero.enums.TipoTelefono;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
public class ContactoTelefonico extends Contacto {
    private String telefono;

    @Enumerated(EnumType.STRING)
    private TipoTelefono tipoTelefono;
}
