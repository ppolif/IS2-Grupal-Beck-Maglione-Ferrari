package com.example.zero.entidades.empresa;

import com.example.zero.entidades.zona.Direccion;
import com.example.zero.enums.TipoEmpresa;
import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Data
@Entity
public class Empresa {
    @Id
    private String id;
    private String razonSocial;
    private String cuit;

    @Enumerated(EnumType.STRING)
    private TipoEmpresa tipoSucursal;

    private boolean eliminado;

    ////PREGUNTAR
    @OneToOne
    private Direccion direccion;

    @OneToMany
    @JoinColumn(name = "empresa_id")
    private List<Contacto> contactos;
}
