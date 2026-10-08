package com.example.tinder.dto;

public class FotoDto {
    private String id;
    private String nombre;
    private String mime;

    public FotoDto() {
    }

    public FotoDto(String id, String nombre, String mime) {
        this.id = id;
        this.nombre = nombre;
        this.mime = mime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getMime() {
        return mime;
    }

    public void setMime(String mime) {
        this.mime = mime;
    }
}

