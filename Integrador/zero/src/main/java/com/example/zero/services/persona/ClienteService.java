package com.example.zero.services.persona;

import com.example.zero.dto.persona.ClienteRegistroDTO;
import com.example.zero.entidades.Imagen;
import com.example.zero.entidades.empresa.Contacto;
import com.example.zero.entidades.empresa.ContactoCorreoElectronico;
import com.example.zero.entidades.empresa.ContactoTelefonico;
import com.example.zero.entidades.persona.Cliente;
import com.example.zero.entidades.persona.Nacionalidad;
import com.example.zero.entidades.persona.Usuario;
import com.example.zero.entidades.zona.Direccion;
import com.example.zero.entidades.zona.Localidad;
import com.example.zero.enums.RolUsuario;
import com.example.zero.enums.TipoContacto;
import com.example.zero.enums.TipoDocumento;
import com.example.zero.enums.TipoImagen;
import com.example.zero.enums.TipoTelefono;
import com.example.zero.repositories.*;
import com.example.zero.services.ImagenService;
import com.example.zero.services.zona.ZonaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final NacionalidadRepository nacionalidadRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;
    private final ZonaService zonaService;
    private final DireccionRepository direccionRepository;
    private final ContactoRepository contactoRepository;
    private final ImagenService imagenService;

    public ClienteService(ClienteRepository clienteRepository) {
        this(clienteRepository, null, null, null, null, null, null, null);
    }

    public ClienteService(ClienteRepository clienteRepository,
                          NacionalidadRepository nacionalidadRepository,
                          UsuarioRepository usuarioRepository,
                          UsuarioService usuarioService,
                          ZonaService zonaService,
                          DireccionRepository direccionRepository,
                          ContactoRepository contactoRepository) {
        this(clienteRepository, nacionalidadRepository, usuarioRepository, usuarioService, zonaService, direccionRepository, contactoRepository, null);
    }

    @Autowired
    public ClienteService(ClienteRepository clienteRepository,
                          NacionalidadRepository nacionalidadRepository,
                          UsuarioRepository usuarioRepository,
                          UsuarioService usuarioService,
                          ZonaService zonaService,
                          DireccionRepository direccionRepository,
                          ContactoRepository contactoRepository,
                          ImagenService imagenService) {
        this.clienteRepository = clienteRepository;
        this.nacionalidadRepository = nacionalidadRepository;
        this.usuarioRepository = usuarioRepository;
        this.usuarioService = usuarioService;
        this.zonaService = zonaService;
        this.direccionRepository = direccionRepository;
        this.contactoRepository = contactoRepository;
        this.imagenService = imagenService;
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
    public Usuario registrarCliente(ClienteRegistroDTO dto) {
        return registrarCliente(dto, null);
    }

    @Transactional
    public Usuario registrarCliente(ClienteRegistroDTO dto, MultipartFile fotoPerfil) {
        if (dto == null) {
            throw new IllegalArgumentException("Los datos de registro no pueden ser nulos");
        }

        // 1. Validar Credenciales
        if (dto.getPassword() == null || dto.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("La contraseña no puede estar vacía");
        }
        if (dto.getPassword().length() < 4) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 4 caracteres");
        }
        if (dto.getConfirmPassword() == null || !dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("Las contraseñas no coinciden");
        }
        if (dto.getEmail() == null || dto.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("El correo electrónico no puede estar vacío");
        }
        String emailLimpio = dto.getEmail().trim().toLowerCase();
        if (usuarioRepository != null && usuarioRepository.findByNombreUsuarioAndEliminadoFalse(emailLimpio).isPresent()) {
            throw new IllegalArgumentException("Ya existe un usuario activo con el correo: " + emailLimpio);
        }

        // 2. Validar Datos Personales
        validar(dto.getNumeroDocumento(), dto.getNombre(), dto.getApellido());
        String docLimpio = dto.getNumeroDocumento().trim();
        if (clienteRepository.findByNumeroDocumentoAndEliminadoFalse(docLimpio).isPresent()) {
            throw new IllegalArgumentException("Ya existe un cliente activo con el documento: " + docLimpio);
        }
        if (dto.getFechaNacimiento() == null) {
            throw new IllegalArgumentException("Debe ingresar su fecha de nacimiento");
        }
        if (dto.getFechaNacimiento().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de nacimiento no puede ser posterior a la actual");
        }

        // 3. Validar Nacionalidad
        if (dto.getNacionalidadId() == null || dto.getNacionalidadId().trim().isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar una nacionalidad");
        }
        Nacionalidad nacionalidad = null;
        if (nacionalidadRepository != null) {
            nacionalidad = nacionalidadRepository.findActive(dto.getNacionalidadId().trim())
                    .orElseThrow(() -> new IllegalArgumentException("La nacionalidad seleccionada no es válida"));
        }

        // 4. Validar Ubicación y Domicilio
        if (dto.getLocalidadId() == null || dto.getLocalidadId().trim().isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar una localidad para el domicilio");
        }
        Localidad localidad = null;
        if (zonaService != null) {
            localidad = zonaService.buscarLocalidadPorId(dto.getLocalidadId().trim());
        }
        if (dto.getCalle() == null || dto.getCalle().trim().isEmpty()) {
            throw new IllegalArgumentException("La calle de la dirección no puede estar vacía");
        }
        if (dto.getNumeracion() == null || dto.getNumeracion().trim().isEmpty()) {
            throw new IllegalArgumentException("La numeración de la dirección no puede estar vacía");
        }

        Direccion direccion = new Direccion();
        direccion.setCalle(dto.getCalle().trim());
        direccion.setNumeracion(dto.getNumeracion().trim());
        direccion.setBarrio(dto.getBarrio() != null ? dto.getBarrio().trim() : null);
        direccion.setManzanaPiso(dto.getManzanaPiso() != null ? dto.getManzanaPiso().trim() : null);
        direccion.setCasaDepartamento(dto.getCasaDepartamento() != null ? dto.getCasaDepartamento().trim() : null);
        direccion.setReferencia(dto.getReferencia() != null ? dto.getReferencia().trim() : null);
        direccion.setLocalidad(localidad);
        direccion.setEliminado(false);

        if (direccionRepository != null) {
            direccion = direccionRepository.save(direccion);
        }

        // 5. Validar y Crear Contacto (Correo o Celular)
        Contacto contacto;
        String tipoContacto = dto.getTipoContacto() != null ? dto.getTipoContacto().trim().toUpperCase() : "EMAIL";

        if ("CELULAR".equals(tipoContacto)) {
            if (dto.getContactoTelefono() == null || dto.getContactoTelefono().trim().isEmpty()) {
                throw new IllegalArgumentException("Debe ingresar un número de celular de contacto");
            }
            ContactoTelefonico tel = new ContactoTelefonico();
            tel.setTelefono(dto.getContactoTelefono().trim());
            tel.setTipoTelefono(TipoTelefono.CELULAR);
            tel.setTipoContacto(TipoContacto.PERSONAL);
            tel.setObservacion(dto.getContactoObservacion() != null ? dto.getContactoObservacion().trim() : "Contacto móvil registrado");
            tel.setEliminado(false);
            contacto = tel;
        } else {
            String mailContacto = (dto.getContactoEmail() != null && !dto.getContactoEmail().trim().isEmpty())
                    ? dto.getContactoEmail().trim()
                    : emailLimpio;
            ContactoCorreoElectronico mail = new ContactoCorreoElectronico();
            mail.setEmail(mailContacto);
            mail.setTipoContacto(TipoContacto.PERSONAL);
            mail.setObservacion(dto.getContactoObservacion() != null ? dto.getContactoObservacion().trim() : "Correo principal registrado");
            mail.setEliminado(false);
            contacto = mail;
        }

        if (contactoRepository != null) {
            contacto = contactoRepository.save(contacto);
        }

        // 6. Gestionar Foto de Perfil (Opcional, con monigote por defecto)
        List<Imagen> imagenes = new ArrayList<>();
        if (imagenService != null) {
            Imagen img;
            if (fotoPerfil != null && !fotoPerfil.isEmpty()) {
                img = imagenService.guardarImagen(fotoPerfil, TipoImagen.PERSONA);
            } else {
                img = imagenService.guardarMonigoteDefault();
            }
            if (img != null) {
                imagenes.add(img);
            }
        }

        // 7. Crear y Persistir Cliente
        List<Direccion> direcciones = new ArrayList<>();
        direcciones.add(direccion);

        List<Contacto> contactos = new ArrayList<>();
        contactos.add(contacto);

        Cliente cliente = Cliente.builder()
                .numeroDocumento(docLimpio)
                .nombre(dto.getNombre().trim())
                .apellido(dto.getApellido().trim())
                .fechaNacimiento(dto.getFechaNacimiento())
                .tipoDocumento(dto.getTipoDocumento() != null ? dto.getTipoDocumento() : TipoDocumento.DNI)
                .nacionalidad(nacionalidad)
                .direccion(direcciones)
                .contactos(contactos)
                .imagen(imagenes)
                .eliminado(false)
                .build();

        cliente = clienteRepository.save(cliente);

        // 8. Crear Usuario con Rol CLIENTE (inactivo hasta verificar correo)
        Usuario usuario = null;
        if (usuarioService != null) {
            usuario = usuarioService.crearUsuario(emailLimpio, dto.getPassword(), RolUsuario.CLIENTE, cliente, false);
            cliente.setUsuario(usuario);
            clienteRepository.save(cliente);

            try {
                String codigo = usuarioService.generarYAsignarCodigo(emailLimpio);
                usuarioService.enviarCodigoConfirmacion(emailLimpio, codigo);
            } catch (Exception e) {
                // Registrar advertencia para no abortar si el mock o smtp falla en entornos de prueba
                System.err.println(">> [ClienteService] Advertencia al despachar código de confirmación: " + e.getMessage());
            }
        }

        return usuario;
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
