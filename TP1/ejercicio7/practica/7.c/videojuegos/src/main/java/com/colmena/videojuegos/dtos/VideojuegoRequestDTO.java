package com.colmena.videojuegos.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;

public class VideojuegoRequestDTO {

    private Long id;

    @NotBlank(message = "El título no puede estar vacío")
    @Size(min = 2, max = 100, message = "El título debe tener entre 2 y 100 caracteres")
    private String titulo;

    @Size(max = 1000, message = "La descripción no puede superar los 1000 caracteres")
    private String descripcion;

    @NotNull(message = "El precio es obligatorio")
    @Min(value = 0, message = "El precio no puede ser negativo")
    private Float precio;

    @NotNull(message = "El stock es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    private short stock;

    @NotNull(message = "No puede ser nulo la fecha")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date fechaLanzamiento;

    @NotNull(message = "Debe seleccionar una categoría")
    private Long idCategoria;

    @NotNull(message = "Debe seleccionar un estudio")
    private Long idEstudio;

    private MultipartFile imagen;

    public VideojuegoRequestDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Float getPrecio() {
        return precio;
    }

    public void setPrecio(Float precio) {
        this.precio = precio;
    }

    public short getStock() {
        return stock;
    }

    public void setStock(short stock) {
        this.stock = stock;
    }

    public Date getFechaLanzamiento() {
        return fechaLanzamiento;
    }

    public void setFechaLanzamiento(Date fechaLanzamiento) {
        this.fechaLanzamiento = fechaLanzamiento;
    }

    public Long getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(Long idCategoria) {
        this.idCategoria = idCategoria;
    }

    public Long getIdEstudio() {
        return idEstudio;
    }

    public void setIdEstudio(Long idEstudio) {
        this.idEstudio = idEstudio;
    }

    public MultipartFile getImagen() {
        return imagen;
    }

    public void setImagen(MultipartFile imagen) {
        this.imagen = imagen;
    }
}