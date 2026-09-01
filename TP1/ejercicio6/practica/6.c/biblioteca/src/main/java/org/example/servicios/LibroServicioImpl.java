package org.example.servicios;


import org.example.dtos.*;
import org.example.dtos.*;
import org.example.entidades.Autor;
import org.example.entidades.Editorial;
import org.example.entidades.Imagen;
import org.example.entidades.Libro;
import org.example.errores.RecursoNoEncontradoException;
import org.example.repositorios.AutorRepositorio;
import org.example.repositorios.EditorialRepositorio;
import org.example.repositorios.ImagenRepositorio;
import org.example.repositorios.LibroRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LibroServicioImpl implements LibroServicio {

    private final LibroRepositorio libroRepositorio;
    private final AutorRepositorio autorRepositorio;
    private final EditorialRepositorio editorialRepositorio;
    private final ImagenRepositorio imagenRepositorio;

    public LibroServicioImpl(LibroRepositorio libroRepositorio, AutorRepositorio autorRepositorio,
                             EditorialRepositorio editorialRepositorio, ImagenRepositorio imagenRepositorio) {
        this.libroRepositorio = libroRepositorio;
        this.autorRepositorio = autorRepositorio;
        this.editorialRepositorio = editorialRepositorio;
        this.imagenRepositorio = imagenRepositorio;
    }

    @Override
    @Transactional
    public LibroResponseDTO crear(LibroRequestDTO dto) {
        if (libroRepositorio.existsById(dto.getIsbn())) {
            throw new IllegalArgumentException("Ya existe un libro con el ISBN: " + dto.getIsbn());
        }

        Editorial editorial = editorialRepositorio.findByIdAndAltaTrue(dto.getIdEditorial())
                .orElseThrow(() -> new RecursoNoEncontradoException("Editorial no encontrada o inactiva"));

        List<Autor> autores = autorRepositorio.findAllById(dto.getIdAutores());
        if (autores.isEmpty() || autores.size() != dto.getIdAutores().size()) {
            throw new RecursoNoEncontradoException("Uno o más autores no fueron encontrados");
        }

        Imagen imagen = null;
        if (dto.getIdImagen() != null && !dto.getIdImagen().isEmpty()) {
            imagen = imagenRepositorio.findById(dto.getIdImagen())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Imagen no encontrada"));
        }

        // Lógica de negocio de ejemplares
        Libro libro = Libro.builder()
                .isbn(dto.getIsbn())
                .titulo(dto.getTitulo())
                .anio(dto.getAnio())
                .ejemplares(dto.getEjemplares())
                .ejemplaresPrestados(0) // Inicializa en 0
                .ejemplaresRestantes(dto.getEjemplares()) // Iguales al total inicial
                .alta(true)
                .editorial(editorial)
                .autores(autores)
                .imagen(imagen)
                .build();

        libro = libroRepositorio.save(libro);
        return mapearADTO(libro);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LibroResponseDTO> obtenerTodosActivos() {
        return libroRepositorio.findByAltaTrue().stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public LibroResponseDTO obtenerPorIsbn(Long isbn) {
        return mapearADTO(buscarLibroActivo(isbn));
    }

    @Override
    @Transactional
    public LibroResponseDTO actualizar(Long isbn, LibroRequestDTO dto) {
        Libro libro = buscarLibroActivo(isbn);

        Editorial editorial = editorialRepositorio.findByIdAndAltaTrue(dto.getIdEditorial())
                .orElseThrow(() -> new RecursoNoEncontradoException("Editorial no encontrada o inactiva"));

        List<Autor> autores = autorRepositorio.findAllById(dto.getIdAutores());

        Imagen imagen = null;
        if (dto.getIdImagen() != null && !dto.getIdImagen().isEmpty()) {
            imagen = imagenRepositorio.findById(dto.getIdImagen())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Imagen no encontrada"));
        }

        // Si cambia la cantidad de ejemplares totales, recalcular los restantes
        int diferenciaEjemplares = dto.getEjemplares() - libro.getEjemplares();
        int nuevosRestantes = libro.getEjemplaresRestantes() + diferenciaEjemplares;

        if (nuevosRestantes < 0) {
            throw new IllegalArgumentException("No se pueden reducir los ejemplares, hay más libros prestados que la nueva cantidad de ejemplares totales");
        }

        libro.setTitulo(dto.getTitulo());
        libro.setAnio(dto.getAnio());
        libro.setEjemplares(dto.getEjemplares());
        libro.setEjemplaresRestantes(nuevosRestantes);
        libro.setEditorial(editorial);
        libro.setAutores(autores);
        libro.setImagen(imagen);

        libro = libroRepositorio.save(libro);
        return mapearADTO(libro);
    }

    @Override
    @Transactional
    public void darDeBaja(Long isbn) {
        Libro libro = buscarLibroActivo(isbn);
        libro.setAlta(false);
        libroRepositorio.save(libro);
    }

    // --- Métodos Privados Utilitarios ---

    private Libro buscarLibroActivo(Long isbn) {
        return libroRepositorio.findByIsbnAndAltaTrue(isbn)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró un libro activo con el ISBN: " + isbn));
    }

    private LibroResponseDTO mapearADTO(Libro libro) {
        // Mapeo manual
        EditorialResponseDTO editorialDTO = EditorialResponseDTO.builder()
                .id(libro.getEditorial().getId())
                .nombre(libro.getEditorial().getNombre())
                .alta(libro.getEditorial().isAlta())
                .build();

        List<AutorResponseDTO> autoresDTO = libro.getAutores().stream().map(a ->
                AutorResponseDTO.builder().id(a.getId()).nombre(a.getNombre()).alta(a.isAlta()).build()
        ).collect(Collectors.toList());

        ImagenResponseDTO imagenDTO = null;
        if (libro.getImagen() != null) {
            imagenDTO = ImagenResponseDTO.builder()
                    .id(libro.getImagen().getId())
                    .nombre(libro.getImagen().getNombre())
                    .mime(libro.getImagen().getMime())
                    .build();
        }

        return LibroResponseDTO.builder()
                .isbn(libro.getIsbn())
                .titulo(libro.getTitulo())
                .anio(libro.getAnio())
                .ejemplares(libro.getEjemplares())
                .ejemplaresPrestados(libro.getEjemplaresPrestados())
                .ejemplaresRestantes(libro.getEjemplaresRestantes())
                .alta(libro.isAlta())
                .editorial(editorialDTO)
                .autores(autoresDTO)
                .imagen(imagenDTO)
                .build();
    }
}
