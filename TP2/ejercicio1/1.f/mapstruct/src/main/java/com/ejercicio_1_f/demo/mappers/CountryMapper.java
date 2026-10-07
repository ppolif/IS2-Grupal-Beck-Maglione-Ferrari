package com.ejercicio_1_f.demo.mappers;

import com.ejercicio_1_f.demo.dtos.CountryDto;
import com.ejercicio_1_f.demo.dtos.LanguageDto;
import com.ejercicio_1_f.demo.models.Country;
import com.ejercicio_1_f.demo.models.Language;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface CountryMapper {

    CountryMapper mapper = Mappers.getMapper(CountryMapper.class);

    @Mapping(target = "continent", source = "location.continent")
    CountryDto toCountryDto(Country country);

    @Mapping(target = "isOfficialLanguage", source = "isOfficial")
    @Mapping(target = "speakersTotal", source = "speakersCount")
    LanguageDto toLanguageDto(Language language);

}
