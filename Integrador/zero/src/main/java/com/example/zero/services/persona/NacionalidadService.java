package com.example.zero.services.persona;

import com.example.zero.entidades.persona.Nacionalidad;
import com.example.zero.repositories.NacionalidadRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class NacionalidadService {

    private final NacionalidadRepository nacionalidadRepository;

    public NacionalidadService(NacionalidadRepository nacionalidadRepository) {
        this.nacionalidadRepository = nacionalidadRepository;
    }

    @Transactional(readOnly = true)
    public Nacionalidad buscarPorId(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("El identificador de nacionalidad no puede estar vacío");
        }
        return nacionalidadRepository.findActive(id.trim())
                .orElseThrow(() -> new IllegalArgumentException("La nacionalidad seleccionada no es válida"));
    }

    @Transactional(readOnly = true)
    public Optional<Nacionalidad> buscarPorNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return Optional.empty();
        }
        return nacionalidadRepository.findByNombreAndEliminadoFalse(nombre.trim());
    }

    @Transactional(readOnly = true)
    public List<Nacionalidad> listarActivas() {
        return nacionalidadRepository.findByEliminadoFalse();
    }
}
