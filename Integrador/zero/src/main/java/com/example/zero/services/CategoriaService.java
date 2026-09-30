package com.example.zero.services;

import com.example.zero.dto.producto.CategoriaDTO;
import com.example.zero.entidades.producto.Categoria;
import com.example.zero.entidades.producto.SubCategoria;
import com.example.zero.repositories.CategoriaRepository;
import com.example.zero.repositories.SubCategoriaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final SubCategoriaRepository subCategoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository, SubCategoriaRepository subCategoriaRepository) {
        this.categoriaRepository = categoriaRepository;
        this.subCategoriaRepository = subCategoriaRepository;
    }

    public void validar(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la categoría no puede estar vacío");
        }
    }

    @Transactional
    public Categoria crearCategoria(String nombre) {
        validar(nombre);
        String nombreLimpio = nombre.trim();

        Optional<Categoria> existente = categoriaRepository.findByNombreAndEliminadoFalse(nombreLimpio);
        if (existente.isPresent()) {
            throw new IllegalArgumentException("Ya existe una categoría activa con el nombre: " + nombreLimpio);
        }

        Categoria categoria = Categoria.builder()
                .nombre(nombreLimpio)
                .eliminado(false)
                .build();

        return categoriaRepository.save(categoria);
    }

    @Transactional
    public Categoria modificarCategoria(String id, String nuevoNombre) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID de la categoría no puede ser nulo o vacío");
        }
        validar(nuevoNombre);
        String nombreLimpio = nuevoNombre.trim();

        Categoria categoria = buscarPorId(id);

        Optional<Categoria> existente = categoriaRepository.findByNombreAndEliminadoFalse(nombreLimpio);
        if (existente.isPresent() && !existente.get().getId().equals(id)) {
            throw new IllegalArgumentException("Ya existe otra categoría activa con el nombre: " + nombreLimpio);
        }

        categoria.setNombre(nombreLimpio);
        return categoriaRepository.save(categoria);
    }

    @Transactional
    public void eliminarCategoria(String id) {
        Categoria categoria = buscarPorId(id);
        categoria.setEliminado(true);

        List<SubCategoria> subCategorias = subCategoriaRepository.findByCategoriaIdAndEliminadoFalse(id);
        if (subCategorias != null && !subCategorias.isEmpty()) {
            for (SubCategoria subCategoria : subCategorias) {
                subCategoria.setEliminado(true);
            }
            subCategoriaRepository.saveAll(subCategorias);
        }

        categoriaRepository.save(categoria);
    }

    @Transactional(readOnly = true)
    public List<CategoriaDTO> listarConSubcategorias() {
        List<Categoria> categorias = categoriaRepository.findByEliminadoFalse();
        List<SubCategoria> todasSub = subCategoriaRepository.findByEliminadoFalse();

        Map<String, List<SubCategoria>> subsPorCat = todasSub.stream()
                .filter(s -> s.getCategoria() != null && s.getCategoria().getId() != null)
                .collect(Collectors.groupingBy(s -> s.getCategoria().getId()));

        return categorias.stream()
                .sorted(Comparator.comparing(Categoria::getNombre, String.CASE_INSENSITIVE_ORDER))
                .map(c -> CategoriaDTO.builder()
                        .id(c.getId())
                        .nombre(c.getNombre())
                        .subCategorias(subsPorCat.getOrDefault(c.getId(), Collections.emptyList()))
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public Categoria buscarPorId(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID de la categoría no puede ser nulo o vacío");
        }
        return categoriaRepository.findActive(id)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la categoría activa con ID: " + id));
    }

    @Transactional(readOnly = true)
    public Categoria buscarPorNombre(String nombre) {
        validar(nombre);
        return categoriaRepository.findByNombreAndEliminadoFalse(nombre.trim())
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la categoría activa con nombre: " + nombre));
    }

    @Transactional(readOnly = true)
    public List<Categoria> listarActivas() {
        return categoriaRepository.findByEliminadoFalse();
    }

    @Transactional(readOnly = true)
    public List<Categoria> listarTodas() {
        return categoriaRepository.findAll();
    }
}

