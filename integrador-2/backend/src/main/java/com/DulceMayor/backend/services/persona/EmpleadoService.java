package com.DulceMayor.backend.services.persona;

import com.DulceMayor.backend.config.ConflictoException;
import com.DulceMayor.backend.config.RecursoNoEncontradoException;
import com.DulceMayor.backend.config.ValidacionException;
import com.DulceMayor.backend.dtos.ContactoRequest;
import com.DulceMayor.backend.dtos.direccion.DireccionRequest;
import com.DulceMayor.backend.dtos.persona.EmpleadoMapper;
import com.DulceMayor.backend.dtos.persona.EmpleadoPage;
import com.DulceMayor.backend.dtos.persona.EmpleadoRequest;
import com.DulceMayor.backend.dtos.persona.EmpleadoResponse;
import com.DulceMayor.backend.dtos.persona.RolEmpleado;
import com.DulceMayor.backend.entities.RolUsuario;
import com.DulceMayor.backend.entities.Usuario;
import com.DulceMayor.backend.entities.persona.Distribuidor;
import com.DulceMayor.backend.entities.persona.Empleado;
import com.DulceMayor.backend.entities.persona.Preventor;
import com.DulceMayor.backend.repositories.persona.EmpleadoRepository;
import com.DulceMayor.backend.services.BaseService;
import com.DulceMayor.backend.services.ContactoService;
import com.DulceMayor.backend.services.UsuarioService;
import com.DulceMayor.backend.services.direccion.DireccionService;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de negocio para la administración de empleados (Jefes, Administrativos,
 * Preventores y Distribuidores).
 */
@Service
public class EmpleadoService extends BaseService<Empleado, EmpleadoRequest, EmpleadoResponse> {

    private final EmpleadoRepository empleadoRepository;
    private final EmpleadoMapper empleadoMapper;
    private final PersonaService personaService;
    private final UsuarioService usuarioService;
    private final DireccionService direccionService;
    private final ContactoService contactoService;

    public EmpleadoService(
            EmpleadoRepository empleadoRepository,
            EmpleadoMapper empleadoMapper,
            PersonaService personaService,
            UsuarioService usuarioService,
            DireccionService direccionService,
            ContactoService contactoService) {
        super(empleadoRepository, empleadoMapper, "Empleado");
        this.empleadoRepository = empleadoRepository;
        this.empleadoMapper = empleadoMapper;
        this.personaService = personaService;
        this.usuarioService = usuarioService;
        this.direccionService = direccionService;
        this.contactoService = contactoService;
    }

    @Override
    protected EmpleadoRequest prepararRequest(EmpleadoRequest request) {
        if (request == null) {
            return null;
        }
        return new EmpleadoRequest(
                request.nombre() != null ? request.nombre().trim() : null,
                request.apellido() != null ? request.apellido().trim() : null,
                request.fechaNacimiento(),
                request.tipoDocumento(),
                request.numeroDocumento() != null ? request.numeroDocumento().trim() : null,
                request.direcciones(),
                request.contactos(),
                request.legajo() != null ? request.legajo().trim() : null,
                request.nombreUsuario() != null ? request.nombreUsuario().trim().toLowerCase() : null,
                request.rol()
        );
    }

    @Override
    protected void validarCrear(EmpleadoRequest request) {
        validarFormato(request);
        personaService.validarDocumentoUnico(request.tipoDocumento(), request.numeroDocumento());
        if (empleadoRepository.existsByLegajoAndEliminadoFalse(request.legajo())) {
            throw new ConflictoException("DUPLICADO", "Ya existe un empleado con el legajo " + request.legajo());
        }
        if (usuarioService.existeNombreUsuario(request.nombreUsuario())) {
            throw new ConflictoException("DUPLICADO", "El nombre de usuario ya se encuentra registrado");
        }
    }

    @Override
    protected void validarModificar(String id, EmpleadoRequest request, Empleado existente) {
        validarFormato(request);
        personaService.validarDocumentoUnicoExcluyendoId(request.tipoDocumento(), request.numeroDocumento(), id);
        if (empleadoRepository.existsByLegajoAndIdNotAndEliminadoFalse(request.legajo(), id)) {
            throw new ConflictoException("DUPLICADO", "Ya existe otro empleado con el legajo " + request.legajo());
        }
        String usuarioId = existente.getUsuario() != null ? existente.getUsuario().getId() : null;
        if (usuarioId != null && usuarioService.existeNombreUsuarioExcluyendoId(request.nombreUsuario(), usuarioId)) {
            throw new ConflictoException("DUPLICADO", "El nombre de usuario ya se encuentra registrado por otro usuario");
        }

        // Validar coherencia de subclase física
        RolEmpleado rolNuevo = request.rol();
        if (existente instanceof Preventor && rolNuevo != RolEmpleado.PREVENTISTA) {
            throw new ConflictoException("TRANSICION_INVALIDA", "No es posible cambiar la especialidad de un Preventor");
        }
        if (existente instanceof Distribuidor && rolNuevo != RolEmpleado.DISTRIBUIDOR) {
            throw new ConflictoException("TRANSICION_INVALIDA", "No es posible cambiar la especialidad de un Distribuidor");
        }
        if (!(existente instanceof Preventor) && !(existente instanceof Distribuidor)
                && (rolNuevo == RolEmpleado.PREVENTISTA || rolNuevo == RolEmpleado.DISTRIBUIDOR)) {
            throw new ConflictoException("TRANSICION_INVALIDA", "No es posible reasignar la clase base de empleado a una especialidad");
        }
    }

