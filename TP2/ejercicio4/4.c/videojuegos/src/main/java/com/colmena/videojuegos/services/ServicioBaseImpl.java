package com.colmena.videojuegos.services;

import com.colmena.videojuegos.entities.Base;
import com.colmena.videojuegos.repositories.BaseRepository;

import javax.transaction.Transactional;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;

public abstract class ServicioBaseImpl<E extends Base, ID extends Serializable> implements ServicioBase<E, ID> {

    protected BaseRepository<E, ID> baseRepository;

    public ServicioBaseImpl(BaseRepository<E, ID> baseRepository) {
        this.baseRepository = baseRepository;
    }

    /**
     * Algoritmo Plantilla (Template Method) para listar entidades.
     */
    @Override
    @Transactional
    public List<E> findAll() throws Exception {
        try {
            List<E> entities = this.baseRepository.findAll();
            return filterEntities(entities);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    /**
     * Paso de gancho (Hook) modificable por subclases para filtrar elementos.
     */
    protected List<E> filterEntities(List<E> entities) {
        return entities;
    }

    /**
     * Algoritmo Plantilla (Template Method) para buscar entidad por ID.
     */
    @Override
    @Transactional
    public E findById(ID id) throws Exception {
        try {
            Optional<E> opt = this.baseRepository.findById(id);
            if (opt.isEmpty()) {
                throw new Exception("Entidad no encontrada con id: " + id);
            }
            validateFoundEntity(opt.get());
            return opt.get();
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    /**
     * Hook para validar entidad encontrada.
     */
    protected void validateFoundEntity(E entity) throws Exception {
    }

    /**
     * Algoritmo Plantilla (Template Method) para persistir nueva entidad.
     */
    @Override
    @Transactional
    public E saveOne(E entity) throws Exception {
        try {
            beforeSave(entity);
            E savedEntity = this.baseRepository.save(entity);
            afterSave(savedEntity);
            return savedEntity;
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    /**
     * Hook previo al guardado.
     */
    protected void beforeSave(E entity) throws Exception {
        entity.setActivo(true);
    }

    /**
     * Hook posterior al guardado.
     */
    protected void afterSave(E entity) {
    }

    /**
     * Algoritmo Plantilla (Template Method) para actualizar entidad.
     */
    @Override
    @Transactional
    public E updateOne(E entity, ID id) throws Exception {
        try {
            Optional<E> opt = this.baseRepository.findById(id);
            if (opt.isEmpty()) {
                throw new Exception("Entidad no encontrada con id: " + id);
            }
            beforeUpdate(entity, id);
            entity.setId((Long) id);
            E updatedEntity = this.baseRepository.save(entity);
            afterUpdate(updatedEntity);
            return updatedEntity;
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    /**
     * Hook previo a la actualización.
     */
    protected void beforeUpdate(E entity, ID id) throws Exception {
    }

    /**
     * Hook posterior a la actualización.
     */
    protected void afterUpdate(E entity) {
    }

    /**
     * Algoritmo Plantilla (Template Method) para alternar / eliminar lógicamente una entidad.
     */
    @Override
    @Transactional
    public boolean deleteById(ID id) throws Exception {
        try {
            Optional<E> opt = this.baseRepository.findById(id);
            if (opt.isPresent()) {
                E entity = opt.get();
                beforeDelete(entity);
                entity.setActivo(!entity.isActivo());
                this.baseRepository.save(entity);
                afterDelete(entity);
                return true;
            } else {
                throw new Exception("Entidad no encontrada con id: " + id);
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    /**
     * Hook previo al borrado lógico.
     */
    protected void beforeDelete(E entity) throws Exception {
    }

    /**
     * Hook posterior al borrado lógico.
     */
    protected void afterDelete(E entity) {
    }
}
