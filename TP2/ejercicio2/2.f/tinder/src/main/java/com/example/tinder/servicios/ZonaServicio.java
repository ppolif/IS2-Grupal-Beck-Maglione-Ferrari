package com.example.tinder.servicios;

import com.example.tinder.entidades.Zona;
import com.example.tinder.errores.ErrorServicio;
import com.example.tinder.repositorios.ZonaRepositorio;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ZonaServicio {

    @Autowired
    private ZonaRepositorio zonaRepositorio;

    public List<Zona> listarTodas() {
        return zonaRepositorio.findAll();
    }

    public Zona buscarPorId(String id) throws ErrorServicio {
        Optional<Zona> opt = zonaRepositorio.findById(id);
        if (opt.isPresent()) {
            return opt.get();
        }
        throw new ErrorServicio("No se encontró la zona con id: " + id);
    }

    @Transactional
    public Zona crearZona(String nombre, String descripcion) throws ErrorServicio {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new ErrorServicio("El nombre de la zona es obligatorio");
        }
        Zona zona = new Zona();
        zona.setNombre(nombre.trim());
        zona.setDescripcion(descripcion);
        return zonaRepositorio.save(zona);
    }
}

