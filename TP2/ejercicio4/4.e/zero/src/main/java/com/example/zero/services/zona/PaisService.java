package com.example.zero.services.zona;

import com.example.zero.dto.zona.PaisDTO;
import com.example.zero.entidades.zona.Pais;
import com.example.zero.repositories.PaisRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PaisService {
    @Autowired private PaisRepository paisRepository;

    @Transactional
    public void crearPais(PaisDTO dto) {
        validar(dto.getNombre());
        Pais pais = new Pais();
        pais.setNombre(dto.getNombre());
        pais.setEliminado(false);
        paisRepository.save(pais);
    }

    public void validar(String nombre) {
        if (nombre == null || nombre.isEmpty()) throw new IllegalArgumentException("Nombre inválido");
    }

    @Transactional(readOnly = true)
    public PaisDTO buscarPais(String id) {
        Pais pais = paisRepository.findById(id).orElseThrow();
        return mapearADTO(pais);
    }

    @Transactional(readOnly = true)
    public PaisDTO buscarPaisPorNombre(String nombre) {
        return mapearADTO(paisRepository.findByNombre(nombre));
    }

    @Transactional
    public void modificarPais(String id, PaisDTO dto) {
        validar(dto.getNombre());
        Pais pais = paisRepository.findById(id).orElseThrow();
        pais.setNombre(dto.getNombre());
        paisRepository.save(pais);
    }

    @Transactional
    public void eliminarPais(String id) {
        Pais pais = paisRepository.findById(id).orElseThrow();
        pais.setEliminado(true); // Baja lógica según UML
        paisRepository.save(pais);
    }

    @Transactional(readOnly = true)
    public List<PaisDTO> listarPaisActivo() {
        return paisRepository.findAll().stream()
                .filter(p -> !p.isEliminado())
                .map(this::mapearADTO)
                .collect(Collectors.toList());
    }

    private PaisDTO mapearADTO(Pais pais) {
        PaisDTO dto = new PaisDTO();
        dto.setId(pais.getId());
        dto.setNombre(pais.getNombre());
        return dto;
    }
}


