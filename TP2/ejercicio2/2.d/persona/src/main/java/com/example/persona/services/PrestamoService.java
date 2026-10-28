package com.example.persona.services;

import com.example.persona.dtos.AltaPrestamoDto;
import com.example.persona.dtos.PrestamoDto;

import java.util.List;

public interface PrestamoService extends BaseService<PrestamoDto, Long> {
    PrestamoDto darDeAlta(AltaPrestamoDto altaDto) throws Exception;
    PrestamoDto devolver(Long prestamoId) throws Exception;
    List<PrestamoDto> findActivos() throws Exception;
}

