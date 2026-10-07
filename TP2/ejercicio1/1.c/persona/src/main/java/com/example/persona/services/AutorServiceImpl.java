package com.example.persona.services;

import com.example.persona.dtos.AutorDto;
import com.example.persona.entities.Autor;
import com.example.persona.repositories.AutorRepository;
import com.example.persona.repositories.BaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AutorServiceImpl extends BaseServiceImpl<Autor, AutorDto, Long> implements AutorService {

    @Autowired
    private AutorRepository autorRepository;

    public AutorServiceImpl(BaseRepository<Autor, Long> baseRepository) {
        super(baseRepository, Autor.class, AutorDto.class);
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
