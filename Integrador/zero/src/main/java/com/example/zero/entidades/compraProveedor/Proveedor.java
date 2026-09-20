package com.example.zero.entidades.compraProveedor;

import com.example.zero.entidades.empresa.Contacto;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
public class Proveedor {
    @Id
    private String id;
    private String razonSocial;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "proveedor_id")
    private List<Contacto> contactos;
}
