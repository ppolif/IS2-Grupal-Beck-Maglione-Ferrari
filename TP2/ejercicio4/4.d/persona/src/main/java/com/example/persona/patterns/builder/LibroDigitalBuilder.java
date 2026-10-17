package com.example.persona.patterns.builder;

import com.example.persona.entities.Autor;
import com.example.persona.entities.LibroDigital;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LibroDigitalBuilder extends LibroBuilder {
    private Double tamanoMB;
    private String formato;
    private String urlDescarga;

    public LibroDigitalBuilder tamanoMB(Double tamanoMB) {
        this.tamanoMB = tamanoMB;
        return this;
    }

    public LibroDigitalBuilder formato(String formato) {
        this.formato = formato;
        return this;
    }

    public LibroDigitalBuilder urlDescarga(String urlDescarga) {
        this.urlDescarga = urlDescarga;
        return this;
    }

    @Override
    public LibroDigitalBuilder id(Long id) {
        super.id(id);
        return this;
    }

    @Override
    public LibroDigitalBuilder titulo(String titulo) {
        super.titulo(titulo);
        return this;
    }

    @Override
    public LibroDigitalBuilder fecha(int fecha) {
        super.fecha(fecha);
        return this;
    }

    @Override
    public LibroDigitalBuilder genero(String genero) {
        super.genero(genero);
        return this;
    }

    @Override
    public LibroDigitalBuilder paginas(int paginas) {
        super.paginas(paginas);
        return this;
    }

    @Override
    public LibroDigitalBuilder fechaVencimientoDevolucion(LocalDate fechaVencimientoDevolucion) {
        super.fechaVencimientoDevolucion(fechaVencimientoDevolucion);
        return this;
    }

    @Override
    public LibroDigitalBuilder autores(List<Autor> autores) {
        super.autores(autores);
        return this;
    }

    @Override
    public LibroDigitalBuilder autor(Autor autor) {
        super.autor(autor);
        return this;
    }

    @Override
    public LibroDigitalBuilder reset() {
        super.reset();
        this.tamanoMB = null;
        this.formato = null;
        this.urlDescarga = null;
        return this;
    }

    @Override
    public LibroDigital build() {
        LibroDigital libro = new LibroDigital();
        libro.setId(this.id);
        libro.setTitulo(this.titulo);
        libro.setFecha(this.fecha);
        libro.setGenero(this.genero);
        libro.setPaginas(this.paginas);
        libro.setFechaVencimientoDevolucion(this.fechaVencimientoDevolucion);
        libro.setAutores(this.autores != null ? new ArrayList<>(this.autores) : new ArrayList<>());
        libro.setTamanoMB(this.tamanoMB);
        libro.setFormato(this.formato);
        libro.setUrlDescarga(this.urlDescarga);
        return libro;
    }
}

