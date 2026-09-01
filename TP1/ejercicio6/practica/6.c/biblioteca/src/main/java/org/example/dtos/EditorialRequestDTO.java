package org.example.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EditorialRequestDTO {

    @NotBlank(message = "El nombre de la editorial no puede estar vacío")
    private String nombre;
}