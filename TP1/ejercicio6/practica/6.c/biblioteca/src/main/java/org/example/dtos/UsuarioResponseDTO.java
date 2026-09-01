package org.example.dtos;

import org.example.entidades.enumeraciones.Rol;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioResponseDTO {
    private String id;
    private long dni;
    private String nombre;
    private String telefono;
    private String mail;
    private Rol rol;
    private boolean alta;
}
