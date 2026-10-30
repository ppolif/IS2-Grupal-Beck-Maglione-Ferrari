package com.DulceMayor.backend.services.persona;

import com.DulceMayor.backend.config.UsuarioPrincipal;
import com.DulceMayor.backend.config.ValidacionException;
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
import com.DulceMayor.backend.dtos.persona.MinoristaEliminadoEvent;
import org.springframework.context.event.EventListener;
import java.util.List;
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

    public PuntoDeVentaService(
            PuntoDeVentaRepository puntoDeVentaRepository,
            PuntoDeVentaMapper puntoDeVentaMapper,
            MinoristaService minoristaService,
            EmpleadoService empleadoService,
            DireccionService direccionService) {
        super(puntoDeVentaRepository, puntoDeVentaMapper, "Punto de Venta");
        this.puntoDeVentaRepository = puntoDeVentaRepository;
        this.puntoDeVentaMapper = puntoDeVentaMapper;
        this.minoristaService = minoristaService;
        this.empleadoService = empleadoService;
        this.direccionService = direccionService;
    }

    @Override
    protected PuntoDeVentaRequest prepararRequest(PuntoDeVentaRequest request) {
        if (request == null) {
            return null;
        }
        return new PuntoDeVentaRequest(
                request.nombreComercio() != null ? request.nombreComercio().trim() : null,
                request.minoristaId() != null ? request.minoristaId().trim() : null,
                request.direccion(),
                request.preventorId() != null ? request.preventorId().trim() : null
        );
    }

    @Override
    protected void validarCrear(PuntoDeVentaRequest request) {
        validarFormato(request);
        minoristaService.obtenerEntidad(request.minoristaId());
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
            Direccion dir = direccionService.construirEntidad(request.direccion());
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
        }
    }

    @Transactional(readOnly = true)
    public PuntoDeVentaPage buscarPaginado(String minoristaId, String preventorId, String q, Pageable pageable) {
        String preventorFiltro = preventorId != null && !preventorId.isBlank() ? preventorId.trim() : null;
        if (esPreventistaAutenticado()) {
            UsuarioPrincipal principal = obtenerPrincipalAutenticado();
            if (principal != null) {
                Preventor miPreventor = empleadoService.obtenerPreventorPorUsuarioId(principal.getId());
                preventorFiltro = miPreventor.getId();
            }
        }

        String minoristaFiltro = minoristaId != null && !minoristaId.isBlank() ? minoristaId.trim() : null;
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

    @EventListener
    @Transactional
    public void onMinoristaEliminado(MinoristaEliminadoEvent event) {
        if (event == null || event.minoristaId() == null) return;
        List<PuntoDeVenta> puntos = puntoDeVentaRepository.findAllByMinoristaIdAndEliminadoFalse(event.minoristaId());
        for (PuntoDeVenta pv : puntos) {
            pv.setEliminado(true);
            if (pv.getDireccion() != null) {
                pv.getDireccion().setEliminado(true);
            }
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
            throw new ValidacionException("El id del minorista es obligatorio");
        }
        if (request.direccion() == null) {
            throw new ValidacionException("La dirección es obligatoria");
        }
    }
}

