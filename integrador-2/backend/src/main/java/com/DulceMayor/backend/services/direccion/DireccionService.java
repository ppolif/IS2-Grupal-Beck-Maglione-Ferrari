package com.DulceMayor.backend.services.direccion;

import com.DulceMayor.backend.config.ValidacionException;
import com.DulceMayor.backend.dtos.direccion.DireccionMapper;
import com.DulceMayor.backend.dtos.direccion.DireccionRequest;
import com.DulceMayor.backend.dtos.direccion.DireccionResponse;
import com.DulceMayor.backend.entities.direccion.Direccion;
import com.DulceMayor.backend.entities.direccion.Localidad;
import com.DulceMayor.backend.repositories.direccion.DireccionRepository;
import com.DulceMayor.backend.services.BaseService;
import com.DulceMayor.backend.services.direccion.adapter.GeocodingPort;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio interno de dominio para la gestión, validación y geocodificación automática de {@link Direccion}.
 * No expone controlador REST propio ya que las direcciones se administran por composición o
 * referencia desde {@code Mayorista}, {@code Empleado}, {@code Minorista} y {@code PuntoDeVenta}.
 * Extiende {@link BaseService} cumpliendo la Regla 1 de ArchUnit al inyectar únicamente
 * {@link DireccionRepository}, delegando la resolución geográfica en {@link LocalidadService}
 * y la geocodificación vial en {@link GeocodingPort} (OpenStreetMap Nominatim + OSRM).
 */
@Service
public class DireccionService extends BaseService<Direccion, DireccionRequest, DireccionResponse> {

    private static final double DEFAULT_LAT_PLACEHOLDER = -32.8895;
    private static final double DEFAULT_LON_PLACEHOLDER = -68.8458;

    private final DireccionRepository direccionRepository;
    private final DireccionMapper direccionMapper;
    private final LocalidadService localidadService;
    private final GeocodingPort geocodingPort;

    public DireccionService(
            DireccionRepository direccionRepository,
            DireccionMapper direccionMapper,
            LocalidadService localidadService,
            GeocodingPort geocodingPort) {
        super(direccionRepository, direccionMapper, "Dirección");
        this.direccionRepository = direccionRepository;
        this.direccionMapper = direccionMapper;
        this.localidadService = localidadService;
        this.geocodingPort = geocodingPort;
    }

