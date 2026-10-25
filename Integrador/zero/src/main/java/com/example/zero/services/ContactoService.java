package com.example.zero.services;

import com.example.zero.entidades.compraProveedor.Proveedor;
import com.example.zero.entidades.empresa.Contacto;
import com.example.zero.entidades.empresa.ContactoCorreoElectronico;
import com.example.zero.entidades.empresa.ContactoTelefonico;
import com.example.zero.entidades.persona.Persona;
import com.example.zero.enums.TipoContacto;
import com.example.zero.enums.TipoTelefono;
import com.example.zero.repositories.ContactoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
public class ContactoService {

    private final ContactoRepository contactoRepository;

    public ContactoService(ContactoRepository contactoRepository) {
        this.contactoRepository = contactoRepository;
    }


    public ContactoCorreoElectronico crearContactoCorreo(String email, TipoContacto tipoContacto, String observacion) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("El correo electrónico de contacto no puede estar vacío");
        }

        ContactoCorreoElectronico contacto = ContactoCorreoElectronico.builder()
                .email(email.trim().toLowerCase())
                .tipoContacto(tipoContacto)
                .observacion(observacion)
                .eliminado(false)
                .build();
        return contactoRepository.save(contacto);
    }

    public ContactoTelefonico crearContactoTelefonico(String telefono, TipoTelefono tipoTelefono, TipoContacto tipoContacto,String observacion) {
        if (telefono == null || telefono.trim().isEmpty()) {
            throw new IllegalArgumentException("Debe ingresar un número de teléfono de contacto");
        }
        ContactoTelefonico contacto = ContactoTelefonico.builder()
                .telefono(telefono.trim())
                .tipoTelefono(tipoTelefono)
                .tipoContacto(tipoContacto)
                .observacion(observacion)
                .eliminado(false)
                .build();
        return contactoRepository.save(contacto);
    }


    ////correo electrónico principal de una persona mediante ContactoCorreoElectronico.
    public Optional<String> obtenerEmailPrincipal(Persona persona) {
        if (persona == null || persona.getContactos() == null) {
            return Optional.empty();
        }
        return buscarEmailEnContactos(persona.getContactos());
    }

    ///teléfono principal de una Persona mediante ContactoTelefonico.
    public Optional<String> obtenerTelefonoPrincipal(Persona persona) {
        if (persona == null || persona.getContactos() == null) {
            return Optional.empty();
        }
        return buscarTelefonoEnContactos(persona.getContactos());
    }

    ///correo electrónico de un proveedor mediante ContactoCorreoElectronico.
    public Optional<String> obtenerEmailProveedor(Proveedor proveedor) {
        if (proveedor == null || proveedor.getContactos() == null) {
            return Optional.empty();
        }
        return buscarEmailEnContactos(proveedor.getContactos());
    }

    ///teléfono de un Proveedor mediante ContactoTelefonico.
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
