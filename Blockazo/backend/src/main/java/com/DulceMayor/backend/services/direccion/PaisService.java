package com.DulceMayor.backend.services.direccion;

import com.DulceMayor.backend.config.ConflictoException;
import com.DulceMayor.backend.config.ValidacionException;
import com.DulceMayor.backend.dtos.BaseMapper;
import com.DulceMayor.backend.dtos.direccion.PaisMapper;
import com.DulceMayor.backend.dtos.direccion.PaisRequest;
import com.DulceMayor.backend.dtos.direccion.PaisResponse;
import com.DulceMayor.backend.entities.direccion.Pais;
import com.DulceMayor.backend.repositories.BaseRepository;
import com.DulceMayor.backend.repositories.direccion.PaisRepository;
import java.util.List;

import com.DulceMayor.backend.services.BaseService;
import org.springframework.stereotype.Service;

/**
 * Servicio de negocio para la gestión de países del diccionario geográfico.
 * Aplica el patrón Template Method heredando de {@link BaseService}.
 */
@Service
public class PaisService extends BaseService<Pais, PaisRequest, PaisResponse> {

    private static final int LONGITUD_MAXIMA_NOMBRE = 100;

    private final PaisRepository paisRepository;

    public PaisService(
            PaisRepository paisRepository,
            PaisMapper paisMapper) {
        super(paisRepository, paisMapper, "País");
        this.paisRepository = paisRepository;
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

