package com.example.zero.entidades.empresa;

import com.example.zero.entidades.zona.Direccion;
import com.example.zero.enums.TipoEmpresa;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "empresa")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"direccion", "contactos"})
public class Empresa {

    @Id
    private String id;
    private String razonSocial;
    private String cuit;

    @Enumerated(EnumType.STRING)
    private TipoEmpresa tipoSucursal;

    private boolean eliminado;

    @OneToMany
    @JoinColumn(name = "empresa_id")
    private List<Direccion> direccion;

    @OneToMany
    @JoinColumn(name = "empresa_id")
    private List<Contacto> contactos;
}