    @Override
    protected DireccionRequest prepararRequest(DireccionRequest request) {
        if (request == null) {
            return null;
        }
        return new DireccionRequest(
                request.calle() != null ? request.calle().trim() : null,
                request.numeracion() != null ? request.numeracion().trim() : null,
                request.barrio() != null ? request.barrio().trim() : null,
                request.manzanaPiso() != null ? request.manzanaPiso().trim() : null,
                request.casaDepartamento() != null ? request.casaDepartamento().trim() : null,
                request.referencia() != null ? request.referencia().trim() : null,
                request.latitud(),
                request.longitud(),
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
        Localidad localidad = localidadService.obtenerEntidad(request.localidadId());
        entidad.setLocalidad(localidad);
        resolverCoordenadasSiCorresponde(entidad, request.latitud(), request.longitud(), false);
    }

    /**
     * Construye, valida, geocodifica en OpenStreetMap/OSRM y persiste una entidad {@link Direccion}
     * a partir de su DTO de entrada.
     *
     * @param request datos de la dirección
     * @return entidad persistida en base de datos
     */
    public Direccion crearEntidad(DireccionRequest request) {
        Direccion entidad = construirEntidad(request);
        entidad.setEliminado(false);
        return direccionRepository.save(entidad);
    }

    public Direccion construirEntidad(DireccionRequest request) {
        DireccionRequest normalizado = prepararRequest(request);
        validarCrear(normalizado);
        Direccion entidad = direccionMapper.aEntidad(normalizado);
        Localidad localidad = localidadService.obtenerEntidad(normalizado.localidadId());
        entidad.setLocalidad(localidad);
        resolverCoordenadasSiCorresponde(entidad, normalizado.latitud(), normalizado.longitud(), false);
        return entidad;
    }

    /**
     * Actualiza los atributos de una {@link Direccion} existente, sincroniza su {@link Localidad}
     * y re-geocodifica en OpenStreetMap/OSRM si cambiaron los datos postales o carecía de coordenadas.
     *
     * @param entidad dirección existente a actualizar
     * @param request nuevos datos
     */
    public void actualizarEntidad(Direccion entidad, DireccionRequest request) {
        DireccionRequest normalizado = prepararRequest(request);
        validarModificar(entidad.getId(), normalizado, entidad);

        boolean cambioDireccionPostal = !Objects.equals(entidad.getCalle(), normalizado.calle())
                || !Objects.equals(entidad.getNumeracion(), normalizado.numeracion())
                || (entidad.getLocalidad() != null && !Objects.equals(entidad.getLocalidad().getId(), normalizado.localidadId()));

        direccionMapper.actualizarEntidad(normalizado, entidad);
        Localidad localidad = localidadService.obtenerEntidad(normalizado.localidadId());
        entidad.setLocalidad(localidad);
        resolverCoordenadasSiCorresponde(entidad, normalizado.latitud(), normalizado.longitud(), cambioDireccionPostal);
    }

    /**
     * Garantiza que una entidad {@link Direccion} ya persistida cuente con coordenadas reales geocodificadas
     * en OpenStreetMap y ajustadas a la red vial de OSRM. Si carecía de ellas, las calcula y persiste.
     *
     * @param direccion Dirección a verificar y geocodificar.
     * @return {@code true} si las coordenadas fueron actualizadas; {@code false} si ya contaba con coordenadas válidas.
     */
    @Transactional
    public boolean asegurarCoordenadas(Direccion direccion) {
        if (direccion == null) {
            return false;
        }
        if (careceDeCoordenadasReales(direccion.getCalle(), direccion.getLatitud(), direccion.getLongitud())) {
            double[] coords = geocodingPort.geocodificarDireccion(
                    direccion.getCalle(),
                    direccion.getNumeracion(),
                    direccion.getLocalidad()
            );
            direccion.setLatitud(coords[0]);
            direccion.setLongitud(coords[1]);
            if (direccion.getId() != null) {
                direccionRepository.save(direccion);
            }
            return true;
        }
        return false;
    }

    private void resolverCoordenadasSiCorresponde(
            Direccion entidad,
            Double latitudSolicitada,
            Double longitudSolicitada,
            boolean forzarPorCambioPostal) {

        if (forzarPorCambioPostal || careceDeCoordenadasReales(entidad.getCalle(), latitudSolicitada, longitudSolicitada)) {
            double[] coords = geocodingPort.geocodificarDireccion(
                    entidad.getCalle(),
                    entidad.getNumeracion(),
                    entidad.getLocalidad()
            );
            entidad.setLatitud(coords[0]);
            entidad.setLongitud(coords[1]);
        }
    }

    private boolean careceDeCoordenadasReales(String calle, Double lat, Double lon) {
        if (lat == null || lon == null || (lat == 0.0 && lon == 0.0)) {
            return true;
        }
        // Si tiene una calle específica distinta al placeholder "Gran Mendoza", pero trae las coordenadas genéricas por defecto
        boolean esCalleReal = calle != null && !calle.isBlank() && !"Gran Mendoza".equalsIgnoreCase(calle.trim());
        boolean esCoordPlaceholder = Math.abs(lat - DEFAULT_LAT_PLACEHOLDER) < 0.0001
                && Math.abs(lon - DEFAULT_LON_PLACEHOLDER) < 0.0001;
        return esCalleReal && esCoordPlaceholder;
    }

    private void validarFormato(DireccionRequest request) {
        if (request == null) {
            throw new ValidacionException("Los datos de la dirección son obligatorios");
        }
        if (request.calle() == null || request.calle().isBlank()) {
            throw new ValidacionException("La calle es obligatoria");
        }
        if (request.numeracion() == null || request.numeracion().isBlank()) {
            throw new ValidacionException("La numeración es obligatoria");
        }
        if (request.localidadId() == null || request.localidadId().isBlank()) {
            throw new ValidacionException("El id de la localidad es obligatorio");
        }
        if (request.latitud() != null && (request.latitud() < -90.0 || request.latitud() > 90.0)) {
            throw new ValidacionException("La latitud debe estar comprendida entre -90 y 90");
        }
        if (request.longitud() != null && (request.longitud() < -180.0 || request.longitud() > 180.0)) {
            throw new ValidacionException("La longitud debe estar comprendida entre -180 y 180");
        }
    }
}
