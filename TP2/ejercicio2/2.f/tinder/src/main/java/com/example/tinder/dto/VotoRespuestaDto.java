package com.example.tinder.dto;

import jakarta.validation.constraints.NotBlank;

public class VotoRespuestaDto {

    @NotBlank(message = "El idUsuario es obligatorio")
    private String idUsuario;

    @NotBlank(message = "El idVoto es obligatorio")
    private String idVoto;

    public VotoRespuestaDto() {
    }

    public VotoRespuestaDto(String idUsuario, String idVoto) {
        this.idUsuario = idUsuario;
        this.idVoto = idVoto;
    }

    public String getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getIdVoto() {
        return idVoto;
    }

    public void setIdVoto(String idVoto) {
        this.idVoto = idVoto;
    }
}

