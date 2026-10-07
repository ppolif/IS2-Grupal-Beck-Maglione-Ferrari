package com.ejercicio_1_f.demo.dtos;

import lombok.Data;

@Data
public class LanguageDto {
    private String name;
    private Boolean isOfficialLanguage;
    private Integer speakersTotal;
}
