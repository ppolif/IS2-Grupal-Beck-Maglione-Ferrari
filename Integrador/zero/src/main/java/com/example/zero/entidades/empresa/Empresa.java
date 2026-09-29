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


    @OneToMany
    @JoinColumn(name = "empresa_id")
    private List<Direccion> direccion;

    @OneToMany
    @JoinColumn(name = "empresa_id")
    private List<Contacto> contactos;

    public String getDireccionCompleta() {
        if (direccion != null && !direccion.isEmpty()) {
            for (Direccion dir : direccion) {
                if (dir != null && !dir.isEliminado() && dir.getCalle() != null && !dir.getCalle().isBlank()) {
                    String calle = dir.getCalle().trim();
                    String num = dir.getNumeracion() != null ? dir.getNumeracion().trim() : "";
                    String loc = (dir.getLocalidad() != null && dir.getLocalidad().getNombre() != null) ? ", " + dir.getLocalidad().getNombre().trim() : "";
                    return (calle + " " + num + loc).trim();
                }
            }
        }
        return "Av. Corrientes 1234, CABA";
    }
}
