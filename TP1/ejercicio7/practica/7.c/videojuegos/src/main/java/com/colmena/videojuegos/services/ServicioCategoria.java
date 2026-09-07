package com.colmena.videojuegos.services;

import com.colmena.videojuegos.dtos.CategoriaResponseDTO;
import com.colmena.videojuegos.entities.Categoria;
import com.colmena.videojuegos.repositories.RepositorioCategoria;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ServicioCategoria implements ServicioBase<Categoria> {

    @Autowired
    private RepositorioCategoria repositorioCategoria;

    public ServicioCategoria(RepositorioCategoria repositorioCategoria) {
        this.repositorioCategoria = repositorioCategoria;
    }

    @Override
    @Transactional
    public List<Categoria> findAll() throws Exception {
        try {
            return this.repositorioCategoria.findAll();
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    @Transactional
    public Categoria findById(long id) throws Exception {
        try {
            Optional<Categoria> opt = this.repositorioCategoria.findById(id);
            return opt.get();
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    @Transactional
    public Categoria saveOne(Categoria entity) throws Exception {
        try {
            return this.repositorioCategoria.save(entity);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    @Transactional
    public Categoria updateOne(Categoria entity, long id) throws Exception {
        try {
            Optional<Categoria> opt = this.repositorioCategoria.findById(id);
            Categoria categoria = opt.get();
            categoria = this.repositorioCategoria.save(entity);
            return categoria;
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    @Transactional
    public boolean deleteById(long id) throws Exception {
        try {
            Optional<Categoria> opt = this.repositorioCategoria.findById(id);
            if (!opt.isEmpty()) {
                this.repositorioCategoria.delete(opt.get());
                return true;
            } else {
                throw new Exception();
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Transactional
    public List<CategoriaResponseDTO> listarDTO() throws Exception {
        return this.findAll().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    public CategoriaResponseDTO convertirADTO(Categoria categoria) {
        if (categoria == null) {
            return null;
        }
        return new CategoriaResponseDTO(categoria.getId(), categoria.getNombre());
    }
}