package com.example.carrito.servicios;

import com.example.carrito.entidades.Articulo;
import com.example.carrito.entidades.Imagen;
import com.example.carrito.entidades.Proveedor;
import com.example.carrito.excepciones.ErrorServicio;
import com.example.carrito.repositorios.ArticuloRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class ArticuloServicio {

    @Autowired
    private ArticuloRepositorio articuloRepositorio;

    @Autowired
    private ProveedorServicio proveedorServicio;

    @Autowired
    private ImagenServicio imagenServicio;

    @Transactional
    public Articulo registrarArticulo(String nombre, double precio, String proveedorId, MultipartFile archivoImagen)
            throws ErrorServicio {

        validar(nombre, precio, proveedorId);

        Proveedor proveedor = proveedorServicio.buscarPorId(proveedorId);
        Imagen imagen = null;
        if (archivoImagen != null && !archivoImagen.isEmpty()) {
            imagen = imagenServicio.guardar(archivoImagen);
        }

        Articulo articulo = Articulo.builder()
                .nombre(nombre.trim())
                .precio(precio)
                .proveedor(proveedor)
                .imagen(imagen)
                .eliminado(false)
                .build();

        return articuloRepositorio.save(articulo);
    }

    @Transactional(readOnly = true)
    public List<Articulo> listarActivos() {
        return articuloRepositorio.findAllByEliminadoFalse();
    }

    @Transactional(readOnly = true)
    public Articulo buscarPorId(String id) throws ErrorServicio {
        return articuloRepositorio.findByIdAndEliminadoFalse(id)
                .orElseThrow(() -> new ErrorServicio("El artículo solicitado no existe o no se encuentra activo"));
    }

    @Transactional
    public void eliminarLogico(String id) throws ErrorServicio {
        Articulo articulo = buscarPorId(id);
        articulo.setEliminado(true);
        articuloRepositorio.save(articulo);
    }

    private void validar(String nombre, double precio, String proveedorId) throws ErrorServicio {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new ErrorServicio("El nombre del artículo no puede estar en blanco");
        }
        if (precio <= 0) {
            throw new ErrorServicio("El precio debe ser un valor numérico mayor a cero");
        }
        if (proveedorId == null || proveedorId.trim().isEmpty()) {
            throw new ErrorServicio("Debe asociar el artículo a un proveedor activo");
        }
    }
}

