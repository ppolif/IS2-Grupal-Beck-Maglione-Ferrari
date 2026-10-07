package com.ejercicio_1_f.demo;

import com.ejercicio_1_f.demo.dao.CountryDao;
import com.ejercicio_1_f.demo.dtos.CountryDto;
import com.ejercicio_1_f.demo.mappers.CountryMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j //generar los getters y setters de lombok en tiempo de compilacion
public class CountryService {

    public CountryDto readById(UUID id) {

        if (CountryDao.db.containsKey(id)) {
            return CountryMapper.mapper.toCountryDto(CountryDao.db.get(id));
        } else {
            log.error("Country with id {} not found", id);
            throw  new RuntimeException("Country with id: " + id  + " not found");
        }
    }
}
