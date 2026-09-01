package org.example.dtos;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImagenResponseDTO {

    private String id;
    private String nombre;
    private String mime;
}
