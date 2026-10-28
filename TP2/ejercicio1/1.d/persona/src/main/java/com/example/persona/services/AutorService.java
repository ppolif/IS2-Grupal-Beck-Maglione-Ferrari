package com.example.persona.services;

import com.example.persona.dtos.AutorDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AutorService extends BaseService<AutorDto, Long> {

    List<AutorDto> search(String filtro) throws Exception;
    Page<AutorDto> search(String filtro, Pageable pageable) throws Exception;
}
