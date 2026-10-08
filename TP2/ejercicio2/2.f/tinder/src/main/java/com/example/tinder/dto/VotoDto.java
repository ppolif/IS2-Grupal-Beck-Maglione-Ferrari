package com.example.tinder.dto;

import java.util.Date;

public class VotoDto {
    private String id;
    private Date fecha;
    private Date respuesta;
    private String mascota1Id;
    private String mascota1Nombre;
    private String mascota2Id;
    private String mascota2Nombre;

    public VotoDto() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public Date getRespuesta() {
        return respuesta;
    }

    public void setRespuesta(Date respuesta) {
        this.respuesta = respuesta;
    }

    public String getMascota1Id() {
        return mascota1Id;
    }

    public void setMascota1Id(String mascota1Id) {
        this.mascota1Id = mascota1Id;
    }

    public String getMascota1Nombre() {
        return mascota1Nombre;
    }

    public void setMascota1Nombre(String mascota1Nombre) {
        this.mascota1Nombre = mascota1Nombre;
    }

    public String getMascota2Id() {
        return mascota2Id;
    }

    public void setMascota2Id(String mascota2Id) {
        this.mascota2Id = mascota2Id;
    }

    public String getMascota2Nombre() {
        return mascota2Nombre;
    }

    public void setMascota2Nombre(String mascota2Nombre) {
        this.mascota2Nombre = mascota2Nombre;
    }
}

