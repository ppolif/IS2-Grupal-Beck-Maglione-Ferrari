package com.example.persona.services;

import com.example.persona.dtos.LocalidadDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface LocalidadService extends BaseService<LocalidadDto, Long> {

    List<LocalidadDto> search(String filtro) throws Exception;
    Page<LocalidadDto> search(String filtro, Pageable pageable) throws Exception;
}
