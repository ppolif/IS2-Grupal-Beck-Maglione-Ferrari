package com.example.persona.services;

import com.example.persona.dtos.BaseDto;
import com.example.persona.entities.Base;
import com.example.persona.repositories.BaseRepository;
import com.example.persona.mappers.BaseMapper;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

public abstract class BaseServiceImpl<E extends Base, D extends BaseDto, ID extends Serializable> implements BaseService<D, ID> {
    protected BaseRepository<E, ID> baseRepository;

    @Autowired(required = false)
    protected ModelMapper modelMapper;

    protected BaseMapper<E, D> baseMapper;

    protected Class<E> entityClass;
    protected Class<D> dtoClass;

    public BaseServiceImpl(BaseRepository<E, ID> baseRepository, Class<E> entityClass, Class<D> dtoClass) {
        this.baseRepository = baseRepository;
        this.entityClass = entityClass;
        this.dtoClass = dtoClass;
    }

    public BaseServiceImpl(BaseRepository<E, ID> baseRepository, BaseMapper<E, D> baseMapper) {
        this.baseRepository = baseRepository;
        this.baseMapper = baseMapper;
    }

    protected D toDto(E entity) {
        if (entity == null) {
            return null;
        }
        if (baseMapper != null) {
            return baseMapper.toDto(entity);
        }
        return modelMapper != null ? modelMapper.map(entity, dtoClass) : null;
    }

    protected E toEntity(D dto) {
        if (dto == null) {
            return null;
        }
        if (baseMapper != null) {
            return baseMapper.toEntity(dto);
        }
        return modelMapper != null ? modelMapper.map(dto, entityClass) : null;
    }

    // ============================================================
    // TEMPLATE METHOD: findAll()
    // ============================================================
    @Override
    @Transactional
    public List<D> findAll() throws Exception {
        try {
            beforeFindAll();
            List<E> entities = doFindAll();
            afterFindAll(entities);
            return entities.stream().map(this::toDto).toList();
        } catch (Exception e) {
            handleServiceException("findAll", e);
            throw new Exception(e.getMessage());
        }
    }

    protected void beforeFindAll() throws Exception {
        // Hook
    }

    protected List<E> doFindAll() throws Exception {
        return baseRepository.findAll();
    }

    protected void afterFindAll(List<E> entities) throws Exception {
        // Hook
    }

    // ============================================================
    // TEMPLATE METHOD: findAll(Pageable)
    // ============================================================
    @Override
    @Transactional
    public Page<D> findAll(Pageable pageable) throws Exception {
        try {
            beforeFindAllPaged(pageable);
            Page<E> entities = doFindAll(pageable);
            afterFindAllPaged(entities);
            return entities.map(this::toDto);
        } catch (Exception e) {
            handleServiceException("findAllPaged", e);
            throw new Exception(e.getMessage());
        }
    }

    protected void beforeFindAllPaged(Pageable pageable) throws Exception {
        // Hook
    }

    protected Page<E> doFindAll(Pageable pageable) throws Exception {
        return baseRepository.findAll(pageable);
    }

    protected void afterFindAllPaged(Page<E> entities) throws Exception {
        // Hook
    }

    // ============================================================
    // TEMPLATE METHOD: findById(ID)
    // ============================================================
    @Override
    @Transactional
    public D findById(ID id) throws Exception {
        try {
            validateId(id);
            E entity = findEntityById(id);
            afterFind(entity);
            return toDto(entity);
        } catch (Exception e) {
            handleServiceException("findById", e);
            throw new Exception(e.getMessage());
        }
    }

    protected void validateId(ID id) throws Exception {
        if (id == null) {
            throw new Exception("El identificador no puede ser nulo.");
        }
    }

    protected E findEntityById(ID id) throws Exception {
        Optional<E> entityOptional = baseRepository.findById(id);
        if (entityOptional.isEmpty()) {
            throw new Exception("Entidad no encontrada con id: " + id);
        }
        return entityOptional.get();
    }

    protected void afterFind(E entity) throws Exception {
        // Hook
    }

    // ============================================================
    // TEMPLATE METHOD: save(D)
    // ============================================================
    @Override
    @Transactional
    public D save(D dto) throws Exception {
        try {
            validateDto(dto);
            E entity = toEntity(dto);
            beforeSave(entity, dto);
            E savedEntity = doSave(entity);
            afterSave(savedEntity, dto);
            return toDto(savedEntity);
        } catch (Exception e) {
            handleServiceException("save", e);
            throw new Exception(e.getMessage());
        }
    }

    protected void validateDto(D dto) throws Exception {
        if (dto == null) {
            throw new Exception("El DTO a persistir no puede ser nulo.");
        }
    }

    protected void beforeSave(E entity, D dto) throws Exception {
        // Hook
    }

    protected E doSave(E entity) throws Exception {
        return baseRepository.save(entity);
    }

    protected void afterSave(E savedEntity, D dto) throws Exception {
        // Hook
    }

    // ============================================================
    // TEMPLATE METHOD: update(ID, D)
    // ============================================================
    @Override
    @Transactional
    public D update(ID id, D dto) throws Exception {
        try {
            validateId(id);
            validateUpdateDto(id, dto);
            E existingEntity = findEntityById(id);
            beforeUpdate(existingEntity, dto);
            E entityUpdate = mapUpdate(existingEntity, dto, id);
            E updatedEntity = doUpdate(entityUpdate);
            afterUpdate(updatedEntity, dto);
            return toDto(updatedEntity);
        } catch (Exception e) {
            handleServiceException("update", e);
            throw new Exception(e.getMessage());
        }
    }

    protected void validateUpdateDto(ID id, D dto) throws Exception {
        if (dto == null) {
            throw new Exception("El DTO a actualizar no puede ser nulo.");
        }
    }

    protected void beforeUpdate(E existingEntity, D dto) throws Exception {
        // Hook
    }

    protected E mapUpdate(E existingEntity, D dto, ID id) {
        E entityUpdate = toEntity(dto);
        entityUpdate.setId((Long) id);
        return entityUpdate;
    }

    protected E doUpdate(E entity) throws Exception {
        return baseRepository.save(entity);
    }

    protected void afterUpdate(E updatedEntity, D dto) throws Exception {
        // Hook
    }

    // ============================================================
    // TEMPLATE METHOD: delete(ID)
    // ============================================================
    @Override
    @Transactional
    public boolean delete(ID id) throws Exception {
        try {
            validateId(id);
            validateDelete(id);
            E entity = findEntityById(id);
            beforeDelete(entity);
            doDelete(id);
            afterDelete(id);
            return true;
        } catch (Exception e) {
            handleServiceException("delete", e);
            throw new Exception(e.getMessage());
        }
    }

    protected void validateDelete(ID id) throws Exception {
        // Hook
    }

    protected void beforeDelete(E entity) throws Exception {
        // Hook
    }

    protected void doDelete(ID id) throws Exception {
        baseRepository.deleteById(id);
    }

    protected void afterDelete(ID id) throws Exception {
        // Hook
    }

    // ============================================================
    // HOOK: handleServiceException
    // ============================================================
    protected void handleServiceException(String operation, Exception e) {
        // Hook para logging o transformación si se requiere
    }
}
