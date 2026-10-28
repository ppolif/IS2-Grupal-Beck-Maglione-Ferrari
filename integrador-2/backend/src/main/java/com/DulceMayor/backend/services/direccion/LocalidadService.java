package com.DulceMayor.backend.services.direccion;

import com.DulceMayor.backend.config.ConflictoException;
import com.DulceMayor.backend.config.ValidacionException;
import com.DulceMayor.backend.dtos.BaseMapper;
import com.DulceMayor.backend.dtos.direccion.LocalidadMapper;
import com.DulceMayor.backend.dtos.direccion.LocalidadRequest;
import com.DulceMayor.backend.dtos.direccion.LocalidadResponse;
import com.DulceMayor.backend.entities.direccion.Departamento;
import com.DulceMayor.backend.entities.direccion.Localidad;
import com.DulceMayor.backend.repositories.BaseRepository;
import com.DulceMayor.backend.repositories.direccion.LocalidadRepository;
import java.util.List;

import com.DulceMayor.backend.services.BaseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de negocio para la gestión de localidades del diccionario geográfico.
 * Aplica el patrón Template Method heredando de {@link BaseService}.
 */
@Service
public class LocalidadService extends BaseService<Localidad, LocalidadRequest, LocalidadResponse> {

    private static final int LONGITUD_MAXIMA_NOMBRE = 100;
    private static final int LONGITUD_MAXIMA_CODIGO_POSTAL = 20;

    private final LocalidadRepository localidadRepository;
    private final LocalidadMapper localidadMapper;
    private final DepartamentoService departamentoService;

    public LocalidadService(
            LocalidadRepository localidadRepository,
            LocalidadMapper localidadMapper,
            DepartamentoService departamentoService) {
        this.localidadRepository = localidadRepository;
        this.localidadMapper = localidadMapper;
        this.departamentoService = departamentoService;
    }

    @Override
    protected BaseRepository<Localidad> getRepository() {
        return localidadRepository;
    }

    @Override
    protected BaseMapper<Localidad, LocalidadRequest, LocalidadResponse> getMapper() {
        return localidadMapper;
    }

    @Override
    protected String getNombreRecurso() {
        return "Localidad";
    }

    @Override
    protected LocalidadRequest prepararRequest(LocalidadRequest request) {
        if (request == null) {
            return null;
        }
        String nombreLimpio = request.nombre() != null ? request.nombre().trim() : null;
        String codigoPostalLimpio = request.codigoPostal() != null ? request.codigoPostal().trim() : null;
        return new LocalidadRequest(nombreLimpio, codigoPostalLimpio, request.departamentoId());
    }

    @Override
    protected void validarCrear(LocalidadRequest request) {
        validarFormato(request);
        departamentoService.obtenerEntidad(request.departamentoId());
        if (localidadRepository.existsByNombreIgnoreCaseAndDepartamentoIdAndEliminadoFalse(
                request.nombre(), request.departamentoId())) {
            throw new ConflictoException(
                    "DUPLICADO",
                    "Ya existe una localidad con el nombre '" + request.nombre() + "' en el departamento indicado"
            );
        }
    }

    @Override
    protected void validarModificar(String id, LocalidadRequest request, Localidad existente) {
        validarFormato(request);
        departamentoService.obtenerEntidad(request.departamentoId());
        if (localidadRepository.existsByNombreIgnoreCaseAndDepartamentoIdAndIdNotAndEliminadoFalse(
                request.nombre(), request.departamentoId(), id)) {
            throw new ConflictoException(
                    "DUPLICADO",
                    "Ya existe otra localidad con el nombre '" + request.nombre() + "' en el departamento indicado"
            );
        }
    }

    @Override
    protected void antesDePersistir(Localidad entidad, LocalidadRequest request) {
        Departamento departamento = departamentoService.obtenerEntidad(request.departamentoId());
        entidad.setDepartamento(departamento);
    }

    @Override
    protected List<Localidad> buscarEntidadesActivas() {
        return localidadRepository.findAllByEliminadoFalseOrderByNombreAsc();
    }

    @Transactional(readOnly = true)
    public List<LocalidadResponse> listar(String departamentoId) {
        if (departamentoId == null || departamentoId.isBlank()) {
            return listar();
        }
        List<Localidad> entidades =
                localidadRepository.findAllByDepartamentoIdAndEliminadoFalseOrderByNombreAsc(departamentoId);
        return localidadMapper.aResponseList(entidades);
    }

    private void validarFormato(LocalidadRequest request) {
        if (request == null) {
            throw new ValidacionException("Los datos de la localidad son obligatorios");
        }
        if (request.nombre() == null || request.nombre().isBlank()) {
            throw new ValidacionException("El nombre de la localidad es obligatorio");
        }
        if (request.nombre().length() > LONGITUD_MAXIMA_NOMBRE) {
            throw new ValidacionException(
                    "El nombre de la localidad no puede superar los " + LONGITUD_MAXIMA_NOMBRE + " caracteres"
            );
        }
        if (request.codigoPostal() == null || request.codigoPostal().isBlank()) {
            throw new ValidacionException("El código postal es obligatorio");
        }
        if (request.codigoPostal().length() > LONGITUD_MAXIMA_CODIGO_POSTAL) {
            throw new ValidacionException(
                    "El código postal no puede superar los " + LONGITUD_MAXIMA_CODIGO_POSTAL + " caracteres"
            );
        }
        if (request.departamentoId() == null || request.departamentoId().isBlank()) {
            throw new ValidacionException("El id del departamento es obligatorio");
        }
    }
}

