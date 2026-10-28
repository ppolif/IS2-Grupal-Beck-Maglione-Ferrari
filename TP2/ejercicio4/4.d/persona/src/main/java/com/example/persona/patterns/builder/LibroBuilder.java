package com.example.persona.patterns.builder;

import com.example.persona.entities.Autor;
import com.example.persona.entities.Libro;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LibroBuilder {
    protected Long id;
    protected String titulo;
    protected int fecha;
    protected String genero;
    protected int paginas;
    protected LocalDate fechaVencimientoDevolucion;
    protected List<Autor> autores = new ArrayList<>();

    public LibroBuilder() {
    }

    public LibroBuilder id(Long id) {
        this.id = id;
        return this;
    }

    public LibroBuilder titulo(String titulo) {
        this.titulo = titulo;
        return this;
    }

    public LibroBuilder fecha(int fecha) {
        this.fecha = fecha;
        return this;
    }

    public LibroBuilder genero(String genero) {
        this.genero = genero;
        return this;
    }

    public LibroBuilder paginas(int paginas) {
        this.paginas = paginas;
        return this;
    }

    public LibroBuilder fechaVencimientoDevolucion(LocalDate fechaVencimientoDevolucion) {
        this.fechaVencimientoDevolucion = fechaVencimientoDevolucion;
        return this;
    }

    public LibroBuilder autores(List<Autor> autores) {
        this.autores = autores != null ? new ArrayList<>(autores) : new ArrayList<>();
        return this;
    }

    public LibroBuilder autor(Autor autor) {
        if (this.autores == null) {
            this.autores = new ArrayList<>();
        }
        if (autor != null) {
            this.autores.add(autor);
        }
        return this;
    }

    public LibroBuilder reset() {
        this.id = null;
        this.titulo = null;
        this.fecha = 0;
        this.genero = null;
        this.paginas = 0;
        this.fechaVencimientoDevolucion = null;
        this.autores = new ArrayList<>();
        return this;
    }

    public Libro build() {
        Libro libro = new Libro();
        libro.setId(this.id);
        libro.setTitulo(this.titulo);
        libro.setFecha(this.fecha);
        libro.setGenero(this.genero);
        libro.setPaginas(this.paginas);
        libro.setFechaVencimientoDevolucion(this.fechaVencimientoDevolucion);
        libro.setAutores(this.autores != null ? new ArrayList<>(this.autores) : new ArrayList<>());
        return libro;
    }
}

