package org.example.dtos;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AutorResponseDTO {

    private String id;
    private String nombre;
    private boolean alta;
}
