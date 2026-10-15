package com.colmena.videojuegos.services;

import com.colmena.videojuegos.entities.Videojuego;
import com.colmena.videojuegos.repositories.RepositorioVideojuego;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class ServicioVideojuego extends ServicioBaseImpl<Videojuego, Long> {

    private final RepositorioVideojuego repositorioVideojuego;

    @Autowired
    public ServicioVideojuego(RepositorioVideojuego repositorio) {
        super(repositorio);
        this.repositorioVideojuego = repositorio;
    }

    /* Metodos específicos de dominio */

    @Transactional
    public List<Videojuego> findAllByActivo() throws Exception {
        try {
            return this.repositorioVideojuego.findAllByActivo();
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Transactional
    public Videojuego findByIdAndActivo(long id) throws Exception {
        try {
            Optional<Videojuego> opt = this.repositorioVideojuego.findByIdAndActivo(id);
            if (opt.isEmpty()) {
                throw new Exception("Videojuego activo no encontrado con id: " + id);
            }
            return opt.get();
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Transactional
    public List<Videojuego> findByTitle(String q) throws Exception {
        try {
            return this.repositorioVideojuego.findByTitle(q);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }
}
