package com.example.tinder.dto;

import jakarta.validation.constraints.NotBlank;

public class ZonaRequestDto {

    @NotBlank(message = "El nombre de la zona es obligatorio")
    private String nombre;

    private String descripcion;

    public ZonaRequestDto() {
    }

    public ZonaRequestDto(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}

