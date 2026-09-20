package com.example.zero.entidades.empresa;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
public class ContactoCorreoElectronico extends Contacto {
    private String email;
}
