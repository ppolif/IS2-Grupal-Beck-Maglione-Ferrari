package com.DulceMayor.backend.services.direccion;

import com.DulceMayor.backend.config.ValidacionException;
import com.DulceMayor.backend.dtos.direccion.DireccionMapper;
import com.DulceMayor.backend.dtos.direccion.DireccionRequest;
import com.DulceMayor.backend.dtos.direccion.DireccionResponse;
import com.DulceMayor.backend.entities.direccion.Direccion;
import com.DulceMayor.backend.entities.direccion.Localidad;
import com.DulceMayor.backend.repositories.direccion.DireccionRepository;
import com.DulceMayor.backend.services.BaseService;
import org.springframework.stereotype.Service;

/**
 * Servicio interno de dominio para la gestión y validación de {@link Direccion}.
 * No expone controlador REST propio ya que las direcciones se administran por composición o
 * referencia desde {@code Mayorista}, {@code Empleado}, {@code Minorista} y {@code PuntoDeVenta}.
 * Extiende {@link BaseService} cumpliendo la Regla 1 de ArchUnit al inyectar únicamente
 * {@link DireccionRepository} y delegar la resolución geográfica en {@link LocalidadService}.
 */
@Service
public class DireccionService extends BaseService<Direccion, DireccionRequest, DireccionResponse> {

    private final DireccionRepository direccionRepository;
    private final DireccionMapper direccionMapper;
    private final LocalidadService localidadService;

    public DireccionService(
            DireccionRepository direccionRepository,
            DireccionMapper direccionMapper,
            LocalidadService localidadService) {
        super(direccionRepository, direccionMapper, "Dirección");
        this.direccionRepository = direccionRepository;
        this.direccionMapper = direccionMapper;
        this.localidadService = localidadService;
    }

    @Override
    protected DireccionRequest prepararRequest(DireccionRequest request) {
        if (request == null) {
            return null;
        }
        return new DireccionRequest(
                request.calle() != null ? request.calle().trim() : null,
                request.numero() != null ? request.numero().trim() : null,
                request.manzanaPiso() != null ? request.manzanaPiso().trim() : null,
                request.casaDepartamento() != null ? request.casaDepartamento().trim() : null,
                request.localidadId() != null ? request.localidadId().trim() : null
        );
    }

    @Override
    protected void validarCrear(DireccionRequest request) {
        validarFormato(request);
    }

    @Override
    protected void validarModificar(String id, DireccionRequest request, Direccion existente) {
        validarFormato(request);
    }

    @Override
    protected void antesDePersistir(Direccion entidad, DireccionRequest request) {
        if (request.localidadId() != null && !request.localidadId().isBlank()) {
            Localidad localidad = localidadService.obtenerEntidad(request.localidadId());
            entidad.setLocalidad(localidad);
        } else {
            entidad.setLocalidad(null);
        }
    }

    /**
     * Construye y valida una instancia desacoplada de {@link Direccion} a partir de su DTO de entrada.
     *
     * @param request datos de la dirección
     * @return entidad construida con su {@link Localidad} asociada
     */
    public Direccion construirEntidad(DireccionRequest request) {
        DireccionRequest normalizado = prepararRequest(request);
        validarCrear(normalizado);
        Direccion entidad = direccionMapper.aEntidad(normalizado);
        if (normalizado.localidadId() != null && !normalizado.localidadId().isBlank()) {
            Localidad localidad = localidadService.obtenerEntidad(normalizado.localidadId());
            entidad.setLocalidad(localidad);
        }
        return entidad;
    }

    /**
     * Actualiza los atributos de una {@link Direccion} existente y sincroniza su {@link Localidad}.
     *
     * @param entidad dirección existente a actualizar
     * @param request nuevos datos
     */
    public void actualizarEntidad(Direccion entidad, DireccionRequest request) {
        DireccionRequest normalizado = prepararRequest(request);
        validarModificar(entidad.getId(), normalizado, entidad);
        direccionMapper.actualizarEntidad(normalizado, entidad);
        if (normalizado.localidadId() != null && !normalizado.localidadId().isBlank()) {
            Localidad localidad = localidadService.obtenerEntidad(normalizado.localidadId());
            entidad.setLocalidad(localidad);
        } else {
            entidad.setLocalidad(null);
        }
    }

    private void validarFormato(DireccionRequest request) {
        if (request == null) {
            throw new ValidacionException("Los datos de la dirección son obligatorios");
        }
        if (request.calle() == null || request.calle().isBlank()) {
            throw new ValidacionException("La calle es obligatoria");
        }
        if (request.numero() == null || request.numero().isBlank()) {
            throw new ValidacionException("El número de dirección es obligatorio");
        }
    }
}

