package com.example.tinder.dto;

import com.example.tinder.enumeraciones.Sexo;
import com.example.tinder.enumeraciones.Tipo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class MascotaRequestDto {

    @NotBlank(message = "El nombre de la mascota es obligatorio")
    private String nombre;

    @NotNull(message = "El sexo es obligatorio")
    private Sexo sexo;

    @NotNull(message = "El tipo es obligatorio")
    private Tipo tipo;

    private String idUsuario;

    public MascotaRequestDto() {
    }

    public MascotaRequestDto(String nombre, Sexo sexo, Tipo tipo, String idUsuario) {
        this.nombre = nombre;
        this.sexo = sexo;
        this.tipo = tipo;
        this.idUsuario = idUsuario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Sexo getSexo() {
        return sexo;
    }

    public void setSexo(Sexo sexo) {
        this.sexo = sexo;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public void setTipo(Tipo tipo) {
        this.tipo = tipo;
    }

    public String getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }
}

