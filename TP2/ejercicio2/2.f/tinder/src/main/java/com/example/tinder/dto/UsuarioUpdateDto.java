package com.example.tinder.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UsuarioUpdateDto {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    private String apellido;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato de email no es válido")
    private String email;

    @NotBlank(message = "La clave es obligatoria")
    @Size(min = 6, message = "La clave debe tener al menos 6 caracteres")
    private String clave;

    @NotBlank(message = "Debe repetir la clave")
    private String repetirClave;

    @NotBlank(message = "Debe especificar el ID de la zona")
    private String idZona;

    public UsuarioUpdateDto() {
    }

    public UsuarioUpdateDto(String nombre, String apellido, String email, String clave, String repetirClave, String idZona) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.clave = clave;
        this.repetirClave = repetirClave;
        this.idZona = idZona;
    }

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
}