    /// Como tenemos jerarquia polimorfica (JOINED), sobreescribimos aEntidad
    /// para resolver la subclase antes de persistir.
    @Override
    protected Empleado aEntidad(EmpleadoRequest request) {
        Empleado entidad;
        if (request.rol() == RolEmpleado.PREVENTISTA) {
            entidad = new Preventor();
        } else if (request.rol() == RolEmpleado.DISTRIBUIDOR) {
            entidad = new Distribuidor();
        } else {
            entidad = new Empleado();
        }
        entidad.setNombre(request.nombre());
        entidad.setApellido(request.apellido());
        entidad.setFechaNacimiento(request.fechaNacimiento());
        entidad.setTipoDocumento(request.tipoDocumento());
        entidad.setNumeroDocumento(request.numeroDocumento());
        entidad.setLegajo(request.legajo());
        return entidad;
    }

    @Override
    protected void antesDePersistir(Empleado entidad, EmpleadoRequest request) {
        if (entidad.getUsuario() == null) {
            Usuario usuario = usuarioService.crearUsuarioConClavePorDefecto(
                    request.nombreUsuario(), toRolUsuario(request.rol()));
            entidad.setUsuario(usuario);
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

    @Override
    protected void despuesDeModificar(Empleado entidad, EmpleadoRequest request) {
        if (entidad.getUsuario() != null) {
            usuarioService.actualizarUsuarioEmpleado(
                    entidad.getUsuario(), request.nombreUsuario(), toRolUsuario(request.rol()));
        }
    }

    @Override
    protected void antesDeEliminar(Empleado existente) {
        if (existente.getUsuario() != null) {
            usuarioService.eliminarUsuario(existente.getUsuario().getId());
        }
    }

    @Transactional(readOnly = true)
    public EmpleadoPage buscarPaginado(String rolStr, String legajo, String q, Pageable pageable) {
        RolUsuario rol = null;
        if (rolStr != null && !rolStr.isBlank()) {
            try {
                rol = RolUsuario.valueOf(rolStr.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new ValidacionException("Rol no válido: " + rolStr);
            }
        }
        String legajoParam = (legajo != null && !legajo.isBlank()) ? legajo.trim() : null;
        String qParam = (q != null && !q.isBlank()) ? q.trim() : null;

        Page<Empleado> page = empleadoRepository.buscarConFiltros(rol, legajoParam, qParam, pageable);
        List<EmpleadoResponse> content = mapper.aResponseList(page.getContent());
        return new EmpleadoPage(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    @Transactional(readOnly = true)
    public Preventor obtenerPreventor(String id) {
        Empleado emp = obtenerEntidad(id);
        if (!(emp instanceof Preventor preventor)) {
            throw new ValidacionException("El empleado con id " + id + " no es un Preventor");
        }
        return preventor;
    }

    @Transactional(readOnly = true)
    public Distribuidor obtenerDistribuidor(String id) {
        Empleado emp = obtenerEntidad(id);
        if (!(emp instanceof Distribuidor distribuidor)) {
            throw new ValidacionException("El empleado con id " + id + " no es un Distribuidor");
        }
        return distribuidor;
    }

    @Transactional(readOnly = true)
    public Preventor obtenerPreventorPorUsuarioId(String usuarioId) {
        Empleado emp = empleadoRepository.findByUsuarioIdAndEliminadoFalse(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró empleado preventor para el usuario autenticado"));
        if (!(emp instanceof Preventor preventor)) {
            throw new ValidacionException("El usuario autenticado no posee perfil de Preventor");
        }
        return preventor;
    }

    @Transactional(readOnly = true)
    public Distribuidor obtenerDistribuidorPorUsuarioId(String usuarioId) {
        Empleado emp = empleadoRepository.findByUsuarioIdAndEliminadoFalse(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró empleado distribuidor para el usuario autenticado"));
        if (!(emp instanceof Distribuidor distribuidor)) {
            throw new ValidacionException("El usuario autenticado no posee perfil de Distribuidor");
        }
        return distribuidor;
    }



    private RolUsuario toRolUsuario(RolEmpleado rol) {
        return RolUsuario.valueOf(rol.name());
    }

    private void validarFormato(EmpleadoRequest request) {
        if (request == null) {
            throw new ValidacionException("Los datos del empleado son obligatorios");
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
        if (request.legajo() == null || request.legajo().isBlank()) {
            throw new ValidacionException("El legajo es obligatorio");
        }
        if (request.nombreUsuario() == null || request.nombreUsuario().isBlank()) {
            throw new ValidacionException("El nombre de usuario es obligatorio");
        }
        if (request.rol() == null) {
            throw new ValidacionException("El rol es obligatorio");
        }
    }
}

