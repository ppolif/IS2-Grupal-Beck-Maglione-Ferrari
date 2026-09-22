package com.example.zero.services;

import com.example.zero.entidades.compraProveedor.Proveedor;
import com.example.zero.repositories.ProveedorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio de negocio para la gestión de Proveedores.
 */
@Service
public class ProveedorService {

    private final ProveedorRepository proveedorRepository;

    public ProveedorService(ProveedorRepository proveedorRepository) {
        this.proveedorRepository = proveedorRepository;
    }

    public void validar(String razonSocial, String cuit) {
        if (razonSocial == null || razonSocial.trim().isEmpty()) {
            throw new IllegalArgumentException("La razón social del proveedor no puede estar vacía");
        }
        if (cuit == null || cuit.trim().isEmpty()) {
            throw new IllegalArgumentException("El CUIT del proveedor no puede estar vacío");
        }
    }

    @Transactional
    public Proveedor crearProveedor(String razonSocial, String cuit) {
        validar(razonSocial, cuit);

        String cuitLimpio = cuit.trim();
        Optional<Proveedor> existente = proveedorRepository.findByCuitAndEliminadoFalse(cuitLimpio);
        if (existente.isPresent()) {
            throw new IllegalArgumentException("Ya existe un proveedor activo con el CUIT: " + cuitLimpio);
        }

        Proveedor proveedor = Proveedor.builder()
                .razonSocial(razonSocial.trim())
                .cuit(cuitLimpio)
                .eliminado(false)
                .build();

        return proveedorRepository.save(proveedor);
    }

    @Transactional
    public Proveedor modificarProveedor(String id, String razonSocial, String cuit) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del proveedor no puede ser nulo o vacío");
        }
        validar(razonSocial, cuit);

        Proveedor proveedor = buscarPorId(id);
        String cuitLimpio = cuit.trim();

        if (!proveedor.getCuit().equalsIgnoreCase(cuitLimpio)) {
            Optional<Proveedor> existente = proveedorRepository.findByCuitAndEliminadoFalse(cuitLimpio);
            if (existente.isPresent()) {
                throw new IllegalArgumentException("Ya existe otro proveedor activo con el CUIT: " + cuitLimpio);
            }
        }

        proveedor.setRazonSocial(razonSocial.trim());
        proveedor.setCuit(cuitLimpio);

        return proveedorRepository.save(proveedor);
    }

    @Transactional
    public void eliminarProveedor(String id) {
        Proveedor proveedor = buscarPorId(id);
        proveedor.setEliminado(true);
        proveedorRepository.save(proveedor);
    }

    @Transactional(readOnly = true)
    public Proveedor buscarPorId(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del proveedor no puede ser nulo o vacío");
        }
        return proveedorRepository.findActive(id)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el proveedor activo con ID: " + id));
    }

    @Transactional(readOnly = true)
    public Proveedor buscarPorCuit(String cuit) {
        if (cuit == null || cuit.trim().isEmpty()) {
            throw new IllegalArgumentException("El CUIT del proveedor no puede ser nulo o vacío");
        }
        return proveedorRepository.findByCuitAndEliminadoFalse(cuit.trim())
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el proveedor activo con CUIT: " + cuit));
    }

    @Transactional(readOnly = true)
    public List<Proveedor> listarActivos() {
        return proveedorRepository.findByEliminadoFalse();
    }

    @Transactional(readOnly = true)
    public List<Proveedor> listarTodos() {
        return proveedorRepository.findAll();
    }
}

