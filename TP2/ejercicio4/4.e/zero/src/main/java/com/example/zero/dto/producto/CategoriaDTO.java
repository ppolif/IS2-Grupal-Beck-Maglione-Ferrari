package com.example.zero.dto.producto;

import com.example.zero.entidades.producto.SubCategoria;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoriaDTO {

    private String id;
    private String nombre;

    @Builder.Default
    private List<SubCategoria> subCategorias = new ArrayList<>();

    // Alias para compatibilidad con vistas o plantillas que usen .name
    public String getName() {
        return nombre;
    }
}
