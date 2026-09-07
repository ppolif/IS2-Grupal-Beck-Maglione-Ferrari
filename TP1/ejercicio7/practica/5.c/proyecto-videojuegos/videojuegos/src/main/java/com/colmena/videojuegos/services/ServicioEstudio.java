package com.colmena.videojuegos.services;

import com.colmena.videojuegos.dtos.EstudioResponseDTO;
import com.colmena.videojuegos.entities.Estudio;
import com.colmena.videojuegos.repositories.RepositorioEstudio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ServicioEstudio implements ServicioBase<Estudio> {

    @Autowired
    private RepositorioEstudio repositorioEstudio;

    public ServicioEstudio(RepositorioEstudio repositorioEstudio) {
        this.repositorioEstudio = repositorioEstudio;
    }

    @Override
    @Transactional
    public List<Estudio> findAll() throws Exception {
        try {
            return this.repositorioEstudio.findAll();
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    @Transactional
    public Estudio findById(long id) throws Exception {
        try {
            Optional<Estudio> opt = this.repositorioEstudio.findById(id);
            return opt.get();
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    @Transactional
    public Estudio saveOne(Estudio entity) throws Exception {
        try {
            return this.repositorioEstudio.save(entity);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    @Transactional
    public Estudio updateOne(Estudio entity, long id) throws Exception {
        try {
            Optional<Estudio> opt = this.repositorioEstudio.findById(id);
            Estudio estudio = opt.get();
            estudio = this.repositorioEstudio.save(entity);
            return estudio;
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    @Transactional
    public boolean deleteById(long id) throws Exception {
        try {
            Optional<Estudio> opt = this.repositorioEstudio.findById(id);
            if (!opt.isEmpty()) {
                this.repositorioEstudio.delete(opt.get());
                return true;
            } else {
                throw new Exception();
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Transactional
    public List<EstudioResponseDTO> listarDTO() throws Exception {
        return this.findAll().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    public EstudioResponseDTO convertirADTO(Estudio estudio) {
        if (estudio == null) {
            return null;
        }
        return new EstudioResponseDTO(estudio.getId(), estudio.getNombre());
    }
}