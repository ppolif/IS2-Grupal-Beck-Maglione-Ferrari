package com.example.persona.services;

import com.example.persona.dtos.LibroDto;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface LibroService extends BaseService<LibroDto, Long> {
    List<LibroDto> search(String filtro) throws Exception;
    Page<LibroDto> search(String filtro, Pageable pageable) throws Exception;
    LibroDto saveConArchivo(LibroDto dto, MultipartFile file) throws Exception;
    LibroDto updateConArchivo(Long id, LibroDto dto, MultipartFile file) throws Exception;
    Resource getPdfAsResource(Long id) throws Exception;
}
