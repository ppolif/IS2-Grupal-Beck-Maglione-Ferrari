package com.example.zero.services;

import com.example.zero.entidades.producto.Producto;
import com.example.zero.entidades.producto.VigenciaPrecio;
import com.example.zero.repositories.ProductoRepository;
import com.example.zero.repositories.VigenciaPrecioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class VigenciaPrecioService {

    private final VigenciaPrecioRepository vigenciaPrecioRepository;
    private final ProductoRepository productoRepository;

    public VigenciaPrecioService(VigenciaPrecioRepository vigenciaPrecioRepository, ProductoRepository productoRepository) {
        this.vigenciaPrecioRepository = vigenciaPrecioRepository;
        this.productoRepository = productoRepository;
    }

    public void validar(double precio, LocalDate fechaDesde) {
        if (precio <= 0) {
            throw new IllegalArgumentException("El precio debe ser un valor positivo mayor a cero");
        }
        if (fechaDesde == null) {
            throw new IllegalArgumentException("La fecha desde no puede ser nula");
        }
    }

    @Transactional
    public VigenciaPrecio crearVigenciaPrecio(String productoId, double precio, LocalDate fechaDesde) {
        if (productoId == null || productoId.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del producto no puede ser nulo o vacío");
        }
        validar(precio, fechaDesde);

        Producto producto = productoRepository.findActive(productoId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el producto activo con ID: " + productoId));

        // Cerrar vigencia actual si existe
        Optional<VigenciaPrecio> vigenciaActual = vigenciaPrecioRepository.findPrecioActualByProductoId(productoId);
        if (vigenciaActual.isPresent()) {
            VigenciaPrecio actual = vigenciaActual.get();
            actual.setFechaHasta(fechaDesde);
            vigenciaPrecioRepository.save(actual);
        }

        // Crear nueva vigencia actual (fechaHasta == null)
        VigenciaPrecio nuevaVigencia = VigenciaPrecio.builder()
                .producto(producto)
                .precio(precio)
                .fechaDesde(fechaDesde)
                .fechaHasta(null)
                .eliminado(false)
                .build();

        return vigenciaPrecioRepository.save(nuevaVigencia);
    }

    @Transactional
    public VigenciaPrecio actualizarPrecio(String productoId, double nuevoPrecio) {
        return crearVigenciaPrecio(productoId, nuevoPrecio, LocalDate.now());
    }

    @Transactional
    public VigenciaPrecio modificarPrecioVigente(String vigenciaId, double nuevoPrecio) {
        if (vigenciaId == null || vigenciaId.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID de la vigencia de precio no puede ser nulo o vacío");
        }
        if (nuevoPrecio <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor a cero");
        }

        VigenciaPrecio vigencia = vigenciaPrecioRepository.findActive(vigenciaId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la vigencia de precio activa con ID: " + vigenciaId));

        vigencia.setPrecio(nuevoPrecio);
        return vigenciaPrecioRepository.save(vigencia);
    }

    @Transactional
    public void eliminarVigencia(String vigenciaId) {
        if (vigenciaId == null || vigenciaId.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID de la vigencia de precio no puede ser nulo o vacío");
        }

        VigenciaPrecio vigencia = vigenciaPrecioRepository.findActive(vigenciaId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la vigencia de precio activa con ID: " + vigenciaId));

        vigencia.setEliminado(true);
        vigenciaPrecioRepository.save(vigencia);
    }

    @Transactional(readOnly = true)
    public VigenciaPrecio buscarVigenciaActual(String productoId) {
        if (productoId == null || productoId.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del producto no puede ser nulo o vacío");
        }
        return vigenciaPrecioRepository.findPrecioActualByProductoId(productoId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró una vigencia de precio actual para el producto con ID: " + productoId));
    }

    @Transactional(readOnly = true)
    public double obtenerPrecioActual(String productoId) {
        return buscarVigenciaActual(productoId).getPrecio();
    }

    @Transactional(readOnly = true)
    public List<VigenciaPrecio> listarHistorialPrecios(String productoId) {
        if (productoId == null || productoId.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del producto no puede ser nulo o vacío");
        }
        return vigenciaPrecioRepository.findByProductoIdAndEliminadoFalseOrderByFechaDesdeDesc(productoId);
    }
}

