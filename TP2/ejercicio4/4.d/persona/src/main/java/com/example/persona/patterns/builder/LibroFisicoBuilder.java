package com.example.persona.patterns.builder;

import com.example.persona.entities.Autor;
import com.example.persona.entities.LibroFisico;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LibroFisicoBuilder extends LibroBuilder {
    private String numeroEstante;
    private Integer pesoGramos;

    public LibroFisicoBuilder numeroEstante(String numeroEstante) {
        this.numeroEstante = numeroEstante;
        return this;
    }

    public LibroFisicoBuilder pesoGramos(Integer pesoGramos) {
        this.pesoGramos = pesoGramos;
        return this;
    }

    @Override
    public LibroFisicoBuilder id(Long id) {
        super.id(id);
        return this;
    }

    @Override
    public LibroFisicoBuilder titulo(String titulo) {
        super.titulo(titulo);
        return this;
    }

    @Override
    public LibroFisicoBuilder fecha(int fecha) {
        super.fecha(fecha);
        return this;
    }

    @Override
    public LibroFisicoBuilder genero(String genero) {
        super.genero(genero);
        return this;
    }

    @Override
    public LibroFisicoBuilder paginas(int paginas) {
        super.paginas(paginas);
        return this;
    }

    @Override
    public LibroFisicoBuilder fechaVencimientoDevolucion(LocalDate fechaVencimientoDevolucion) {
        super.fechaVencimientoDevolucion(fechaVencimientoDevolucion);
        return this;
    }

    @Override
    public LibroFisicoBuilder autores(List<Autor> autores) {
        super.autores(autores);
        return this;
    }

    @Override
    public LibroFisicoBuilder autor(Autor autor) {
        super.autor(autor);
        return this;
    }

    @Override
    public LibroFisicoBuilder reset() {
        super.reset();
        this.numeroEstante = null;
        this.pesoGramos = null;
        return this;
    }

    @Override
    public LibroFisico build() {
        LibroFisico libro = new LibroFisico();
        libro.setId(this.id);
        libro.setTitulo(this.titulo);
        libro.setFecha(this.fecha);
        libro.setGenero(this.genero);
        libro.setPaginas(this.paginas);
        libro.setFechaVencimientoDevolucion(this.fechaVencimientoDevolucion);
        libro.setAutores(this.autores != null ? new ArrayList<>(this.autores) : new ArrayList<>());
        libro.setNumeroEstante(this.numeroEstante);
        libro.setPesoGramos(this.pesoGramos);
        return libro;
    }
}

