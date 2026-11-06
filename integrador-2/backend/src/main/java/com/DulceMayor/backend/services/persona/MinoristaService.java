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

    /**
     * Normaliza los datos de entrada del minorista (recorte de espacios en blanco).
     *
     * <p><b>Cuándo se usa:</b> Automáticamente al inicio de las operaciones {@code crear} y {@code modificar}
     * del Template Method en {@link BaseService}, garantizando consistencia en validaciones y persistencia.</p>
     */
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

    /**
     * Valida el formato y la unicidad de tipo y número de documento para el alta de un minorista.
     * Si el alta es realizada por un preventista en terreno, exige obligatoriamente un contacto
     * de correo electrónico válido, ya que este se utilizará como nombre de usuario para su cuenta.
     *
     * <p><b>Cuándo se usa:</b> Durante {@code crear(request)} antes de persistir la entidad.</p>
     */
    @Override
    protected void validarCrear(MinoristaRequest request) {
        validarFormato(request);
        personaService.validarDocumentoUnico(request.tipoDocumento(), request.numeroDocumento());

        ///si esta siendo registrado en campo, en los contactos debe venir un mail si o si, que sera el del usuario que se cree
        if (esPreventistaAutenticado()) {
            String email = obtenerEmailDeContactos(request);
            if (email == null) {
                throw new ValidacionException(
                        "Al registrar un minorista como preventista es obligatorio ingresar un contacto de tipo CORREO con su email, ya que será su nombre de usuario en el sistema."
                );
            }
            if (usuarioService.existeNombreUsuario(email)) {
                throw new ConflictoException("DUPLICADO", "El correo ingresado ya se encuentra registrado por otro usuario");
            }
        }
    }

    /**
     * Valida el formato y la unicidad del documento excluyendo el ID del propio minorista que se modifica.
     *
     * <p><b>Cuándo se usa:</b> Durante {@code modificar(id, request)} antes de aplicar los cambios.</p>
     *
     * El metodo crear del minorista es el que se llama si lo registra un preventor, sino se llama registroEnPortal
     */
    @Override
    protected void validarModificar(String id, MinoristaRequest request, Minorista existente) {
        validarFormato(request);
    }

    /**
     * Hook del Template Method previo a la persistencia (alta o modificación administrativa):
     * 1. Si el alta es realizada por un preventista en terreno y el cliente no tiene usuario,
     *    le genera automáticamente una cuenta con clave temporal por defecto (regla de negocio 01-dominio.md).
     * 2. Construye y asocia las colecciones anidadas de direcciones y contactos.
     *
     * <p><b>Cuándo se usa:</b> Inmediatamente antes de ejecutar {@code save} en {@code crear} y {@code modificar}.</p>
     */
    @Override
    protected void antesDePersistir(Minorista entidad, MinoristaRequest request) {
        // Si lo da de alta un preventor en terreno y no tiene usuario aún, se crea con claveTemporal = true
        if (entidad.getUsuario() == null && esPreventistaAutenticado()) {
            String email = obtenerEmailDeContactos(request);

            Usuario usuario = usuarioService.crearUsuario(
                    email,
                    UsuarioService.CLAVE_POR_DEFECTO,
                    RolUsuario.MINORISTA,
                    true
            );
            entidad.setUsuario(usuario);
        }

        /// como los atributos de minorista tienen CascadeType.ALL, al construirlos y agregarlos en la entidad minorista,
        /// cuando se persiste el minorista JPA se encarga de persistir todas las entidades "hijas"
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
     * Proceso de registro público de un cliente minorista en el portal web (auto-registro).
     *
     * <p>Dado que cuando un preventor registra a un minorista en terreno ya se le crea una cuenta de usuario
     * con clave temporal, un minorista existente en el sistema ya posee usuario asignado. Por lo tanto,
     * este método realiza un auto-registro desde cero para nuevos clientes y rechaza solicitudes si el
     * correo o el documento ya se encuentran registrados en el sistema.</p>
     *
     * <p><b>Cuándo se usa:</b> Al invocar {@code POST /auth/registro} en {@code AuthController}.</p>
     */
    @Transactional
    public AuthResponse registrarEnPortal(RegistroMinoristaRequest request) {
        validarRegistro(request);

        String emailLimpio = request.email().trim().toLowerCase();
        if (usuarioService.existeNombreUsuario(emailLimpio)) {
            throw new ConflictoException("DUPLICADO", "El correo ya posee un usuario registrado");
        }

        personaService.validarDocumentoUnico(request.tipoDocumento(), request.numeroDocumento().trim());

        Usuario usuario = usuarioService.crearUsuario(
                emailLimpio,
                request.clave(),
                RolUsuario.MINORISTA,
                false
        );

        /// el mail que usa para registrarse se guarda automatico como un contacto
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

        Minorista nuevo = Minorista.builder()
                .nombre(request.nombre().trim())
                .apellido(request.apellido().trim())
                .fechaNacimiento(request.fechaNacimiento())
                .tipoDocumento(request.tipoDocumento())
                .numeroDocumento(request.numeroDocumento().trim())
                .usuario(usuario)
                .contactos(contactos)
                .eliminado(false)
                .build();

        minoristaRepository.save(nuevo);
        return usuarioService.generarAuthResponse(usuario);
    }

    /**
     * Búsqueda paginada de clientes minoristas con filtros opcionales por documento o término de búsqueda.
     *
     * <p><b>Cuándo se usa:</b> Desde {@code MinoristaController} en {@code GET /minoristas} para panel administrativo y preventistas.</p>
     */
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

    /**
     * Busca opcionalmente un minorista por el ID de su cuenta de usuario.
     *
     * <p><b>Cuándo se usa:</b> En consultas de seguridad o autorizaciones donde el usuario puede o no ser un minorista.</p>
     */
    @Transactional(readOnly = true)
    public Optional<Minorista> buscarPorUsuarioId(String usuarioId) {
        return minoristaRepository.findByUsuarioIdAndEliminadoFalse(usuarioId);
    }

    /**
     * Obtiene la entidad {@link Minorista} a partir del ID del usuario, lanzando 404 si no existe o está eliminado.
     *
     * <p><b>Cuándo se usa:</b> En servicios de dominio (pedidos, facturas, puntos de venta) para asociar el cliente autenticado.</p>
     */
    @Transactional(readOnly = true)
    public Minorista obtenerPorUsuarioId(String usuarioId) {
        return minoristaRepository.findByUsuarioIdAndEliminadoFalse(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró minorista para el usuario con id: " + usuarioId));
    }

    /**
     * Proyecta el perfil personal del minorista autenticado a DTO de respuesta.
     *
     * <p><b>Cuándo se usa:</b> En el endpoint {@code GET /mi-cuenta/perfil} del portal del cliente.</p>
     */
    @Transactional(readOnly = true)
    public MinoristaResponse obtenerMiPerfil(String usuarioId) {
        Minorista minorista = obtenerPorUsuarioId(usuarioId);
        return mapper.aResponse(minorista);
    }

    /**
     * Actualiza los datos de contacto, documento y credencial de correo del minorista autenticado.
     *
     * <p><b>Cuándo se usa:</b> En el endpoint {@code PUT /mi-cuenta/perfil} del portal del cliente.</p>
     */
    @Transactional
    public MinoristaResponse actualizarMiPerfil(String usuarioId, ActualizarPerfilMinoristaRequest request) {
        if (request == null) {
            throw new ValidacionException("Los datos de perfil son obligatorios");
        }
        Minorista minorista = obtenerPorUsuarioId(usuarioId);

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

        Minorista guardado = minoristaRepository.save(minorista);
        return mapper.aResponse(guardado);
    }

    /**
     * Da de baja la cuenta del propio cliente autenticado (derecho al olvido).
     *
     * <p><b>Cuándo se usa:</b> En el endpoint {@code DELETE /mi-cuenta} del portal del cliente.</p>
     */
    @Transactional
    public void eliminarMiCuenta(String usuarioId) {
        Minorista minorista = obtenerPorUsuarioId(usuarioId);
        eliminar(minorista.getId());
    }

    /**
     * Hook del Template Method previo al borrado lógico del minorista.
     * Da de baja su {@link Usuario}, sus contactos y sus direcciones en cascada lógica.
     *
     * <p><b>Cuándo se usa:</b> Durante la ejecución de {@code eliminar(id)}.</p>
     */
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

    /**
     * Hook del Template Method posterior al borrado lógico del minorista.
     * Emite el evento {@link MinoristaEliminadoEvent} para que otros observadores
     * (por ejemplo, puntos de venta asociados) reaccionen desacopladamente.
     *
     * <p><b>Cuándo se usa:</b> Inmediatamente después de completar el guardado del borrado lógico.</p>
     */
    @Override
    protected void despuesDeEliminar(Minorista existente) {
        if (eventPublisher != null) {
            eventPublisher.publishEvent(new MinoristaEliminadoEvent(existente.getId()));
        }
    }

    /**
     * Verifica si el usuario autenticado en el contexto de seguridad posee rol PREVENTISTA.
     * lo cual significa que el minorista esta siendo registrado en campo
     */
    private boolean esPreventistaAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities() == null) {
            return false;
        }
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_PREVENTISTA") || a.getAuthority().equals("PREVENTISTA"));
    }

    /**
     * Resuelve el nombre de usuario sugerido para el minorista (su correo o número de documento).
     * el minorista hereda de persona, asi que no hay email en MinoristaRequest para directamente pedir que se complete ese
     * campo en el caso de que el minorista este siendo registrado por un preventor
     * por eso se usan los contactos, y nos aseguramos de que se este ingresando un contacto de correo que funcionará como el
     * correo del usuario del minorista
     */
    private String obtenerEmailDeContactos(MinoristaRequest request) {
        if (request.contactos() != null) {
            for (ContactoRequest c : request.contactos()) {
                if ("CORREO".equalsIgnoreCase(c.medio()) && c.email() != null && !c.email().isBlank()) {
                    return c.email().trim().toLowerCase();
                }
            }
        }
        return null;
    }

    /**
     * Valida la presencia de campos obligatorios para el request administrativo de minorista.
     */
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

    /**
     * Valida los requisitos de formato y longitud de clave en el auto-registro del portal minorista.
     */
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

