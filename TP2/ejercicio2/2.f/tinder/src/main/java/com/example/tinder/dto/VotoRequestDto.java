package com.example.tinder.dto;

import jakarta.validation.constraints.NotBlank;

public class VotoRequestDto {

    @NotBlank(message = "El idUsuario es obligatorio")
    private String idUsuario;

    @NotBlank(message = "El idMascota1 es obligatorio")
    private String idMascota1;

    @NotBlank(message = "El idMascota2 es obligatorio")
    private String idMascota2;

    public VotoRequestDto() {
    }

    public VotoRequestDto(String idUsuario, String idMascota1, String idMascota2) {
        this.idUsuario = idUsuario;
        this.idMascota1 = idMascota1;
        this.idMascota2 = idMascota2;
    }

    public String getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getIdMascota1() {
        return idMascota1;
    }

    public void setIdMascota1(String idMascota1) {
        this.idMascota1 = idMascota1;
    }

    public String getIdMascota2() {
        return idMascota2;
    }

    public void setIdMascota2(String idMascota2) {
        this.idMascota2 = idMascota2;
    }
}

