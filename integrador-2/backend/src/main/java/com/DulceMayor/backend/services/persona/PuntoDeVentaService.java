package com.DulceMayor.backend.services.persona;

import com.DulceMayor.backend.config.UsuarioPrincipal;
import com.DulceMayor.backend.config.ValidacionException;
import com.DulceMayor.backend.dtos.persona.MinoristaEliminadoEvent;
import com.DulceMayor.backend.dtos.persona.PuntoDeVentaCarteraActualizadaEvent;
import com.DulceMayor.backend.dtos.persona.PuntoDeVentaMapper;
import com.DulceMayor.backend.dtos.persona.PuntoDeVentaPage;
import com.DulceMayor.backend.dtos.persona.PuntoDeVentaRequest;
import com.DulceMayor.backend.dtos.persona.PuntoDeVentaResponse;
import com.DulceMayor.backend.entities.direccion.Direccion;
import com.DulceMayor.backend.entities.persona.Minorista;
import com.DulceMayor.backend.entities.persona.Preventor;
import com.DulceMayor.backend.entities.persona.PuntoDeVenta;
import com.DulceMayor.backend.repositories.persona.PuntoDeVentaRepository;
import com.DulceMayor.backend.services.BaseService;
import com.DulceMayor.backend.services.direccion.DireccionService;
import java.util.List;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de negocio para la administración de comercios y puntos de venta.
 */
@Service
public class PuntoDeVentaService extends BaseService<PuntoDeVenta, PuntoDeVentaRequest, PuntoDeVentaResponse> {

    private final PuntoDeVentaRepository puntoDeVentaRepository;
    private final PuntoDeVentaMapper puntoDeVentaMapper;
    private final MinoristaService minoristaService;
    private final EmpleadoService empleadoService;
    private final DireccionService direccionService;
    private final ApplicationEventPublisher eventPublisher;

