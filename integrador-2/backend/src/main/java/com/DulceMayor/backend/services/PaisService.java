package com.DulceMayor.backend.services;

import com.DulceMayor.backend.config.ConflictoException;
import com.DulceMayor.backend.config.ValidacionException;
import com.DulceMayor.backend.dtos.BaseMapper;
import com.DulceMayor.backend.dtos.PaisMapper;
import com.DulceMayor.backend.dtos.PaisRequest;
import com.DulceMayor.backend.dtos.PaisResponse;
import com.DulceMayor.backend.entities.Pais;
import com.DulceMayor.backend.repositories.BaseRepository;
import com.DulceMayor.backend.repositories.PaisRepository;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Servicio de negocio para la gestión de países del diccionario geográfico.
 * Aplica el patrón Template Method heredando de {@link BaseService}.
 */
@Service
public class PaisService extends BaseService<Pais, PaisRequest, PaisResponse> {

    private static final int LONGITUD_MAXIMA_NOMBRE = 100;

    private final PaisRepository paisRepository;
    private final PaisMapper paisMapper;

    public PaisService(
            PaisRepository paisRepository,
            PaisMapper paisMapper) {
        this.paisRepository = paisRepository;
        this.paisMapper = paisMapper;
    }

    @Override
    protected BaseRepository<Pais> getRepository() {
        return paisRepository;
    }

    @Override
    protected BaseMapper<Pais, PaisRequest, PaisResponse> getMapper() {
        return paisMapper;
    }

    @Override
    protected String getNombreRecurso() {
        return "País";
    }

    @Override
    protected PaisRequest prepararRequest(PaisRequest request) {
        if (request == null || request.nombre() == null) {
            return request;
        }
        return new PaisRequest(request.nombre().trim());
    }

    @Override
    protected void validarCrear(PaisRequest request) {
        validarFormatoNombre(request);
        if (paisRepository.existsByNombreIgnoreCaseAndEliminadoFalse(request.nombre())) {
            throw new ConflictoException(
                    "DUPLICADO",
                    "Ya existe un país con el nombre '" + request.nombre() + "'"
            );
        }
    }

    @Override
    protected void validarModificar(String id, PaisRequest request, Pais existente) {
        validarFormatoNombre(request);
        if (paisRepository.existsByNombreIgnoreCaseAndIdNotAndEliminadoFalse(request.nombre(), id)) {
            throw new ConflictoException(
                    "DUPLICADO",
                    "Ya existe otro país con el nombre '" + request.nombre() + "'"
            );
        }
    }

    @Override
    protected List<Pais> buscarEntidadesActivas() {
        return paisRepository.findAllByEliminadoFalseOrderByNombreAsc();
    }

    private void validarFormatoNombre(PaisRequest request) {
        if (request == null || request.nombre() == null || request.nombre().isBlank()) {
            throw new ValidacionException("El nombre del país es obligatorio");
        }
        if (request.nombre().length() > LONGITUD_MAXIMA_NOMBRE) {
            throw new ValidacionException(
                    "El nombre del país no puede superar los " + LONGITUD_MAXIMA_NOMBRE + " caracteres"
            );
        }
    }
}

