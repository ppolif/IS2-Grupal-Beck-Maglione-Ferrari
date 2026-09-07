package com.example.tinder.dto;

import com.example.tinder.enumeraciones.Sexo;
import com.example.tinder.enumeraciones.Tipo;
import org.springframework.web.multipart.MultipartFile;

public class MascotaEdicionDTO {
    private String id;
    private String nombre;
    private Sexo sexo;
    private Tipo tipo;
    private MultipartFile archivo;

    // Booleano para que Thymeleaf sepa si dibujar o no la imagen actual
    private boolean tieneFoto;

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

    public MultipartFile getArchivo() {
        return archivo;
    }

    public void setArchivo(MultipartFile archivo) {
        this.archivo = archivo;
    }

    public boolean isTieneFoto() {
        return tieneFoto;
    }

    public void setTieneFoto(boolean tieneFoto) {
        this.tieneFoto = tieneFoto;
    }
}
