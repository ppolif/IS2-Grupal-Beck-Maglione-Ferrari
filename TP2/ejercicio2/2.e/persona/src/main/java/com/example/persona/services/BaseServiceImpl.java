package com.example.persona.services;

import com.example.persona.dtos.BaseDto;
import com.example.persona.entities.Base;
import com.example.persona.repositories.BaseRepository;
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

    @Autowired
    protected ModelMapper modelMapper;

    protected Class<E> entityClass;
    protected Class<D> dtoClass;

    public BaseServiceImpl(BaseRepository<E, ID> baseRepository, Class<E> entityClass, Class<D> dtoClass) {
        this.baseRepository = baseRepository;
        this.entityClass = entityClass;
        this.dtoClass = dtoClass;
    }

    protected D toDto(E entity) {
        return modelMapper.map(entity, dtoClass);
    }

    protected E toEntity(D dto) {
        return modelMapper.map(dto, entityClass);
    }

    @Override
    @Transactional
    public List<D> findAll() throws Exception {
        try {
            List<E> entities = baseRepository.findAll();
            return entities.stream()
                    .filter(e -> e.getActivo() == null || Boolean.TRUE.equals(e.getActivo()))
                    .map(this::toDto)
                    .toList();
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    @Transactional
    public Page<D> findAll(Pageable pageable) throws Exception {
        try {
            Page<E> entities = baseRepository.findAll(pageable);
            return entities.map(this::toDto);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    @Transactional
    public D findById(ID id) throws Exception {
        try {
            Optional<E> entityOptional = baseRepository.findById(id);
            if (entityOptional.isEmpty() || Boolean.FALSE.equals(entityOptional.get().getActivo())) {
                throw new Exception("Entidad no encontrada con id: " + id);
            }
            return toDto(entityOptional.get());
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    @Transactional
    public D save(D dto) throws Exception {
        try {
            if (dto.getActivo() == null) {
                dto.setActivo(true);
            }
            E entity = toEntity(dto);
            if (entity.getActivo() == null) {
                entity.setActivo(true);
            }
            entity = baseRepository.save(entity);
            return toDto(entity);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    @Transactional
    public D update(ID id, D dto) throws Exception {
        try {
            Optional<E> entityOptional = baseRepository.findById(id);
            if (entityOptional.isEmpty() || Boolean.FALSE.equals(entityOptional.get().getActivo())) {
                throw new Exception("Entidad no encontrada con id: " + id);
            }
            E entityUpdate = toEntity(dto);
            entityUpdate.setId((Long) id);
            if (entityUpdate.getActivo() == null) {
                entityUpdate.setActivo(true);
            }
            entityUpdate = baseRepository.save(entityUpdate);
            return toDto(entityUpdate);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    @Transactional
    public boolean delete(ID id) throws Exception {
        try {
            Optional<E> entityOptional = baseRepository.findById(id);
            if (entityOptional.isPresent()) {
                E entity = entityOptional.get();
                if (Boolean.FALSE.equals(entity.getActivo())) {
                    throw new Exception("Entidad ya se encuentra eliminada con id: " + id);
                }
                entity.setActivo(false);
                baseRepository.save(entity);
                return true;
            } else {
                throw new Exception("Entidad no encontrada con id: " + id);
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }
}
