package com.colmena.videojuegos.repositories;

import com.colmena.videojuegos.entities.Categoria;
import org.springframework.stereotype.Repository;

@Repository
public interface RepositorioCategoria extends BaseRepository<Categoria, Long> {
}
