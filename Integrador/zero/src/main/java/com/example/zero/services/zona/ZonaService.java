package com.example.zero.services.zona;

import com.example.zero.dto.zona.DepartamentoDTO;
import com.example.zero.dto.zona.LocalidadDTO;
import com.example.zero.dto.zona.PaisDTO;
import com.example.zero.dto.zona.ProvinciaDTO;
import com.example.zero.entidades.zona.*;
import com.example.zero.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ZonaService {

    private final PaisRepository paisRepository;
    private final ProvinciaRepository provinciaRepository;
    private final DepartamentoRepository departamentoRepository;
    private final LocalidadRepository localidadRepository;
    private final DireccionRepository direccionRepository;

    public ZonaService(PaisRepository paisRepository,
                       ProvinciaRepository provinciaRepository,
                       DepartamentoRepository departamentoRepository,
                       LocalidadRepository localidadRepository,
                       DireccionRepository direccionRepository) {
        this.paisRepository = paisRepository;
        this.provinciaRepository = provinciaRepository;
        this.departamentoRepository = departamentoRepository;
        this.localidadRepository = localidadRepository;
        this.direccionRepository = direccionRepository;
    }

    @Transactional
    public Direccion crearDireccion(String calle, String numeracion, String barrio,
                                    String manzanaPiso, String casaDepartamento,
                                    String referencia, String localidadId) {

        if (localidadId == null || localidadId.trim().isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar una localidad para el domicilio");
        }
        if (calle == null || calle.trim().isEmpty()) {
            throw new IllegalArgumentException("La calle de la dirección no puede estar vacía");
        }
        if (numeracion == null || numeracion.trim().isEmpty()) {
            throw new IllegalArgumentException("La numeración de la dirección no puede estar vacía");
        }

        Localidad localidad = buscarLocalidadPorId(localidadId.trim());
        Direccion direccion = Direccion.builder()
                .calle(calle.trim())
                .numeracion(numeracion.trim())
                .barrio(barrio != null && !barrio.isBlank() ? barrio.trim() : null)
                .manzanaPiso(manzanaPiso != null && !manzanaPiso.isBlank() ? manzanaPiso.trim() : null)
                .casaDepartamento(casaDepartamento != null && !casaDepartamento.isBlank() ? casaDepartamento.trim() : null)
                .referencia(referencia != null && !referencia.isBlank() ? referencia.trim() : null)
                .localidad(localidad)
                .eliminado(false)
                .build();
        return direccionRepository.save(direccion);
    }

    @Transactional(readOnly = true)
    public List<PaisDTO> listarPaisesActivos() {
        return paisRepository.findByEliminadoFalse().stream()
                .map(this::mapearPaisADTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProvinciaDTO> listarTodasProvinciasActivas() {
        return provinciaRepository.findByEliminadoFalse().stream()
                .map(this::mapearProvinciaADTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProvinciaDTO> listarProvinciasPorPais(String paisId) {
        if (paisId == null || paisId.trim().isEmpty()) {
            return List.of();
        }
        return provinciaRepository.findByPaisIdAndEliminadoFalse(paisId.trim()).stream()
                .map(this::mapearProvinciaADTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DepartamentoDTO> listarTodosDepartamentosActivos() {
        return departamentoRepository.findByEliminadoFalse().stream()
                .map(this::mapearDepartamentoADTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DepartamentoDTO> listarDepartamentosPorProvincia(String provinciaId) {
        if (provinciaId == null || provinciaId.trim().isEmpty()) {
            return List.of();
        }
        return departamentoRepository.findByProvinciaIdAndEliminadoFalse(provinciaId.trim()).stream()
                .map(this::mapearDepartamentoADTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LocalidadDTO> listarTodasLocalidadesActivas() {
        return localidadRepository.findByEliminadoFalse().stream()
                .map(this::mapearLocalidadADTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LocalidadDTO> listarLocalidadesPorDepartamento(String departamentoId) {
        if (departamentoId == null || departamentoId.trim().isEmpty()) {
            return List.of();
        }
        return localidadRepository.findByDepartamentoIdAndEliminadoFalse(departamentoId.trim()).stream()
                .map(this::mapearLocalidadADTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Localidad buscarLocalidadPorId(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("El identificador de localidad no puede estar vacío");
        }
        return localidadRepository.findActive(id.trim())
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la localidad seleccionada"));
    }

    private PaisDTO mapearPaisADTO(Pais pais) {
        return new PaisDTO(pais.getId(), pais.getNombre());
    }

    private ProvinciaDTO mapearProvinciaADTO(Provincia provincia) {
        String paisId = provincia.getPais() != null ? provincia.getPais().getId() : null;
        String paisNombre = provincia.getPais() != null ? provincia.getPais().getNombre() : null;
        return new ProvinciaDTO(provincia.getId(), provincia.getNombre(), paisId, paisNombre);
    }

    private DepartamentoDTO mapearDepartamentoADTO(Departamento departamento) {
        String provId = departamento.getProvincia() != null ? departamento.getProvincia().getId() : null;
        String provNombre = departamento.getProvincia() != null ? departamento.getProvincia().getNombre() : null;
        return new DepartamentoDTO(departamento.getId(), departamento.getNombre(), provId, provNombre);
    }

    private LocalidadDTO mapearLocalidadADTO(Localidad localidad) {
        String depId = localidad.getDepartamento() != null ? localidad.getDepartamento().getId() : null;
        String depNombre = localidad.getDepartamento() != null ? localidad.getDepartamento().getNombre() : null;
        return new LocalidadDTO(localidad.getId(), localidad.getNombre(), localidad.getCodigoPostal(), depId, depNombre);
    }
}

