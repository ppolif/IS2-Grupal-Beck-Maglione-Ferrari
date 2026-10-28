package com.colmena.videojuegos.services;

import com.colmena.videojuegos.entities.Base;

import java.io.Serializable;
import java.util.List;

public interface ServicioBase<E extends Base, ID extends Serializable> {
    List<E> findAll() throws Exception;
    E findById(ID id) throws Exception;
    E saveOne(E entity) throws Exception;
    E updateOne(E entity, ID id) throws Exception;
    boolean deleteById(ID id) throws Exception;
}
