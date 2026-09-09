package com.example.zero.entidades.persona;

import com.example.zero.entidades.compra.Factura;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

/**
 * Entidad Cliente (paquete "persona" del diagrama), hereda de Persona.
 *
 * Los métodos de negocio del diagrama (crearCliente, validar, modificarCliente, listarCliente,
 * asociarClienteUsuario) corresponden a la capa de servicio (ClienteService), no a la entidad.
 */
@Entity
@Table(name = "cliente")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(callSuper = true, exclude = {"nacionalidad", "facturas"})
public class Cliente extends Persona {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nacionalidad_id", nullable = false)
    private Nacionalidad nacionalidad;

    // Lado inverso: un Cliente puede tener muchas Facturas.
    @OneToMany(mappedBy = "cliente", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Factura> facturas = new HashSet<>();
}