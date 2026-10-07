package com.example.persona.services;

import com.example.persona.dtos.LocalidadDto;
import com.example.persona.entities.Localidad;
import com.example.persona.repositories.BaseRepository;
import com.example.persona.repositories.LocalidadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LocalidadServiceImpl extends BaseServiceImpl<Localidad, LocalidadDto, Long> implements LocalidadService {

    @Autowired
    private LocalidadRepository localidadRepository;

    public LocalidadServiceImpl(BaseRepository<Localidad, Long> baseRepository) {
        super(baseRepository, Localidad.class, LocalidadDto.class);
    }

    @Override
    public List<LocalidadDto> search(String filtro) throws Exception {
        try {
            List<Localidad> localidades = localidadRepository.searchNativo(filtro);
            return localidades.stream().map(this::toDto).toList();
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public Page<LocalidadDto> search(String filtro, Pageable pageable) throws Exception {
        try {
            Page<Localidad> localidades = localidadRepository.searchNativo(filtro, pageable);
            return localidades.map(this::toDto);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }
}
