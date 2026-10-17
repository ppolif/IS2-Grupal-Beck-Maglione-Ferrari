package com.example.persona.services;

import com.example.persona.dtos.AutorDto;
import com.example.persona.entities.Autor;
import com.example.persona.mappers.AutorMapper;
import com.example.persona.repositories.AutorRepository;
import com.example.persona.repositories.BaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AutorServiceImpl extends BaseServiceImpl<Autor, AutorDto, Long> implements AutorService {

    private final AutorRepository autorRepository;

    @Autowired
    public AutorServiceImpl(AutorRepository autorRepository, AutorMapper autorMapper) {
        super(autorRepository, autorMapper);
        this.autorRepository = autorRepository;
    }

    @Override
    @Transactional
    public AutorDto clonar(Long id) throws Exception {
        try {
            Autor autorOriginal = findEntityById(id);
            Autor autorClon = autorOriginal.clonar();
            Autor guardado = autorRepository.save(autorClon);
            return toDto(guardado);
        } catch (Exception e) {
            throw new Exception("Error al clonar el autor: " + e.getMessage());
        }
    }

    @Override
    public List<AutorDto> search(String filtro) throws Exception {
        try {
            List<Autor> autores = autorRepository.searchNativo(filtro);
            return autores.stream().map(this::toDto).toList();
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public Page<AutorDto> search(String filtro, Pageable pageable) throws Exception {
        try {
            Page<Autor> autores = autorRepository.searchNativo(filtro, pageable);
            return autores.map(this::toDto);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }
}
