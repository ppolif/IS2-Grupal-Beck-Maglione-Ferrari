package com.example.zero.entidades.empresa;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "contacto_correo_electronico")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ContactoCorreoElectronico extends Contacto {
    private String email;
}
