package com.example.persona.services;

import com.example.persona.dtos.BaseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.Serializable;
import java.util.List;

public interface BaseService<D extends BaseDto, ID extends Serializable> {
    public List<D> findAll() throws Exception;
    public Page<D> findAll(Pageable pageable) throws Exception;
    public D findById(ID id) throws Exception;
    public D save(D dto) throws Exception;
    public D update(ID id, D dto) throws Exception;
    public boolean delete(ID id) throws Exception;
}
