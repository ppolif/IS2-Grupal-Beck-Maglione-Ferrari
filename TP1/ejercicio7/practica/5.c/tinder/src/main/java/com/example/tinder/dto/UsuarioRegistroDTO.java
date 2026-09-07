package com.example.tinder.dto;

import org.springframework.web.multipart.MultipartFile;

public class UsuarioRegistroDTO {
    private String nombre;
    private String apellido;
    private String email;
    private String clave;
    private String repetirClave;
    private String idZona;
    private MultipartFile archivo;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    public String getRepetirClave() {
        return repetirClave;
    }

    public void setRepetirClave(String repetirClave) {
        this.repetirClave = repetirClave;
    }

    public String getIdZona() {
        return idZona;
    }

    public void setIdZona(String idZona) {
        this.idZona = idZona;
    }

    public MultipartFile getArchivo() {
        return archivo;
    }

    public void setArchivo(MultipartFile archivo) {
        this.archivo = archivo;
    }
}
