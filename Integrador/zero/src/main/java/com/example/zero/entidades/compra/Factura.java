package com.example.zero.entidades.compra;



import com.example.zero.entidades.empresa.Contacto;
import com.example.zero.entidades.empresa.ContactoCorreoElectronico;
import com.example.zero.entidades.empresa.ContactoTelefonico;
import com.example.zero.entidades.persona.Cliente;
import com.example.zero.entidades.persona.Empleado;
import com.example.zero.entidades.zona.Direccion;
import com.example.zero.enums.EstadoFactura;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
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
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Entidad Factura
 */
@Entity
@Table(name = "factura")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"cliente", "formaDePago", "detalles"})
public class Factura {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false, length = 36)
    private String id;

    @Column(name = "numero_factura", nullable = false, unique = true)
    private Long numeroFactura;

    // El diagrama usa "Date"; se moderniza a LocalDateTime para JPA/Java actual.
    @Column(name = "fecha_factura", nullable = false)
    private LocalDateTime fechaFactura;

    @Column(name = "total_pagado", nullable = false)
    private double totalPagado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoFactura estado;

    @Column(nullable = false)
    @Builder.Default
    private boolean eliminado = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "forma_de_pago_id", nullable = false)
    private FormaDePago formaDePago;

    // Composición: los Detalles no existen sin su Factura.
    @OneToMany(mappedBy = "factura", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Detalle> detalles = new HashSet<>();

    public String getOrderNumber() {
        return numeroFactura != null ? "#ORD-" + numeroFactura : (id != null ? "#ORD-" + id : "");
    }

    public String getCustomerName() {
        if (cliente != null) {
            String nom = cliente.getNombre() != null ? cliente.getNombre().trim() : "";
            String ape = cliente.getApellido() != null ? cliente.getApellido().trim() : "";
            String completo = (nom + " " + ape).trim();
            if (!completo.isEmpty()) {
                return completo;
            }
        }
        return "Cliente Final";
    }

    public String getCustomerEmail() {
        if (cliente != null) {
            if (cliente.getUsuario() != null && cliente.getUsuario().getNombreUsuario() != null && !cliente.getUsuario().getNombreUsuario().isBlank()) {
                return cliente.getUsuario().getNombreUsuario().trim();
            }
            if (cliente.getContactos() != null) {
                for (Contacto c : cliente.getContactos()) {
                    if (c instanceof ContactoCorreoElectronico ce && !ce.isEliminado() && ce.getEmail() != null && !ce.getEmail().isBlank()) {
                        return ce.getEmail().trim();
                    }
                }
            }
            if (cliente.getNumeroDocumento() != null && !cliente.getNumeroDocumento().isBlank()) {
                return "DNI: " + cliente.getNumeroDocumento().trim();
            }
        }
        return "N/A";
    }

    public String getCustomerAvatar() {
        if (cliente != null && cliente.getUsuario() != null) {
            String foto = cliente.getUsuario().getFoto();
            if (foto != null && !foto.isBlank()) {
                return foto.trim();
            }
        }
        return null;
    }

    public String getCustomerPhone() {
        if (cliente != null && cliente.getContactos() != null) {
            for (Contacto c : cliente.getContactos()) {
                if (c instanceof ContactoTelefonico ct && !ct.isEliminado() && ct.getTelefono() != null && !ct.getTelefono().isBlank()) {
                    return ct.getTelefono().trim();
                }
            }
        }
        return null;
    }

    public String getShippingAddress() {
        if (cliente != null && cliente.getDireccion() != null) {
            for (Direccion dir : cliente.getDireccion()) {
                if (!dir.isEliminado() && dir.getCalle() != null && !dir.getCalle().isBlank()) {
                    String calle = dir.getCalle().trim();
                    String num = dir.getNumeracion() != null ? dir.getNumeracion().trim() : "";
                    String res = (calle + " " + num).trim();
                    if (dir.getLocalidad() != null && dir.getLocalidad().getNombre() != null) {
                        res += ", " + dir.getLocalidad().getNombre().trim();
                    }
                    return res;
                }
            }
        }
        return null;
    }

    public String getShippingCity() {
        if (cliente != null && cliente.getDireccion() != null) {
            for (Direccion dir : cliente.getDireccion()) {
                if (!dir.isEliminado() && dir.getLocalidad() != null && dir.getLocalidad().getNombre() != null) {
                    return dir.getLocalidad().getNombre().trim();
                }
            }
        }
        return null;
    }

    public String getShippingZip() {
        if (cliente != null && cliente.getDireccion() != null) {
            for (Direccion dir : cliente.getDireccion()) {
                if (!dir.isEliminado() && dir.getLocalidad() != null && dir.getLocalidad().getCodigoPostal() != null) {
                    return dir.getLocalidad().getCodigoPostal().trim();
                }
            }
        }
        return null;
    }

    public String getPaymentMethod() {
        if (formaDePago != null && formaDePago.getTipoPago() != null) {
            return formaDePago.getTipoPago().name().replace('_', ' ');
        }
        return "Efectivo";
    }

    public String getStatus() {
        if (estado != null) {
            return estado.name();
        }
        return "PAGADA";
    }

    public LocalDateTime getCreatedAt() {
        return fechaFactura;
    }

    public LocalDateTime getDate() {
        return fechaFactura;
    }

    public double getTotalAmount() {
        return totalPagado;
    }

    public double getSubtotal() {
        return totalPagado;
    }

    public Set<Detalle> getItems() {
        return detalles != null ? detalles : Collections.emptySet();
    }

    public String getProductSummary() {
        if (detalles == null || detalles.isEmpty()) {
            return "Venta General";
        }
        StringBuilder sb = new StringBuilder();
        for (Detalle d : detalles) {
            if (!d.isEliminado() && d.getProducto() != null) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(d.getProducto().getNombre()).append(" (x").append(d.getCantidad()).append(")");
            }
        }
        return sb.length() > 0 ? sb.toString() : "Venta General";
    }

    public String getCategoryName() {
        if (detalles != null) {
            for (Detalle d : detalles) {
                if (!d.isEliminado() && d.getProducto() != null && d.getProducto().getSubCategoria() != null
                        && d.getProducto().getSubCategoria().getCategoria() != null) {
                    return d.getProducto().getSubCategoria().getCategoria().getNombre();
                }
            }
        }
        return "General";
    }
}