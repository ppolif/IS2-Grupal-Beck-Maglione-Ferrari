package com.DulceMayor.backend.services.persona;

import com.DulceMayor.backend.config.ConflictoException;
import com.DulceMayor.backend.config.RecursoNoEncontradoException;
import com.DulceMayor.backend.config.ValidacionException;
import com.DulceMayor.backend.dtos.AuthResponse;
import com.DulceMayor.backend.dtos.ContactoRequest;
import com.DulceMayor.backend.dtos.direccion.DireccionRequest;
import com.DulceMayor.backend.dtos.persona.MinoristaMapper;
import com.DulceMayor.backend.dtos.persona.MinoristaPage;
import com.DulceMayor.backend.dtos.persona.MinoristaRequest;
import com.DulceMayor.backend.dtos.persona.MinoristaResponse;
import com.DulceMayor.backend.dtos.persona.RegistroMinoristaRequest;
import com.DulceMayor.backend.entities.RolUsuario;
import com.DulceMayor.backend.entities.Usuario;
import com.DulceMayor.backend.entities.direccion.Direccion;
import com.DulceMayor.backend.entities.contacto.Contacto;
import com.DulceMayor.backend.entities.contacto.ContactoCorreoElectronico;
import com.DulceMayor.backend.entities.contacto.ContactoTelefonico;
import com.DulceMayor.backend.entities.contacto.TipoContacto;
import com.DulceMayor.backend.entities.contacto.TipoTelefono;
import com.DulceMayor.backend.entities.persona.Minorista;
import com.DulceMayor.backend.dtos.persona.ActualizarPerfilMinoristaRequest;
import com.DulceMayor.backend.dtos.persona.MinoristaEliminadoEvent;
import org.springframework.context.ApplicationEventPublisher;
import com.DulceMayor.backend.repositories.persona.MinoristaRepository;
import com.DulceMayor.backend.services.BaseService;
import com.DulceMayor.backend.services.ContactoService;
import com.DulceMayor.backend.services.UsuarioService;
import com.DulceMayor.backend.services.direccion.DireccionService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de negocio para la administración de clientes minoristas y su proceso de auto-registro.
 */
@Service
public class MinoristaService extends BaseService<Minorista, MinoristaRequest, MinoristaResponse> {

    private final MinoristaRepository minoristaRepository;
    private final MinoristaMapper minoristaMapper;
    private final PersonaService personaService;
    private final UsuarioService usuarioService;
    private final DireccionService direccionService;
    private final ContactoService contactoService;
    private final ApplicationEventPublisher eventPublisher;

