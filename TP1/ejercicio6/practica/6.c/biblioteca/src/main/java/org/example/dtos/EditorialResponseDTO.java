package org.example.dtos;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EditorialResponseDTO {

    private String id;
    private String nombre;
    private boolean alta;
}
