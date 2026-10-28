package com.example.persona.services;

import com.example.persona.dtos.LibroDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface LibroService extends BaseService<LibroDto, Long> {
    List<LibroDto> search(String filtro) throws Exception;
    Page<LibroDto> search(String filtro, Pageable pageable) throws Exception;
    List<LibroDto> findDisponibles() throws Exception;
}
