package com.example.carrito.servicios;

import com.example.carrito.entidades.Direccion;
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

    /**
     * Registra un nuevo proveedor junto con su dirección geográfica (latitud y longitud).
     *
     * @param nombre   Nombre de la empresa o vivero proveedor.
     * @param latitud  Latitud en grados decimales (-90 a 90).
     * @param longitud Longitud en grados decimales (-180 a 180).
     * @return El proveedor persistido con su dirección asociada.
     * @throws ErrorServicio Si los datos son nulos, inválidos o el nombre está duplicado.
     */
    @Transactional
    public Proveedor registrarProveedor(String nombre, Double latitud, Double longitud) throws ErrorServicio {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new ErrorServicio("El nombre del proveedor no puede estar vacío");
        }

        // Validación de presencia de coordenadas
        if (latitud == null || longitud == null) {
            throw new ErrorServicio("Debe ingresar la latitud y longitud de la dirección del proveedor");
        }

        // Validación de rangos geográficos válidos para OpenStreetMap / Leaflet
        if (latitud < -90.0 || latitud > 90.0) {
            throw new ErrorServicio("La latitud debe estar entre -90 y 90 grados");
        }
        if (longitud < -180.0 || longitud > 180.0) {
            throw new ErrorServicio("La longitud debe estar entre -180 y 180 grados");
        }

        String nombreNormalizado = nombre.trim();
        if (proveedorRepositorio.existsByNombreIgnoreCaseAndEliminadoFalse(nombreNormalizado)) {
            throw new ErrorServicio("Ya existe un proveedor activo con el nombre: " + nombreNormalizado);
        }

        // Se construye la entidad Direccion con las coordenadas ingresadas
        Direccion direccion = Direccion.builder()
                .latitud(latitud)
                .longitud(longitud)
                .eliminado(false)
                .build();

        // Se construye el Proveedor asociándole su Direccion (se persiste en cascada)
        Proveedor proveedor = Proveedor.builder()
                .nombre(nombreNormalizado)
                .direccion(direccion)
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