    public MinoristaService(
            MinoristaRepository minoristaRepository,
            MinoristaMapper minoristaMapper,
            PersonaService personaService,
            UsuarioService usuarioService,
            DireccionService direccionService,
            ContactoService contactoService,
            ApplicationEventPublisher eventPublisher) {
        super(minoristaRepository, minoristaMapper, "Minorista");
        this.minoristaRepository = minoristaRepository;
        this.minoristaMapper = minoristaMapper;
        this.personaService = personaService;
        this.usuarioService = usuarioService;
        this.direccionService = direccionService;
        this.contactoService = contactoService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    protected MinoristaRequest prepararRequest(MinoristaRequest request) {
        if (request == null) {
            return null;
        }
        return new MinoristaRequest(
                request.nombre() != null ? request.nombre().trim() : null,
                request.apellido() != null ? request.apellido().trim() : null,
                request.fechaNacimiento(),
                request.tipoDocumento(),
                request.numeroDocumento() != null ? request.numeroDocumento().trim() : null,
                request.direcciones(),
                request.contactos()
        );
    }

    @Override
    protected void validarCrear(MinoristaRequest request) {
        validarFormato(request);
        personaService.validarDocumentoUnico(request.tipoDocumento(), request.numeroDocumento());
    }

    @Override
    protected void validarModificar(String id, MinoristaRequest request, Minorista existente) {
        validarFormato(request);
        personaService.validarDocumentoUnicoExcluyendoId(request.tipoDocumento(), request.numeroDocumento(), id);
    }

    @Override
    protected void antesDePersistir(Minorista entidad, MinoristaRequest request) {
        // Si lo da de alta un preventor en terreno y no tiene usuario aún, se crea con claveTemporal = true
        if (entidad.getUsuario() == null && esPreventistaAutenticado()) {
            String username = resolverNombreUsuario(request);
            if (!usuarioService.existeNombreUsuario(username)) {
                Usuario usuario = usuarioService.crearUsuario(
                        username,
                        UsuarioService.CLAVE_POR_DEFECTO,
                        RolUsuario.MINORISTA,
                        true
                );
                entidad.setUsuario(usuario);
            }
        }

        if (request.direcciones() != null) {
            entidad.getDirecciones().clear();
            for (DireccionRequest dReq : request.direcciones()) {
                entidad.getDirecciones().add(direccionService.construirEntidad(dReq));
            }
        }
        if (request.contactos() != null) {
            entidad.getContactos().clear();
            for (ContactoRequest cReq : request.contactos()) {
                entidad.getContactos().add(contactoService.construirEntidad(cReq));
            }
        }
    }

    ///Este metodo es para que el preventor registre minoristas
    @Transactional
    public AuthResponse registrarEnPortal(RegistroMinoristaRequest request) {
        validarRegistro(request);

        String emailLimpio = request.email().trim().toLowerCase();
        if (usuarioService.existeNombreUsuario(emailLimpio)) {
            throw new ConflictoException("DUPLICADO", "El correo ya posee un usuario registrado");
        }

        // Buscar si ya existía el minorista cargado previamente
        Optional<Minorista> minoristaOpt = minoristaRepository.findActivoByEmail(emailLimpio);
        if (minoristaOpt.isEmpty()) {
            minoristaOpt = minoristaRepository.findByTipoDocumentoAndNumeroDocumentoAndEliminadoFalse(
                    request.tipoDocumento(), request.numeroDocumento().trim());
        }

        if (minoristaOpt.isPresent()) {
            Minorista existente = minoristaOpt.get();
            if (existente.getUsuario() != null) {
                // Caso A: Ya existía y ya tiene usuario asignado
                throw new ConflictoException(
                        "DUPLICADO",
                        "El cliente ya posee una cuenta de usuario asignada. Inicie sesión directamente."
                );
            }
            // Caso B: Minorista precargado con usuario == null. Se le asigna claveTemporal = true
            Usuario usuario = usuarioService.crearUsuario(
                    emailLimpio,
                    request.clave(),
                    RolUsuario.MINORISTA,
                    true
            );
            existente.setUsuario(usuario);
            if (request.fechaNacimiento() != null && existente.getFechaNacimiento() == null) {
                existente.setFechaNacimiento(request.fechaNacimiento());
            }
            if (request.telefono() != null && !request.telefono().isBlank()) {
                boolean tieneTelefono = existente.getContactos().stream()
                        .anyMatch(c -> c instanceof ContactoTelefonico && !c.isEliminado());
                if (!tieneTelefono) {
                    existente.getContactos().add(ContactoTelefonico.builder()
                            .tipoContacto(TipoContacto.PERSONAL)
                            .telefono(request.telefono().trim())
                            .tipoTelefono(TipoTelefono.CELULAR)
                            .eliminado(false)
                            .build());
                }
            }
            if ((request.calle() != null && !request.calle().isBlank()) ||
                    (request.numero() != null && !request.numero().isBlank())) {
                String c = request.calle() != null ? request.calle().trim() : "";
                String n = request.numero() != null ? request.numero().trim() : "";
                Optional<Direccion> dirOpt = existente.getDirecciones().stream()
                        .filter(d -> !d.isEliminado())
                        .findFirst();
                if (dirOpt.isPresent()) {
                    dirOpt.get().setCalle(c);
                    dirOpt.get().setNumero(n);
                } else {
                    existente.getDirecciones().add(Direccion.builder()
                            .calle(c)
                            .numero(n)
                            .eliminado(false)
                            .build());
                }
            }
            minoristaRepository.save(existente);
            return usuarioService.generarAuthResponse(usuario);
        }

        // Caso C: Auto-registro de un nuevo minorista desde cero
        personaService.validarDocumentoUnico(request.tipoDocumento(), request.numeroDocumento().trim());

        Usuario usuario = usuarioService.crearUsuario(
                emailLimpio,
                request.clave(),
                RolUsuario.MINORISTA,
                false
        );

        List<Contacto> contactos = new ArrayList<>();
        ContactoCorreoElectronico contactoCorreo = ContactoCorreoElectronico.builder()
                .tipoContacto(TipoContacto.PERSONAL)
                .email(emailLimpio)
                .eliminado(false)
                .build();
        contactos.add(contactoCorreo);

        if (request.telefono() != null && !request.telefono().isBlank()) {
            ContactoTelefonico contactoTel = ContactoTelefonico.builder()
                    .tipoContacto(TipoContacto.PERSONAL)
                    .telefono(request.telefono().trim())
                    .tipoTelefono(TipoTelefono.CELULAR)
                    .eliminado(false)
                    .build();
            contactos.add(contactoTel);
        }

        List<Direccion> direcciones = new ArrayList<>();
        if ((request.calle() != null && !request.calle().isBlank()) ||
                (request.numero() != null && !request.numero().isBlank())) {
            Direccion dir = Direccion.builder()
                    .calle(request.calle() != null ? request.calle().trim() : "")
                    .numero(request.numero() != null ? request.numero().trim() : "")
                    .eliminado(false)
                    .build();
            direcciones.add(dir);
        }

        Minorista nuevo = Minorista.builder()
                .nombre(request.nombre().trim())
                .apellido(request.apellido().trim())
                .fechaNacimiento(request.fechaNacimiento())
                .tipoDocumento(request.tipoDocumento())
                .numeroDocumento(request.numeroDocumento().trim())
                .usuario(usuario)
                .contactos(contactos)
                .direcciones(direcciones)
                .eliminado(false)
                .build();

        minoristaRepository.save(nuevo);
        return usuarioService.generarAuthResponse(usuario);
    }

    @Transactional(readOnly = true)
    public MinoristaPage buscarPaginado(String numeroDocumento, String q, Pageable pageable) {
        String docParam = (numeroDocumento != null && !numeroDocumento.isBlank()) ? numeroDocumento.trim() : null;
        String qParam = (q != null && !q.isBlank()) ? q.trim() : null;

        Page<Minorista> page = minoristaRepository.buscarConFiltros(docParam, qParam, pageable);
        List<MinoristaResponse> content = mapper.aResponseList(page.getContent());
        return new MinoristaPage(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    @Transactional(readOnly = true)
    public Optional<Minorista> buscarPorUsuarioId(String usuarioId) {
        return minoristaRepository.findByUsuarioIdAndEliminadoFalse(usuarioId);
    }

    @Transactional(readOnly = true)
    public Minorista obtenerPorUsuarioId(String usuarioId) {
        return minoristaRepository.findByUsuarioIdAndEliminadoFalse(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró minorista para el usuario con id: " + usuarioId));
    }

    @Transactional(readOnly = true)
    public MinoristaResponse obtenerMiPerfil(String usuarioId) {
        Minorista minorista = obtenerPorUsuarioId(usuarioId);
        return mapper.aResponse(minorista);
    }

    @Transactional
    public MinoristaResponse actualizarMiPerfil(String usuarioId, ActualizarPerfilMinoristaRequest request) {
        if (request == null) {
            throw new ValidacionException("Los datos de perfil son obligatorios");
        }
        Minorista minorista = obtenerPorUsuarioId(usuarioId);

        personaService.validarDocumentoUnicoExcluyendoId(request.tipoDocumento(), request.numeroDocumento().trim(), minorista.getId());

        String nuevoEmail = request.email().trim().toLowerCase();
        Usuario usuario = minorista.getUsuario();
        if (usuario != null) {
            if (!nuevoEmail.equalsIgnoreCase(usuario.getNombreUsuario())) {
                if (usuarioService.existeNombreUsuarioExcluyendoId(nuevoEmail, usuario.getId())) {
                    throw new ConflictoException("DUPLICADO", "El correo ya posee un usuario registrado");
                }
                usuarioService.actualizarNombreUsuario(usuario, nuevoEmail);
            }
        }

        minorista.setNombre(request.nombre().trim());
        minorista.setApellido(request.apellido().trim());
        minorista.setFechaNacimiento(request.fechaNacimiento());
        minorista.setTipoDocumento(request.tipoDocumento());
        minorista.setNumeroDocumento(request.numeroDocumento().trim());

        // Actualizar o agregar ContactoCorreoElectronico
        Optional<ContactoCorreoElectronico> correoOpt = minorista.getContactos().stream()
                .filter(c -> c instanceof ContactoCorreoElectronico && !c.isEliminado())
                .map(c -> (ContactoCorreoElectronico) c)
                .findFirst();
        if (correoOpt.isPresent()) {
            correoOpt.get().setEmail(nuevoEmail);
        } else {
            minorista.getContactos().add(ContactoCorreoElectronico.builder()
                    .tipoContacto(TipoContacto.PERSONAL)
                    .email(nuevoEmail)
                    .eliminado(false)
                    .build());
        }

        // Actualizar o agregar ContactoTelefonico
        Optional<ContactoTelefonico> telOpt = minorista.getContactos().stream()
                .filter(c -> c instanceof ContactoTelefonico && !c.isEliminado())
                .map(c -> (ContactoTelefonico) c)
                .findFirst();
        if (request.telefono() != null && !request.telefono().isBlank()) {
            String telLimpio = request.telefono().trim();
            if (telOpt.isPresent()) {
                telOpt.get().setTelefono(telLimpio);
            } else {
                minorista.getContactos().add(ContactoTelefonico.builder()
                        .tipoContacto(TipoContacto.PERSONAL)
                        .telefono(telLimpio)
                        .tipoTelefono(TipoTelefono.CELULAR)
                        .eliminado(false)
                        .build());
            }
        } else {
            telOpt.ifPresent(c -> c.setEliminado(true));
        }

        // Actualizar o agregar Dirección (calle y número)
        if (request.calle() != null || request.numero() != null) {
            String c = request.calle() != null ? request.calle().trim() : "";
            String n = request.numero() != null ? request.numero().trim() : "";

            Optional<Direccion> dirOpt = minorista.getDirecciones().stream()
                    .filter(d -> !d.isEliminado())
                    .findFirst();

            if (!c.isBlank() || !n.isBlank()) {
                if (dirOpt.isPresent()) {
                    dirOpt.get().setCalle(c);
                    dirOpt.get().setNumero(n);
                } else {
                    minorista.getDirecciones().add(Direccion.builder()
                            .calle(c)
                            .numero(n)
                            .eliminado(false)
                            .build());
                }
            } else {
                dirOpt.ifPresent(d -> d.setEliminado(true));
            }
        }

        Minorista guardado = minoristaRepository.save(minorista);
        return mapper.aResponse(guardado);
    }

    @Transactional
    public void eliminarMiCuenta(String usuarioId) {
        Minorista minorista = obtenerPorUsuarioId(usuarioId);
        eliminar(minorista.getId());
    }

    @Override
    protected void antesDeEliminar(Minorista existente) {
        if (existente.getUsuario() != null) {
            usuarioService.eliminarUsuario(existente.getUsuario().getId());
        }
        if (existente.getContactos() != null) {
            existente.getContactos().forEach(c -> c.setEliminado(true));
        }
        if (existente.getDirecciones() != null) {
            existente.getDirecciones().forEach(d -> d.setEliminado(true));
        }
    }

    @Override
    protected void despuesDeEliminar(Minorista existente) {
        if (eventPublisher != null) {
            eventPublisher.publishEvent(new MinoristaEliminadoEvent(existente.getId()));
        }
    }

    private boolean esPreventistaAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities() == null) {
            return false;
        }
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_PREVENTISTA") || a.getAuthority().equals("PREVENTISTA"));
    }

    private String resolverNombreUsuario(MinoristaRequest request) {
        if (request.contactos() != null) {
            for (ContactoRequest c : request.contactos()) {
                if ("CORREO".equalsIgnoreCase(c.medio()) && c.email() != null && !c.email().isBlank()) {
                    return c.email().trim().toLowerCase();
                }
            }
        }
        return request.numeroDocumento().trim();
    }

    private void validarFormato(MinoristaRequest request) {
        if (request == null) {
            throw new ValidacionException("Los datos del minorista son obligatorios");
        }
        if (request.nombre() == null || request.nombre().isBlank()) {
            throw new ValidacionException("El nombre es obligatorio");
        }
        if (request.apellido() == null || request.apellido().isBlank()) {
            throw new ValidacionException("El apellido es obligatorio");
        }
        if (request.tipoDocumento() == null) {
            throw new ValidacionException("El tipo de documento es obligatorio");
        }
        if (request.numeroDocumento() == null || request.numeroDocumento().isBlank()) {
            throw new ValidacionException("El número de documento es obligatorio");
        }
    }

    private void validarRegistro(RegistroMinoristaRequest request) {
        if (request == null) {
            throw new ValidacionException("Los datos de registro son obligatorios");
        }
        if (request.nombre() == null || request.nombre().isBlank()) {
            throw new ValidacionException("El nombre es obligatorio");
        }
        if (request.apellido() == null || request.apellido().isBlank()) {
            throw new ValidacionException("El apellido es obligatorio");
        }
        if (request.tipoDocumento() == null) {
            throw new ValidacionException("El tipo de documento es obligatorio");
        }
        if (request.numeroDocumento() == null || request.numeroDocumento().isBlank()) {
            throw new ValidacionException("El número de documento es obligatorio");
        }
        if (request.email() == null || request.email().isBlank()) {
            throw new ValidacionException("El correo es obligatorio");
        }
        if (request.clave() == null || request.clave().length() < 8) {
            throw new ValidacionException("La clave debe tener al menos 8 caracteres");
        }
        if (UsuarioService.CLAVE_POR_DEFECTO.equals(request.clave())) {
            throw new ValidacionException("La clave no puede ser la clave por defecto");
        }
    }
}

