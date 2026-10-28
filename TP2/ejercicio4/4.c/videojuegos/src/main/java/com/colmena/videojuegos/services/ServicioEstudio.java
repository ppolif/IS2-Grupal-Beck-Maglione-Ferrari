package com.colmena.videojuegos.services;

import com.colmena.videojuegos.entities.Estudio;
import com.colmena.videojuegos.repositories.RepositorioEstudio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ServicioEstudio extends ServicioBaseImpl<Estudio, Long> {

    @Autowired
    public ServicioEstudio(RepositorioEstudio repositorio) {
        super(repositorio);
    }
}
