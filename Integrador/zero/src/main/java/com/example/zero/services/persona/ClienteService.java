package com.example.zero.services.persona;

import com.example.zero.dto.persona.ClienteRegistroDTO;
import com.example.zero.entidades.Imagen;
import com.example.zero.entidades.empresa.Contacto;
import com.example.zero.entidades.persona.Cliente;
import com.example.zero.entidades.persona.Nacionalidad;
import com.example.zero.entidades.persona.Usuario;
import com.example.zero.entidades.zona.Direccion;
import com.example.zero.enums.RolUsuario;
import com.example.zero.enums.TipoContacto;
import com.example.zero.enums.TipoDocumento;
import com.example.zero.enums.TipoImagen;
import com.example.zero.enums.TipoTelefono;
import com.example.zero.repositories.ClienteRepository;
import com.example.zero.services.ContactoService;
import com.example.zero.services.ImagenService;
import com.example.zero.services.zona.ZonaService;
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
    private final NacionalidadService nacionalidadService;
    private final UsuarioService usuarioService;
    private final ZonaService zonaService;
    private final ContactoService contactoService;
    private final ImagenService imagenService;

    public ClienteService(ClienteRepository clienteRepository,
                          NacionalidadService nacionalidadService,
                          UsuarioService usuarioService,
                          ZonaService zonaService,
                          ContactoService contactoService,
                          ImagenService imagenService) {
        this.clienteRepository = clienteRepository;
        this.nacionalidadService = nacionalidadService;
        this.usuarioService = usuarioService;
        this.zonaService = zonaService;
        this.contactoService = contactoService;
        this.imagenService = imagenService;
    }

    public void validar(String numeroDocumento, String nombre, String apellido, TipoDocumento tipoDocumento) {
        if (numeroDocumento == null || numeroDocumento.trim().isEmpty()) {
            throw new IllegalArgumentException("El número de documento no puede estar vacío");
        }
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío");
        }
        if (apellido == null || apellido.trim().isEmpty()) {
            throw new IllegalArgumentException("El apellido no puede estar vacío");
        }
        if (tipoDocumento == null) {
            throw new IllegalArgumentException("El tipo de documento no puede estar vacío");
        }
    }

    ////este solo se llama cuando un admin registra una venta
    @Transactional
    public Cliente crearCliente(String numeroDocumento, String nombre, String apellido,
                                LocalDate fechaNacimiento, TipoDocumento tipoDocumento,
                                Nacionalidad nacionalidad) {

        validar(numeroDocumento, nombre, apellido, tipoDocumento);
        String docLimpio = numeroDocumento.trim();

        Optional<Cliente> existente = clienteRepository.findByNumeroDocumentoAndEliminadoFalse(docLimpio);
        if (existente.isPresent()) {
            throw new IllegalArgumentException("Ya existe un cliente activo con el documento: " + docLimpio);
        }

        LocalDate fecha = (fechaNacimiento != null) ? fechaNacimiento : LocalDate.of(2000, 1, 1);
        Cliente cliente = Cliente.builder()
                .numeroDocumento(docLimpio)
                .nombre(nombre.trim())
                .apellido(apellido.trim())
                .fechaNacimiento(fecha)
                .tipoDocumento(tipoDocumento)
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



        // validar credenciales
        usuarioService.validar(dto.getEmail(), dto.getPassword(), RolUsuario.CLIENTE);
        if (dto.getConfirmPassword() == null || !dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("Las contraseñas no coinciden");
        }

        String emailLimpio = dto.getEmail().trim().toLowerCase();
        if (usuarioService.existePorNombreUsuario(emailLimpio)) {
            throw new IllegalArgumentException("Ya existe un usuario activo con el correo: " + emailLimpio);
        }

        // validar datos personales
        validar(dto.getNumeroDocumento(), dto.getNombre(), dto.getApellido(), dto.getTipoDocumento());
        if (dto.getFechaNacimiento() == null) {
            throw new IllegalArgumentException("Debe ingresar su fecha de nacimiento");
        }
        if (dto.getFechaNacimiento().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de nacimiento no puede ser posterior a la actual");
        }

        // validar Nacionalidad
        if (dto.getNacionalidadId() == null || dto.getNacionalidadId().trim().isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar una nacionalidad");
        }
        Nacionalidad nacionalidad = nacionalidadService.buscarPorId(dto.getNacionalidadId().trim());

        // crear direccion
        Direccion direccion = zonaService.crearDireccion(
                dto.getCalle(),
                dto.getNumeracion(),
                dto.getBarrio(),
                dto.getManzanaPiso(),
                dto.getCasaDepartamento(),
                dto.getReferencia(),
                dto.getLocalidadId()
        );

        // crear Contacto
        String tipoContacto = dto.getTipoContacto();
        Contacto contacto;
        if ("CELULAR".equalsIgnoreCase(tipoContacto)) {
            contacto = contactoService.crearContactoTelefonico(
                    dto.getContactoTelefono(),
                    TipoTelefono.CELULAR,
                    TipoContacto.PERSONAL,
                    dto.getContactoObservacion()
            );
        } else {
            String mailContacto = (dto.getContactoEmail() != null && !dto.getContactoEmail().trim().isEmpty())
                    ? dto.getContactoEmail()
                    : emailLimpio;
            contacto = contactoService.crearContactoCorreo(
                    mailContacto,
                    TipoContacto.PERSONAL,
                    dto.getContactoObservacion()
            );
        }

        // foto de perfil
        List<Imagen> imagenes = new ArrayList<>();
        String fotoUrl = null;
        Imagen img = null;
        if (fotoPerfil != null && !fotoPerfil.isEmpty()) {
            img = imagenService.guardarImagen(fotoPerfil, TipoImagen.PERSONA);
        }
        if (img == null) {
            img = imagenService.guardarMonigoteDefault();
        }
        if (img != null) {
            imagenes.add(img);
            fotoUrl = "/imagen/" + img.getId();
        }

        // obtener o crear cliente
        String docLimpio = dto.getNumeroDocumento().trim();
        Optional<Cliente> clienteExistente = clienteRepository.findByNumeroDocumentoAndEliminadoFalse(docLimpio);

        Cliente cliente;
        if (clienteExistente.isPresent()) {
            cliente = clienteExistente.get();
        } else {
            List<Direccion> direcciones = new ArrayList<>();
            direcciones.add(direccion);

            List<Contacto> contactos = new ArrayList<>();
            contactos.add(contacto);

            cliente = Cliente.builder()
                    .numeroDocumento(docLimpio)
                    .nombre(dto.getNombre())
                    .apellido(dto.getApellido())
                    .fechaNacimiento(dto.getFechaNacimiento())
                    .tipoDocumento(dto.getTipoDocumento() != null ? dto.getTipoDocumento() : TipoDocumento.DNI)
                    .nacionalidad(nacionalidad)
                    .direccion(direcciones)
                    .contactos(contactos)
                    .imagen(imagenes)
                    .eliminado(false)
                    .build();

            cliente = clienteRepository.save(cliente);
        }

        // 8. Crear Usuario con Rol CLIENTE (inactivo hasta verificar correo)
        Usuario usuario = usuarioService.crearUsuario(emailLimpio, dto.getPassword(), RolUsuario.CLIENTE, cliente, false);
        if (fotoUrl != null) {
            usuario = usuarioService.actualizarFoto(usuario, fotoUrl);
        }

        try {
            String codigo = usuarioService.generarYAsignarCodigo(emailLimpio);
            usuarioService.enviarCodigoConfirmacion(emailLimpio, codigo);
        } catch (Exception e) {
            System.err.println(">> [ClienteService] Advertencia al despachar código de confirmación: " + e.getMessage());
        }

        return usuario;
    }

    @Transactional
    public Cliente modificarCliente(String numeroDocumento, String nombre, String apellido,
                                    LocalDate fechaNacimiento, TipoDocumento tipoDocumento,
                                    Nacionalidad nacionalidad) {
        Cliente cliente = buscarPorDocumento(numeroDocumento);

        validar(numeroDocumento, nombre, apellido, tipoDocumento);

        cliente.setNombre(nombre.trim());
        cliente.setApellido(apellido.trim());
        cliente.setTipoDocumento(tipoDocumento);

        if (fechaNacimiento != null) {
            cliente.setFechaNacimiento(fechaNacimiento);
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
    public Usuario asociarClienteUsuario(String numeroDocumento, Usuario usuario) {
        Cliente cliente = buscarPorDocumento(numeroDocumento);
        return usuarioService.asociarPersona(usuario, cliente);
    }

    public String obtenerFotoPerfilCliente(Cliente c) {
        if (c == null) {
            return "/admin/assets/images/avatar.png";
        }
        if (c.getNumeroDocumento() != null) {
            String fotoUsuario = usuarioService.obtenerFotoPerfilPorDocumento(c.getNumeroDocumento());
            if (fotoUsuario != null && !fotoUsuario.isBlank()) {
                return fotoUsuario;
            }
        }
        if (c.getImagen() != null && !c.getImagen().isEmpty()) {
            for (Imagen img : c.getImagen()) {
                if (img != null && !img.isEliminado() && img.getId() != null) {
                    return "/imagen/" + img.getId();
                }
            }
        }
        return "/admin/assets/images/avatar.png";
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
