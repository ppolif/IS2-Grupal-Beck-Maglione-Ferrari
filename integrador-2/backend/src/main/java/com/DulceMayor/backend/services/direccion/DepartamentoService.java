package com.DulceMayor.backend.services.direccion;

import com.DulceMayor.backend.config.ConflictoException;
import com.DulceMayor.backend.config.ValidacionException;
import com.DulceMayor.backend.dtos.BaseMapper;
import com.DulceMayor.backend.dtos.direccion.DepartamentoMapper;
import com.DulceMayor.backend.dtos.direccion.DepartamentoRequest;
import com.DulceMayor.backend.dtos.direccion.DepartamentoResponse;
import com.DulceMayor.backend.entities.direccion.Departamento;
import com.DulceMayor.backend.entities.direccion.Provincia;
import com.DulceMayor.backend.repositories.BaseRepository;
import com.DulceMayor.backend.repositories.direccion.DepartamentoRepository;
import java.util.List;

import com.DulceMayor.backend.services.BaseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de negocio para la gestión de departamentos del diccionario geográfico.
 * Aplica el patrón Template Method heredando de {@link BaseService}.
 */
@Service
public class DepartamentoService extends BaseService<Departamento, DepartamentoRequest, DepartamentoResponse> {

    private static final int LONGITUD_MAXIMA_NOMBRE = 100;

    private final DepartamentoRepository departamentoRepository;
    private final DepartamentoMapper departamentoMapper;
    private final ProvinciaService provinciaService;

    public DepartamentoService(
            DepartamentoRepository departamentoRepository,
            DepartamentoMapper departamentoMapper,
            ProvinciaService provinciaService) {
        this.departamentoRepository = departamentoRepository;
        this.departamentoMapper = departamentoMapper;
        this.provinciaService = provinciaService;
    }

    @Override
    protected BaseRepository<Departamento> getRepository() {
        return departamentoRepository;
    }

    @Override
    protected BaseMapper<Departamento, DepartamentoRequest, DepartamentoResponse> getMapper() {
        return departamentoMapper;
    }

    @Override
    protected String getNombreRecurso() {
        return "Departamento";
    }

    @Override
    protected DepartamentoRequest prepararRequest(DepartamentoRequest request) {
        if (request == null) {
            return null;
        }
        String nombreLimpio = request.nombre() != null ? request.nombre().trim() : null;
        return new DepartamentoRequest(nombreLimpio, request.provinciaId());
    }

    @Override
    protected void validarCrear(DepartamentoRequest request) {
        validarFormato(request);
        provinciaService.obtenerEntidad(request.provinciaId());
        if (departamentoRepository.existsByNombreIgnoreCaseAndProvinciaIdAndEliminadoFalse(
                request.nombre(), request.provinciaId())) {
            throw new ConflictoException(
                    "DUPLICADO",
                    "Ya existe un departamento con el nombre '" + request.nombre() + "' en la provincia indicada"
            );
        }
    }

    @Override
    protected void validarModificar(String id, DepartamentoRequest request, Departamento existente) {
        validarFormato(request);
        provinciaService.obtenerEntidad(request.provinciaId());
        if (departamentoRepository.existsByNombreIgnoreCaseAndProvinciaIdAndIdNotAndEliminadoFalse(
                request.nombre(), request.provinciaId(), id)) {
            throw new ConflictoException(
                    "DUPLICADO",
                    "Ya existe otro departamento con el nombre '" + request.nombre() + "' en la provincia indicada"
            );
        }
    }

    @Override
    protected void antesDePersistir(Departamento entidad, DepartamentoRequest request) {
        Provincia provincia = provinciaService.obtenerEntidad(request.provinciaId());
        entidad.setProvincia(provincia);
    }

    @Override
    protected List<Departamento> buscarEntidadesActivas() {
        return departamentoRepository.findAllByEliminadoFalseOrderByNombreAsc();
    }

    @Transactional(readOnly = true)
    public List<DepartamentoResponse> listar(String provinciaId) {
        if (provinciaId == null || provinciaId.isBlank()) {
            return listar();
        }
        List<Departamento> entidades =
                departamentoRepository.findAllByProvinciaIdAndEliminadoFalseOrderByNombreAsc(provinciaId);
        return departamentoMapper.aResponseList(entidades);
    }

    private void validarFormato(DepartamentoRequest request) {
        if (request == null) {
            throw new ValidacionException("Los datos del departamento son obligatorios");
        }
        if (request.nombre() == null || request.nombre().isBlank()) {
            throw new ValidacionException("El nombre del departamento es obligatorio");
        }
        if (request.nombre().length() > LONGITUD_MAXIMA_NOMBRE) {
            throw new ValidacionException(
                    "El nombre del departamento no puede superar los " + LONGITUD_MAXIMA_NOMBRE + " caracteres"
            );
        }
        if (request.provinciaId() == null || request.provinciaId().isBlank()) {
            throw new ValidacionException("El id de la provincia es obligatorio");
        }
    }
}

