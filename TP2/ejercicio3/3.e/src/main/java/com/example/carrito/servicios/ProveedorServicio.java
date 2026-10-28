package com.example.carrito.servicios;

import com.example.carrito.entidades.Proveedor;
import com.example.carrito.excepciones.ErrorServicio;
import com.example.carrito.repositorios.ProveedorRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProveedorServicio {

    @Autowired
    private ProveedorRepositorio proveedorRepositorio;

    @Transactional
    public Proveedor registrarProveedor(String nombre) throws ErrorServicio {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new ErrorServicio("El nombre del proveedor no puede estar vacío");
        }

        String nombreNormalizado = nombre.trim();
        if (proveedorRepositorio.existsByNombreIgnoreCaseAndEliminadoFalse(nombreNormalizado)) {
            throw new ErrorServicio("Ya existe un proveedor activo con el nombre: " + nombreNormalizado);
        }

        Proveedor proveedor = Proveedor.builder()
                .nombre(nombreNormalizado)
                .eliminado(false)
                .build();

        return proveedorRepositorio.save(proveedor);
    }

    @Transactional(readOnly = true)
    public List<Proveedor> listarActivos() {
        return proveedorRepositorio.findAllByEliminadoFalse();
    }

    @Transactional(readOnly = true)
    public Proveedor buscarPorId(String id) throws ErrorServicio {
        return proveedorRepositorio.findById(id)
                .filter(p -> !p.isEliminado())
                .orElseThrow(() -> new ErrorServicio("Proveedor no encontrado o inactivo"));
    }
}

