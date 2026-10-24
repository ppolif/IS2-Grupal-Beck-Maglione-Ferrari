package com.example.zero.services;

import com.example.zero.entidades.producto.Categoria;
import com.example.zero.entidades.producto.SubCategoria;
import com.example.zero.repositories.SubCategoriaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SubCategoriaService {

    private final SubCategoriaRepository subCategoriaRepository;
    private final CategoriaService categoriaService;

    public SubCategoriaService(SubCategoriaRepository subCategoriaRepository, CategoriaService categoriaService) {
        this.subCategoriaRepository = subCategoriaRepository;
        this.categoriaService = categoriaService;
    }

    public void validar(String nombre, String categoriaId) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la subcategoría no puede estar vacío");
        }
        if (categoriaId == null || categoriaId.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID de la categoría asociada no puede ser nulo o vacío");
        }
    }

    @Transactional
    public SubCategoria crearSubCategoria(String nombre, String categoriaId) {
        validar(nombre, categoriaId);
        String nombreLimpio = nombre.trim();

        Categoria categoria = categoriaService.buscarPorId(categoriaId);

        SubCategoria subCategoria = SubCategoria.builder()
                .nombre(nombreLimpio)
                .categoria(categoria)
                .eliminado(false)
                .build();

        return subCategoriaRepository.save(subCategoria);
    }

    @Transactional
    public SubCategoria modificarSubCategoria(String id, String nuevoNombre, String nuevoCategoriaId) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID de la subcategoría no puede ser nulo o vacío");
        }
        validar(nuevoNombre, nuevoCategoriaId);

        SubCategoria subCategoria = buscarPorId(id);
        Categoria categoria = categoriaService.buscarPorId(nuevoCategoriaId);

        subCategoria.setNombre(nuevoNombre.trim());
        subCategoria.setCategoria(categoria);

        return subCategoriaRepository.save(subCategoria);
    }

    @Transactional
    public void eliminarSubCategoria(String id) {
        SubCategoria subCategoria = buscarPorId(id);
        subCategoria.setEliminado(true);
        subCategoriaRepository.save(subCategoria);
    }

    @Transactional(readOnly = true)
    public SubCategoria buscarPorId(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID de la subcategoría no puede ser nulo o vacío");
        }
        return subCategoriaRepository.findActive(id)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la subcategoría activa con ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<SubCategoria> listarPorCategoria(String categoriaId) {
        if (categoriaId == null || categoriaId.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID de la categoría no puede ser nulo o vacío");
        }
        return subCategoriaRepository.findByCategoriaIdAndEliminadoFalse(categoriaId);
    }

    @Transactional(readOnly = true)
    public List<SubCategoria> listarActivas() {
        return subCategoriaRepository.findByEliminadoFalse();
    }

    @Transactional(readOnly = true)
    public List<SubCategoria> listarTodas() {
        return subCategoriaRepository.findAll();
    }
}

