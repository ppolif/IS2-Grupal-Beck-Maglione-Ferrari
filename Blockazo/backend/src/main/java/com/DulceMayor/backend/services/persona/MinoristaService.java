package com.DulceMayor.backend.services.persona;

import com.DulceMayor.backend.config.ConflictoException;
import com.DulceMayor.backend.config.RecursoNoEncontradoException;
import com.DulceMayor.backend.config.ValidacionException;
import com.DulceMayor.backend.dtos.AuthResponse;
import com.DulceMayor.backend.dtos.direccion.DireccionRequest;
import com.DulceMayor.backend.dtos.persona.ActualizarPerfilMinoristaRequest;
import com.DulceMayor.backend.dtos.persona.MinoristaEliminadoEvent;
import com.DulceMayor.backend.dtos.persona.MinoristaMapper;
import com.DulceMayor.backend.dtos.persona.MinoristaPage;
import com.DulceMayor.backend.dtos.persona.MinoristaRequest;
import com.DulceMayor.backend.dtos.persona.MinoristaResponse;
import com.DulceMayor.backend.dtos.persona.RegistroMinoristaRequest;
import com.DulceMayor.backend.entities.ProveedorAuth;
import com.DulceMayor.backend.entities.Usuario;
import com.DulceMayor.backend.entities.direccion.Direccion;
import com.DulceMayor.backend.entities.direccion.Localidad;
import com.DulceMayor.backend.entities.persona.Minorista;
import com.DulceMayor.backend.services.direccion.LocalidadService;
import com.DulceMayor.backend.repositories.persona.MinoristaRepository;
import com.DulceMayor.backend.services.BaseService;
import com.DulceMayor.backend.services.UsuarioService;
import com.DulceMayor.backend.services.direccion.DireccionService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final LocalidadService localidadService;
    private final ApplicationEventPublisher eventPublisher;

    public MinoristaService(
            MinoristaRepository minoristaRepository,
            MinoristaMapper minoristaMapper,
            PersonaService personaService,
            UsuarioService usuarioService,
            DireccionService direccionService,
            LocalidadService localidadService,
            ApplicationEventPublisher eventPublisher) {
        super(minoristaRepository, minoristaMapper, "Minorista");
        this.minoristaRepository = minoristaRepository;
        this.minoristaMapper = minoristaMapper;
        this.personaService = personaService;
        this.usuarioService = usuarioService;
        this.direccionService = direccionService;
        this.localidadService = localidadService;
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
                request.numeroDocumento() != null ? request.numeroDocumento().trim() : null,
                request.direcciones()
        );
    }

    @Override
    protected void validarCrear(MinoristaRequest request) {
        validarFormato(request);
        personaService.validarDocumentoUnico(request.numeroDocumento());
    }

    @Override
    protected void validarModificar(String id, MinoristaRequest request, Minorista existente) {
        validarFormato(request);
        personaService.validarDocumentoUnicoExcluyendoId(request.numeroDocumento(), id);
    }

    @Override
    protected void antesDePersistir(Minorista entidad, MinoristaRequest request) {
        if (request.direcciones() != null) {
            entidad.getDirecciones().clear();
            for (DireccionRequest dReq : request.direcciones()) {
                entidad.getDirecciones().add(direccionService.construirEntidad(dReq));
            }
        }
    }

    @Transactional
    public AuthResponse registrarEnPortal(RegistroMinoristaRequest request) {
        validarRegistro(request);

        String emailLimpio = request.email().trim().toLowerCase();
        if (usuarioService.existeNombreUsuario(emailLimpio)) {
            throw new ConflictoException("DUPLICADO", "El correo ya posee un usuario registrado");
        }

        personaService.validarDocumentoUnico(request.numeroDocumento().trim());

        Usuario usuario = usuarioService.crearUsuario(
                emailLimpio,
                request.clave(),
                false
        );

        List<Direccion> direcciones = new ArrayList<>();
        if ((request.calle() != null && !request.calle().isBlank()) ||
                (request.numero() != null && !request.numero().isBlank())) {
            Localidad localidad = null;
            if (request.localidadId() != null && !request.localidadId().isBlank()) {
                try {
                    localidad = localidadService.obtenerEntidad(request.localidadId().trim());
                } catch (Exception ignored) {
                    localidad = null;
                }
            }

            Direccion dir = Direccion.builder()
                    .calle(request.calle() != null ? request.calle().trim() : "")
                    .numero(request.numero() != null ? request.numero().trim() : "")
                    .localidad(localidad)
                    .eliminado(false)
                    .build();
            direcciones.add(dir);
        }

        Minorista nuevo = Minorista.builder()
                .nombre(request.nombre().trim())
                .apellido(request.apellido().trim())
                .fechaNacimiento(request.fechaNacimiento())
                .numeroDocumento(request.numeroDocumento().trim())
                .usuario(usuario)
                .direcciones(direcciones)
                .eliminado(false)
                .build();

        minoristaRepository.save(nuevo);
        return usuarioService.generarAuthResponse(usuario);
    }

    @Transactional
    public AuthResponse autenticarORegistrarSocial(String email, String nombre, String apellido) {
        String emailLimpio = email.trim().toLowerCase();
        Optional<Usuario> usuarioOpt = usuarioService.buscarPorNombreUsuario(emailLimpio);

        Usuario usuario;
        if (usuarioOpt.isPresent()) {
            usuario = usuarioOpt.get();
        } else {
            usuario = usuarioService.crearUsuarioSocial(emailLimpio, ProveedorAuth.GOOGLE);

            String nom = (nombre != null && !nombre.isBlank()) ? nombre.trim() : "Usuario";
            String ape = (apellido != null && !apellido.isBlank()) ? apellido.trim() : "Social";

            Minorista nuevo = Minorista.builder()
                    .nombre(nom)
                    .apellido(ape)
                    .numeroDocumento("DOC-" + System.currentTimeMillis())
                    .usuario(usuario)
                    .eliminado(false)
                    .build();
            minoristaRepository.save(nuevo);
        }

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

        personaService.validarDocumentoUnicoExcluyendoId(request.numeroDocumento().trim(), minorista.getId());

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
        minorista.setNumeroDocumento(request.numeroDocumento().trim());

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
