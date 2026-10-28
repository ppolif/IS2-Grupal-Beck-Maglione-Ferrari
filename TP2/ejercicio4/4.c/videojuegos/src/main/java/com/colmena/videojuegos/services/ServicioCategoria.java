package com.colmena.videojuegos.services;

import com.colmena.videojuegos.entities.Categoria;
import com.colmena.videojuegos.repositories.RepositorioCategoria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ServicioCategoria extends ServicioBaseImpl<Categoria, Long> {

    @Autowired
    public ServicioCategoria(RepositorioCategoria repositorio) {
        super(repositorio);
    }
}
