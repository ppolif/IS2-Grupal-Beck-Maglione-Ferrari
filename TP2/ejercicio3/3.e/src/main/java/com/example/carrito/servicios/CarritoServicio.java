package com.example.carrito.servicios;

import com.example.carrito.entidades.Articulo;
import com.example.carrito.entidades.Carrito;
import com.example.carrito.entidades.Detalle;
import com.example.carrito.entidades.Usuario;
import com.example.carrito.excepciones.ErrorServicio;
import com.example.carrito.repositorios.ArticuloRepositorio;
import com.example.carrito.repositorios.CarritoRepositorio;
import com.example.carrito.repositorios.DetalleRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Optional;

@Service
public class CarritoServicio {

    @Autowired
    private CarritoRepositorio carritoRepositorio;

    @Autowired
    private DetalleRepositorio detalleRepositorio;

    @Autowired
    private ArticuloRepositorio articuloRepositorio;

    public void recalcularTotal(Carrito carrito) {
        if (carrito == null) {
            return;
        }
        if (carrito.getDetalles() == null || carrito.getDetalles().isEmpty()) {
            carrito.setTotal(0.0);
            return;
        }
        double sumatoria = 0.0;
        for (Detalle d : carrito.getDetalles()) {
            if (!d.isEliminado()) {
                d.recalcularSubtotal();
                sumatoria += d.getSubtotal();
            }
        }
        carrito.setTotal(Math.round(sumatoria * 100.0) / 100.0);
    }

    public void agregarDetalle(Carrito carrito, Detalle detalle) {
        if (carrito == null || detalle == null) {
            return;
        }
        if (carrito.getDetalles() == null) {
            carrito.setDetalles(new ArrayList<>());
        }
        carrito.getDetalles().add(detalle);
        detalle.setCarrito(carrito);
        recalcularTotal(carrito);
    }

    @Transactional
    public Carrito obtenerOCrearCarritoActivo(Usuario usuario) {
        return carritoRepositorio.findByUsuarioIdAndEliminadoFalse(usuario.getId())
                .orElseGet(() -> {
                    Carrito nuevo = Carrito.builder()
                            .usuario(usuario)
                            .total(0.0)
                            .eliminado(false)
                            .detalles(new ArrayList<>())
                            .build();
                    return carritoRepositorio.save(nuevo);
                });
    }

    @Transactional
    public Carrito agregarArticulo(String carritoId, String articuloId, int cantidad) throws ErrorServicio {
        if (cantidad <= 0) {
            throw new ErrorServicio("La cantidad a agregar debe ser mayor a cero");
        }

        Carrito carrito = carritoRepositorio.findById(carritoId)
                .filter(c -> !c.isEliminado())
                .orElseThrow(() -> new ErrorServicio("Carrito no encontrado o inactivo"));

        Articulo articulo = articuloRepositorio.findByIdAndEliminadoFalse(articuloId)
                .orElseThrow(() -> new ErrorServicio("El artículo a agregar no existe o está inactivo"));

        // Buscar si ya existe un detalle activo con este artículo
        Optional<Detalle> detalleExistente = carrito.getDetalles().stream()
                .filter(d -> !d.isEliminado() && d.getArticulo().getId().equals(articuloId))
                .findFirst();

        if (detalleExistente.isPresent()) {
            Detalle det = detalleExistente.get();
            det.setCantidad(det.getCantidad() + cantidad);
            det.recalcularSubtotal();
            detalleRepositorio.save(det);
            recalcularTotal(carrito);
        } else {
            Detalle nuevoDetalle = Detalle.builder()
                    .articulo(articulo)
                    .cantidad(cantidad)
                    .eliminado(false)
                    .build();
            nuevoDetalle.recalcularSubtotal();
            detalleRepositorio.save(nuevoDetalle);
            agregarDetalle(carrito, nuevoDetalle);
        }

        return carritoRepositorio.save(carrito);
    }

    @Transactional
    public Carrito modificarCantidad(String detalleId, int nuevaCantidad) throws ErrorServicio {
        Detalle detalle = detalleRepositorio.findByIdAndEliminadoFalse(detalleId)
                .orElseThrow(() -> new ErrorServicio("Detalle de carrito no encontrado"));

        Carrito carrito = detalle.getCarrito();

        if (nuevaCantidad <= 0) {
            detalle.setEliminado(true);
            detalle.setSubtotal(0.0);
        } else {
            detalle.setCantidad(nuevaCantidad);
            detalle.recalcularSubtotal();
        }

        detalleRepositorio.save(detalle);
        recalcularTotal(carrito);
        return carritoRepositorio.save(carrito);
    }

    @Transactional
    public Carrito removerDetalle(String detalleId) throws ErrorServicio {
        return modificarCantidad(detalleId, 0);
    }

    @Transactional(readOnly = true)
    public Carrito buscarPorId(String id) throws ErrorServicio {
        return carritoRepositorio.findById(id)
                .filter(c -> !c.isEliminado())
                .orElseThrow(() -> new ErrorServicio("Carrito no encontrado"));
    }

    @Transactional
    public void vaciarCarrito(Carrito carrito) {
        if (carrito.getDetalles() != null) {
            for (Detalle d : carrito.getDetalles()) {
                if (!d.isEliminado()) {
                    d.setEliminado(true);
                    d.setSubtotal(0.0);
                    detalleRepositorio.save(d);
                }
            }
        }
        carrito.setTotal(0.0);
        carritoRepositorio.save(carrito);
    }
}
