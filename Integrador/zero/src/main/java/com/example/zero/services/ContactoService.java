package com.example.zero.services;

import com.example.zero.entidades.compraProveedor.Proveedor;
import com.example.zero.entidades.empresa.Contacto;
import com.example.zero.entidades.empresa.ContactoCorreoElectronico;
import com.example.zero.entidades.empresa.ContactoTelefonico;
import com.example.zero.entidades.persona.Persona;
import com.example.zero.repositories.ContactoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Servicio encargado de gestionar y consultar la jerarquía de Contacto.
 * Encapsula la obtención de correo electrónico y teléfono a través de las subclases correspondientes.
 */
@Service
public class ContactoService {

    private final ContactoRepository contactoRepository;

    public ContactoService(ContactoRepository contactoRepository) {
        this.contactoRepository = contactoRepository;
    }

    /**
     * Obtiene el correo electrónico principal de una Persona mediante ContactoCorreoElectronico.
     */
    public Optional<String> obtenerEmailPrincipal(Persona persona) {
        if (persona == null || persona.getContactos() == null) {
            return Optional.empty();
        }
        return buscarEmailEnContactos(persona.getContactos());
    }

    /**
     * Obtiene el teléfono principal de una Persona mediante ContactoTelefonico.
     */
    public Optional<String> obtenerTelefonoPrincipal(Persona persona) {
        if (persona == null || persona.getContactos() == null) {
            return Optional.empty();
        }
        return buscarTelefonoEnContactos(persona.getContactos());
    }

    /**
     * Obtiene el correo electrónico de un Proveedor mediante ContactoCorreoElectronico.
     */
    public Optional<String> obtenerEmailProveedor(Proveedor proveedor) {
        if (proveedor == null || proveedor.getContactos() == null) {
            return Optional.empty();
        }
        return buscarEmailEnContactos(proveedor.getContactos());
    }

    /**
     * Obtiene el teléfono de un Proveedor mediante ContactoTelefonico.
     */
    public Optional<String> obtenerTelefonoProveedor(Proveedor proveedor) {
        if (proveedor == null || proveedor.getContactos() == null) {
            return Optional.empty();
        }
        return buscarTelefonoEnContactos(proveedor.getContactos());
    }

    private Optional<String> buscarEmailEnContactos(List<Contacto> contactos) {
        for (Contacto c : contactos) {
            if (c instanceof ContactoCorreoElectronico ce && !ce.isEliminado()) {
                if (ce.getEmail() != null && !ce.getEmail().isBlank()) {
                    return Optional.of(ce.getEmail().trim());
                }
            }
        }
        return Optional.empty();
    }

    private Optional<String> buscarTelefonoEnContactos(List<Contacto> contactos) {
        for (Contacto c : contactos) {
            if (c instanceof ContactoTelefonico ct && !ct.isEliminado()) {
                if (ct.getTelefono() != null && !ct.getTelefono().isBlank()) {
                    return Optional.of(ct.getTelefono().trim());
                }
            }
        }
        return Optional.empty();
    }
}
