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
import com.DulceMayor.backend.entities.mayorista.Mayorista;
import com.DulceMayor.backend.entities.persona.Distribuidor;
import com.DulceMayor.backend.entities.persona.Empleado;
import com.DulceMayor.backend.entities.persona.Preventor;
import com.DulceMayor.backend.repositories.persona.EmpleadoRepository;
import com.DulceMayor.backend.services.BaseService;
import com.DulceMayor.backend.services.ContactoService;
import com.DulceMayor.backend.services.UsuarioService;
import com.DulceMayor.backend.services.direccion.DireccionService;
import com.DulceMayor.backend.services.mayorista.MayoristaService;
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
    private final MayoristaService mayoristaService;

    public EmpleadoService(
            EmpleadoRepository empleadoRepository,
            EmpleadoMapper empleadoMapper,
            PersonaService personaService,
            UsuarioService usuarioService,
            DireccionService direccionService,
            ContactoService contactoService,
            MayoristaService mayoristaService) {
        super(empleadoRepository, empleadoMapper, "Empleado");
        this.empleadoRepository = empleadoRepository;
        this.empleadoMapper = empleadoMapper;
        this.personaService = personaService;
        this.usuarioService = usuarioService;
        this.direccionService = direccionService;
        this.contactoService = contactoService;
        this.mayoristaService = mayoristaService;
    }

    /**
     * Normaliza los campos del DTO de entrada (recorte de espacios y minúsculas en el usuario).
     *
     * <p><b>Cuándo se usa:</b> Automáticamente al inicio de las operaciones {@code crear} y {@code modificar}
     * del Template Method heredado de {@link BaseService}, antes de ejecutar las validaciones.</p>
     */
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
                request.rol(),
                request.mayoristaId() != null ? request.mayoristaId().trim() : null
        );
    }

    /**
     * Valida las reglas de negocio para el alta de un nuevo empleado:
     * campos obligatorios, documento único a nivel persona, legajo único y nombre de usuario único.
     *
     * <p><b>Cuándo se usa:</b> Durante la ejecución del método plantilla {@code crear}, luego de preparar
     * el request y antes de instanciar la entidad.</p>
     */
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

    /**
     * Valida las reglas de negocio al actualizar un empleado existente:
     * documento único (excluyendo el actual), legajo único (excluyendo el actual),
     * nombre de usuario único (excluyendo el suyo) y consistencia del rol (no permite mutar
     * de especialidad {@code PREVENTISTA} o {@code DISTRIBUIDOR} a otra subclase física JPA).
     *
     * <p><b>Cuándo se usa:</b> Durante la ejecución del método plantilla {@code modificar}, luego de obtener
     * la entidad existente y antes de actualizar sus campos.</p>
     */
    @Override
    protected void validarModificar(String id, EmpleadoRequest request, Empleado existente) {
        validarFormato(request);
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

    /**
     * Resuelve la subclase polimórfica concreta ({@link Preventor}, {@link Distribuidor} o {@link Empleado} base)
     * e inicializa los atributos personales a partir del request.
     *
     * <p><b>Cuándo se usa:</b> Durante {@code crear}, inmediatamente después de validar el request para
     * instanciar la entidad correcta en la estrategia de herencia JPA {@code JOINED}.</p>
     */
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

    /**
     * Hook del Template Method ejecutado antes de persistir la entidad en la base de datos.
     * Se encarga de:
     * 1. Crear y asociar la cuenta de {@link Usuario} del empleado con clave temporal/por defecto y rol correspondiente.
     * 2. Construir y asociar las colecciones de direcciones y contactos enviadas en el request.
     *
     * <p><b>Cuándo se usa:</b> Tanto en {@code crear} como en {@code modificar}, justo antes de ejecutar
     * el {@code save} en el repositorio.</p>
     */
    @Override
    protected void antesDePersistir(Empleado entidad, EmpleadoRequest request) {
        if (request.mayoristaId() != null) {
            Mayorista mayorista = mayoristaService.obtenerEntidad(request.mayoristaId());
            if (request.rol() == RolEmpleado.DISTRIBUIDOR && mayorista.getTipoEmpresa() != com.DulceMayor.backend.entities.mayorista.TipoEmpresa.SEDE_CENTRAL) {
                throw new ValidacionException("Los distribuidores solo pueden ser asignados a la sede central");
            }
            entidad.setMayorista(mayorista);
        }

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

    /**
     * Hook del Template Method posterior a la modificación de un empleado.
     * Sincroniza los cambios en el nombre de usuario y rol sobre la entidad {@link Usuario} vinculada.
     *
     * <p><b>Cuándo se usa:</b> Inmediatamente después de que el empleado modificado fue persistido con éxito en la base de datos.</p>
     */
    @Override
    protected void despuesDeModificar(Empleado entidad, EmpleadoRequest request) {
        if (entidad.getUsuario() != null) {
            usuarioService.actualizarUsuarioEmpleado(
                    entidad.getUsuario(), request.nombreUsuario(), toRolUsuario(request.rol()));
        }
    }

    /**
     * Hook del Template Method previo al borrado lógico del empleado.
     * Da de baja lógica la cuenta de {@link Usuario} asociada para inhabilitar el acceso al sistema.
     *
     * <p><b>Cuándo se usa:</b> Durante la ejecución de {@code eliminar(id)}, antes de marcar {@code eliminado = true} en el empleado.</p>
     */
    @Override
    protected void antesDeEliminar(Empleado existente) {
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

    /**
     * Consulta paginada del listado de empleados permitiendo filtrar por rol, legajo o término de búsqueda (nombre/apellido/dni).
     *
     * <p><b>Cuándo se usa:</b> Desde {@code EmpleadoController} en el endpoint {@code GET /empleados} para la administración de personal.</p>
     */
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

    /**
     * Obtiene y castea un empleado por su ID asegurando que sea de tipo {@link Preventor}.
     *
     * <p><b>Cuándo se usa:</b> En servicios que requieren asignar o vincular preventores (por ejemplo, al asignar preventores en pedidos o rutas).</p>
     */
    @Transactional(readOnly = true)
    public Preventor obtenerPreventor(String id) {
        Empleado emp = obtenerEntidad(id);
        if (!(emp instanceof Preventor preventor)) {
            throw new ValidacionException("El empleado con id " + id + " no es un Preventor");
        }
        return preventor;
    }

    /**
     * Obtiene y castea un empleado por su ID asegurando que sea de tipo {@link Distribuidor}.
     *
     * <p><b>Cuándo se usa:</b> En el módulo de logística y reparto al asociar distribuidores a las rutas de entrega.</p>
     */
    @Transactional(readOnly = true)
    public Distribuidor obtenerDistribuidor(String id) {
        Empleado emp = obtenerEntidad(id);
        if (!(emp instanceof Distribuidor distribuidor)) {
            throw new ValidacionException("El empleado con id " + id + " no es un Distribuidor");
        }
        return distribuidor;
    }

    /**
     * Recupera la entidad {@link Preventor} a partir del ID del usuario autenticado en la sesión.
     *
     * <p><b>Cuándo se usa:</b> En los endpoints donde el preventista opera en terreno (cargar pedidos, listar sus clientes, ver sus rutas asignadas).</p>
     */
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

    /**
     * Recupera la entidad {@link Distribuidor} a partir del ID del usuario autenticado en la sesión.
     *
     * <p><b>Cuándo se usa:</b> En el portal o aplicación del distribuidor al consultar su hoja de ruta diaria y registrar entregas.</p>
     */
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

    /**
     * Retorna todos los preventores activos registrados en el sistema.
     */
    @Transactional(readOnly = true)
    public List<Preventor> listarTodosLosPreventoresActivos() {
        return empleadoRepository.findAllByTipoAndEliminadoFalse(Preventor.class);
    }

    /**
     * Retorna todos los distribuidores activos registrados en el sistema.
     */
    @Transactional(readOnly = true)
    public List<Distribuidor> listarTodosLosDistribuidoresActivos() {
        return empleadoRepository.findAllByTipoAndEliminadoFalse(Distribuidor.class);
    }

    /**
     * Localiza el preventor cuya sucursal (Mayorista) se encuentra más cercana geodésicamente
     * a las coordenadas del punto de venta provisto (Fórmula de Haversine).
     */
    @Transactional(readOnly = true)
    public Preventor buscarPreventorMasCercano(double latitud, double longitud) {
        List<Preventor> preventores = listarTodosLosPreventoresActivos();
        if (preventores.isEmpty()) {
            return null;
        }

        Preventor mejorPreventor = null;
        double menorDistancia = Double.MAX_VALUE;

        for (Preventor p : preventores) {
            if (p.getMayorista() != null && p.getMayorista().getDirecciones() != null) {
                for (com.DulceMayor.backend.entities.direccion.Direccion dir : p.getMayorista().getDirecciones()) {
                    if (!dir.isEliminado() && dir.getLatitud() != null && dir.getLongitud() != null
                            && (dir.getLatitud() != 0.0 || dir.getLongitud() != 0.0)) {
                        double distancia = calcularDistanciaHaversine(latitud, longitud, dir.getLatitud(), dir.getLongitud());
                        if (distancia < menorDistancia) {
                            menorDistancia = distancia;
                            mejorPreventor = p;
                        }
                    }
                }
            }
        }

        return mejorPreventor != null ? mejorPreventor : preventores.get(0);
    }

    private double calcularDistanciaHaversine(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double phi1 = Math.toRadians(lat1);
        double phi2 = Math.toRadians(lat2);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(phi1) * Math.cos(phi2)
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return 6371000.0 * c;
    }

    /**
     * Mapea el enum de rol del DTO {@link RolEmpleado} hacia el enum de seguridad {@link RolUsuario}.
     */
    private RolUsuario toRolUsuario(RolEmpleado rol) {
        return RolUsuario.valueOf(rol.name());
    }

    /**
     * Valida que ninguno de los campos obligatorios del empleado sea nulo o esté vacío.
     */
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
        if (request.mayoristaId() == null || request.mayoristaId().isBlank()) {
            throw new ValidacionException("La sucursal o sede central (mayorista) es obligatoria");
        }
    }
}