    public PuntoDeVentaService(
            PuntoDeVentaRepository puntoDeVentaRepository,
            PuntoDeVentaMapper puntoDeVentaMapper,
            MinoristaService minoristaService,
            EmpleadoService empleadoService,
            DireccionService direccionService,
            ApplicationEventPublisher eventPublisher) {
        super(puntoDeVentaRepository, puntoDeVentaMapper, "Punto de Venta");
        this.puntoDeVentaRepository = puntoDeVentaRepository;
        this.puntoDeVentaMapper = puntoDeVentaMapper;
        this.minoristaService = minoristaService;
        this.empleadoService = empleadoService;
        this.direccionService = direccionService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    protected PuntoDeVentaRequest prepararRequest(PuntoDeVentaRequest request) {
        if (request == null) {
            return null;
        }
        String minId = request.minoristaId() != null ? request.minoristaId().trim() : null;
        if ((minId == null || minId.isBlank()) && esMinoristaAutenticado()) {
            UsuarioPrincipal principal = obtenerPrincipalAutenticado();
            if (principal != null) {
                Minorista min = minoristaService.obtenerOCrearPorUsuarioId(principal.getId());
                minId = min.getId();
            }
        }
        return new PuntoDeVentaRequest(
                request.nombreComercio() != null ? request.nombreComercio().trim() : null,
                minId,
                request.direccion(),
                request.preventorId() != null ? request.preventorId().trim() : null
        );
    }

    @Override
    protected void validarCrear(PuntoDeVentaRequest request) {
        validarFormato(request);
        Minorista minorista = minoristaService.obtenerEntidad(request.minoristaId());
        if (esMinoristaAutenticado()) {
            UsuarioPrincipal principal = obtenerPrincipalAutenticado();
            if (principal != null && minorista.getUsuario() != null
                    && !principal.getId().equals(minorista.getUsuario().getId())) {
                throw new ValidacionException("No tiene permisos para dar de alta puntos de venta en nombre de otro minorista");
            }
        }
        if (request.preventorId() != null && !request.preventorId().isBlank()) {
            empleadoService.obtenerPreventor(request.preventorId());
        }
    }

    @Override
    protected void validarModificar(String id, PuntoDeVentaRequest request, PuntoDeVenta existente) {
        validarFormato(request);
        minoristaService.obtenerEntidad(request.minoristaId());
        if (request.preventorId() != null && !request.preventorId().isBlank()) {
            empleadoService.obtenerPreventor(request.preventorId());
        }
    }

    @Override
    protected void antesDePersistir(PuntoDeVenta entidad, PuntoDeVentaRequest request) {
        Minorista minorista = minoristaService.obtenerEntidad(request.minoristaId());
        entidad.setMinorista(minorista);

        if (entidad.getDireccion() == null) {
            Direccion dir = direccionService.crearEntidad(request.direccion());
            entidad.setDireccion(dir);
        } else {
            direccionService.actualizarEntidad(entidad.getDireccion(), request.direccion());
        }

        if (request.preventorId() != null && !request.preventorId().isBlank()) {
            Preventor preventor = empleadoService.obtenerPreventor(request.preventorId());
            entidad.setPreventor(preventor);
        } else if (entidad.getPreventor() == null && esPreventistaAutenticado()) {
            UsuarioPrincipal principal = obtenerPrincipalAutenticado();
            if (principal != null) {
                Preventor preventor = empleadoService.obtenerPreventorPorUsuarioId(principal.getId());
                entidad.setPreventor(preventor);
            }
        } else if (entidad.getPreventor() == null) {
            // Alta desde portal web por minorista: asignación automática por distancia vial OSRM a sucursales
            Direccion dir = entidad.getDireccion();
            if (dir != null && dir.getLatitud() != null && dir.getLongitud() != null
                    && (dir.getLatitud() != 0.0 || dir.getLongitud() != 0.0)) {
                Preventor masCercano = empleadoService.buscarPreventorMasCercano(dir.getLatitud(), dir.getLongitud());
                entidad.setPreventor(masCercano);
            }
        }
    }

    @Override
    protected void despuesDeCrear(PuntoDeVenta entidad, PuntoDeVentaRequest request) {
        notificarActualizacionCarteraPreventor(entidad);
    }

    @Override
    protected void despuesDeModificar(PuntoDeVenta entidad, PuntoDeVentaRequest request) {
        notificarActualizacionCarteraPreventor(entidad);
    }

    private void notificarActualizacionCarteraPreventor(PuntoDeVenta entidad) {
        if (eventPublisher != null && entidad != null && entidad.getPreventor() != null) {
            eventPublisher.publishEvent(new PuntoDeVentaCarteraActualizadaEvent(
                    entidad.getPreventor().getId(),
                    entidad.getId()
            ));
        }
    }

    @Transactional(readOnly = true)
    public List<PuntoDeVenta> listarPorPreventor(String preventorId) {
        if (preventorId == null || preventorId.isBlank()) {
            return List.of();
        }
        return puntoDeVentaRepository.findAllByPreventorIdAndEliminadoFalse(preventorId.trim());
    }

    @Transactional(readOnly = true)
    public PuntoDeVentaPage buscarPaginado(String minoristaId, String preventorId, String q, Pageable pageable) {
        String preventorFiltro = preventorId != null && !preventorId.isBlank() ? preventorId.trim() : null;

        /// Si quien consulta es un PREVENTISTA:
        ///
        /// El sistema ignora cualquier preventorId que venga en los parámetros de la URL.
        /// Obtiene automáticamente el ID del preventista autenticado (miPreventor.getId()) y fuerza el filtro con ese valor.
        /// De esta manera, un preventista solo puede listar y ver su propia cartera de puntos de venta/clientes y no la de sus colegas.
        if (esPreventistaAutenticado()) {
            UsuarioPrincipal principal = obtenerPrincipalAutenticado();
            if (principal != null) {
                Preventor miPreventor = empleadoService.obtenerPreventorPorUsuarioId(principal.getId());
                preventorFiltro = miPreventor.getId();
            }
        }

        String minoristaFiltro = minoristaId != null && !minoristaId.isBlank() ? minoristaId.trim() : null;
        if (esMinoristaAutenticado()) {
            UsuarioPrincipal principal = obtenerPrincipalAutenticado();
            if (principal != null) {
                java.util.Optional<Minorista> miMinorista = minoristaService.buscarPorUsuarioId(principal.getId());
                if (miMinorista.isEmpty()) {
                    return new PuntoDeVentaPage(
                            List.of(),
                            pageable.getPageNumber(),
                            pageable.getPageSize(),
                            0,
                            0
                    );
                }
                minoristaFiltro = miMinorista.get().getId();
            }
        }
        String qFiltro = q != null && !q.isBlank() ? q.trim() : null;

        Page<PuntoDeVenta> page = puntoDeVentaRepository.buscarConFiltros(
                minoristaFiltro, preventorFiltro, qFiltro, pageable);
        List<PuntoDeVentaResponse> content = mapper.aResponseList(page.getContent());
        return new PuntoDeVentaPage(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    /// subscriptor del patron observer, si se elimina el mayorista se eliminan todos sus puntos de venta
    @EventListener
    @Transactional
    public void onMinoristaEliminado(MinoristaEliminadoEvent event) {
        if (event == null || event.minoristaId() == null) return;
        List<PuntoDeVenta> puntos = puntoDeVentaRepository.findAllByMinoristaIdAndEliminadoFalse(event.minoristaId());
        for (PuntoDeVenta pv : puntos) {
            pv.setEliminado(true);
        }
        puntoDeVentaRepository.saveAll(puntos);
    }

    private boolean esPreventistaAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities() == null) {
            return false;
        }
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_PREVENTISTA") || a.getAuthority().equals("PREVENTISTA"));
    }

    private boolean esMinoristaAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities() == null) {
            return false;
        }
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_MINORISTA") || a.getAuthority().equals("MINORISTA"));
    }

    private UsuarioPrincipal obtenerPrincipalAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UsuarioPrincipal principal) {
            return principal;
        }
        return null;
    }

    private void validarFormato(PuntoDeVentaRequest request) {
        if (request == null) {
            throw new ValidacionException("Los datos del punto de venta son obligatorios");
        }
        if (request.nombreComercio() == null || request.nombreComercio().isBlank()) {
            throw new ValidacionException("El nombre del comercio es obligatorio");
        }
        if (request.minoristaId() == null || request.minoristaId().isBlank()) {
            throw new ValidacionException("El minorista es obligatorio");
        }
        if (request.direccion() == null) {
            throw new ValidacionException("La dirección es obligatoria");
        }
    }
}
