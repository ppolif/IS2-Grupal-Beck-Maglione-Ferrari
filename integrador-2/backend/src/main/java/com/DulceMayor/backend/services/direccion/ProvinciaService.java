package com.DulceMayor.backend.services.direccion;

import com.DulceMayor.backend.config.ConflictoException;
import com.DulceMayor.backend.config.ValidacionException;
import com.DulceMayor.backend.dtos.BaseMapper;
import com.DulceMayor.backend.dtos.direccion.ProvinciaMapper;
import com.DulceMayor.backend.dtos.direccion.ProvinciaRequest;
import com.DulceMayor.backend.dtos.direccion.ProvinciaResponse;
import com.DulceMayor.backend.entities.direccion.Pais;
import com.DulceMayor.backend.entities.direccion.Provincia;
import com.DulceMayor.backend.repositories.BaseRepository;
import com.DulceMayor.backend.repositories.direccion.ProvinciaRepository;
import java.util.List;

import com.DulceMayor.backend.services.BaseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de negocio para la gestión de provincias del diccionario geográfico.
 * Aplica el patrón Template Method heredando de {@link BaseService}.
 */
@Service
public class ProvinciaService extends BaseService<Provincia, ProvinciaRequest, ProvinciaResponse> {

    private static final int LONGITUD_MAXIMA_NOMBRE = 100;

    private final ProvinciaRepository provinciaRepository;
    private final PaisService paisService;

    public ProvinciaService(
            ProvinciaRepository provinciaRepository,
            ProvinciaMapper provinciaMapper,
            PaisService paisService) {
        super(provinciaRepository, provinciaMapper, "Provincia");
        this.provinciaRepository = provinciaRepository;
        this.paisService = paisService;
    }

    @Override
    protected ProvinciaRequest prepararRequest(ProvinciaRequest request) {
        if (request == null) {
            return null;
        }
        String nombreLimpio = request.nombre() != null ? request.nombre().trim() : null;
        return new ProvinciaRequest(nombreLimpio, request.paisId());
    }

    @Override
    protected void validarCrear(ProvinciaRequest request) {
        validarFormato(request);
        paisService.obtenerEntidad(request.paisId());
        if (provinciaRepository.existsByNombreIgnoreCaseAndPaisIdAndEliminadoFalse(
                request.nombre(), request.paisId())) {
            throw new ConflictoException(
                    "DUPLICADO",
                    "Ya existe una provincia con el nombre '" + request.nombre() + "' en el país indicado"
            );
        }
    }

    @Override
    protected void validarModificar(String id, ProvinciaRequest request, Provincia existente) {
        validarFormato(request);
        paisService.obtenerEntidad(request.paisId());
        if (provinciaRepository.existsByNombreIgnoreCaseAndPaisIdAndIdNotAndEliminadoFalse(
                request.nombre(), request.paisId(), id)) {
            throw new ConflictoException(
                    "DUPLICADO",
                    "Ya existe otra provincia con el nombre '" + request.nombre() + "' en el país indicado"
            );
        }
    }

    @Override
    protected void antesDePersistir(Provincia entidad, ProvinciaRequest request) {
        Pais pais = paisService.obtenerEntidad(request.paisId());
        entidad.setPais(pais);
    }

    @Override
    protected List<Provincia> buscarEntidadesActivas() {
        return provinciaRepository.findAllByEliminadoFalseOrderByNombreAsc();
    }

    @Transactional(readOnly = true)
    public List<ProvinciaResponse> listar(String paisId) {
        if (paisId == null || paisId.isBlank()) {
            return listar();
        }
        List<Provincia> entidades = provinciaRepository.findAllByPaisIdAndEliminadoFalseOrderByNombreAsc(paisId);
        return mapper.aResponseList(entidades);
    }

    private void validarFormato(ProvinciaRequest request) {
        if (request == null) {
            throw new ValidacionException("Los datos de la provincia son obligatorios");
        }
        if (request.nombre() == null || request.nombre().isBlank()) {
            throw new ValidacionException("El nombre de la provincia es obligatorio");
        }
        if (request.nombre().length() > LONGITUD_MAXIMA_NOMBRE) {
            throw new ValidacionException(
                    "El nombre de la provincia no puede superar los " + LONGITUD_MAXIMA_NOMBRE + " caracteres"
            );
        }
        if (request.paisId() == null || request.paisId().isBlank()) {
            throw new ValidacionException("El id del país es obligatorio");
        }
    }
}

