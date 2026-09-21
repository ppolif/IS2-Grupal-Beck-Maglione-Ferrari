package com.example.zero.services.persona;

import com.example.zero.entidades.persona.Cliente;
import com.example.zero.entidades.persona.Nacionalidad;
import com.example.zero.entidades.persona.Usuario;
import com.example.zero.enums.TipoDocumento;
import com.example.zero.repositories.ClienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    public void validar(String numeroDocumento, String nombre, String apellido) {
        if (numeroDocumento == null || numeroDocumento.trim().isEmpty()) {
            throw new IllegalArgumentException("El número de documento no puede estar vacío");
        }
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío");
        }
        if (apellido == null || apellido.trim().isEmpty()) {
            throw new IllegalArgumentException("El apellido no puede estar vacío");
        }
    }

    @Transactional
    public Cliente crearCliente(String numeroDocumento, String nombre, String apellido,
                                LocalDate fechaNacimiento, TipoDocumento tipoDocumento,
                                Nacionalidad nacionalidad) {
        validar(numeroDocumento, nombre, apellido);
        String docLimpio = numeroDocumento.trim();

        Optional<Cliente> existente = clienteRepository.findByNumeroDocumentoAndEliminadoFalse(docLimpio);
        if (existente.isPresent()) {
            throw new IllegalArgumentException("Ya existe un cliente activo con el documento: " + docLimpio);
        }

        Cliente cliente = Cliente.builder()
                .numeroDocumento(docLimpio)
                .nombre(nombre.trim())
                .apellido(apellido.trim())
                .fechaNacimiento(fechaNacimiento != null ? fechaNacimiento : LocalDate.of(2000, 1, 1))
                .tipoDocumento(tipoDocumento != null ? tipoDocumento : TipoDocumento.DNI)
                .nacionalidad(nacionalidad)
                .eliminado(false)
                .build();

        return clienteRepository.save(cliente);
    }

    @Transactional
    public Cliente modificarCliente(String numeroDocumento, String nombre, String apellido,
                                    LocalDate fechaNacimiento, TipoDocumento tipoDocumento,
                                    Nacionalidad nacionalidad) {
        Cliente cliente = buscarPorDocumento(numeroDocumento);

        if (nombre != null && !nombre.trim().isEmpty()) {
            cliente.setNombre(nombre.trim());
        }
        if (apellido != null && !apellido.trim().isEmpty()) {
            cliente.setApellido(apellido.trim());
        }
        if (fechaNacimiento != null) {
            cliente.setFechaNacimiento(fechaNacimiento);
        }
        if (tipoDocumento != null) {
            cliente.setTipoDocumento(tipoDocumento);
        }
        if (nacionalidad != null) {
            cliente.setNacionalidad(nacionalidad);
        }

        return clienteRepository.save(cliente);
    }

    @Transactional
    public void eliminarCliente(String numeroDocumento) {
        Cliente cliente = buscarPorDocumento(numeroDocumento);
        cliente.setEliminado(true);
        clienteRepository.save(cliente);
    }

    @Transactional(readOnly = true)
    public Cliente buscarPorDocumento(String numeroDocumento) {
        if (numeroDocumento == null || numeroDocumento.trim().isEmpty()) {
            throw new IllegalArgumentException("El número de documento no puede ser nulo o vacío");
        }
        return clienteRepository.findByNumeroDocumentoAndEliminadoFalse(numeroDocumento.trim())
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el cliente con documento: " + numeroDocumento));
    }

    @Transactional
    public Cliente asociarClienteUsuario(String numeroDocumento, Usuario usuario) {
        Cliente cliente = buscarPorDocumento(numeroDocumento);
        cliente.setUsuario(usuario);
        return clienteRepository.save(cliente);
    }

    @Transactional(readOnly = true)
    public List<Cliente> listarActivos() {
        return clienteRepository.findByEliminadoFalse();
    }

    @Transactional(readOnly = true)
    public List<Cliente> listarTodos() {
        return clienteRepository.findAll();
    }
}

