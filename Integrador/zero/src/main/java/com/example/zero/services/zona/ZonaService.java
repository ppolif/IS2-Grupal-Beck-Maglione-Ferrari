package com.example.zero.services.zona;

import com.example.zero.dto.zona.DepartamentoDTO;
import com.example.zero.dto.zona.LocalidadDTO;
import com.example.zero.dto.zona.PaisDTO;
import com.example.zero.dto.zona.ProvinciaDTO;
import com.example.zero.entidades.zona.Departamento;
import com.example.zero.entidades.zona.Localidad;
import com.example.zero.entidades.zona.Pais;
import com.example.zero.entidades.zona.Provincia;
import com.example.zero.repositories.DepartamentoRepository;
import com.example.zero.repositories.LocalidadRepository;
import com.example.zero.repositories.PaisRepository;
import com.example.zero.repositories.ProvinciaRepository;
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

    public ZonaService(PaisRepository paisRepository,
                       ProvinciaRepository provinciaRepository,
                       DepartamentoRepository departamentoRepository,
                       LocalidadRepository localidadRepository) {
        this.paisRepository = paisRepository;
        this.provinciaRepository = provinciaRepository;
        this.departamentoRepository = departamentoRepository;
        this.localidadRepository = localidadRepository;
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

