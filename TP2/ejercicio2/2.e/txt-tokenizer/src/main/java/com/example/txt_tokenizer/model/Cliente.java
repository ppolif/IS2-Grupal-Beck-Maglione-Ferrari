package com.example.txt_tokenizer.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Cliente {
    private Integer id;
    private String nombre;
    private String apellido;
    private String direccion;
}
