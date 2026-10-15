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
        } else {
            Detalle nuevoDetalle = Detalle.builder()
                    .articulo(articulo)
                    .carrito(carrito)
                    .cantidad(cantidad)
                    .eliminado(false)
                    .build();
            nuevoDetalle.recalcularSubtotal();
            detalleRepositorio.save(nuevoDetalle);
            carrito.getDetalles().add(nuevoDetalle);
        }

        carrito.recalcularTotal();
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
        carrito.recalcularTotal();
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

