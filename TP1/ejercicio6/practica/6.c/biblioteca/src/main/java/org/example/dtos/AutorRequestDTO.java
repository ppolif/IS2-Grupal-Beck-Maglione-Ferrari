package org.example.dtos;


import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AutorRequestDTO {

    @NotBlank(message = "El nombre del autor no puede estar vacío")
    private String nombre;
}