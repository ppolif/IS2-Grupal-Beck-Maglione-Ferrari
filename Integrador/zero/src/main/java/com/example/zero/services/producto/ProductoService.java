package com.example.zero.services.producto;

import com.example.zero.entidades.Imagen;
import com.example.zero.entidades.producto.Producto;
import com.example.zero.entidades.producto.SubCategoria;
import com.example.zero.entidades.producto.VigenciaPrecio;
import com.example.zero.enums.TipoImagen;
import com.example.zero.repositories.ProductoRepository;
import com.example.zero.services.ImagenService;
import com.example.zero.services.SubCategoriaService;
import com.example.zero.services.VigenciaPrecioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final SubCategoriaService subCategoriaService;
    private final VigenciaPrecioService vigenciaPrecioService;
    private final ImagenService imagenService;

    public ProductoService(ProductoRepository productoRepository,
                           SubCategoriaService subCategoriaService,
                           VigenciaPrecioService vigenciaPrecioService) {
        this(productoRepository, subCategoriaService, vigenciaPrecioService, null);
    }

    @Autowired
    public ProductoService(ProductoRepository productoRepository,
                           SubCategoriaService subCategoriaService,
                           VigenciaPrecioService vigenciaPrecioService,
                           ImagenService imagenService) {
        this.productoRepository = productoRepository;
        this.subCategoriaService = subCategoriaService;
        this.vigenciaPrecioService = vigenciaPrecioService;
        this.imagenService = imagenService;
    }

    public void validar(String codigo, String nombre, String subCategoriaId) {
        if (codigo == null || codigo.trim().isEmpty()) {
            throw new IllegalArgumentException("El código del producto no puede estar vacío");
        }
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del producto no puede estar vacío");
        }
        if (subCategoriaId == null || subCategoriaId.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID de la subcategoría no puede ser nulo o vacío");
        }
    }

    @Transactional
    public Producto crearProducto(String codigo, String nombre, String descripcion, String talle,
                                  String subCategoriaId, double precioInicial, boolean enOferta) {
        return crearProductoInterno(codigo, nombre, descripcion, talle, subCategoriaId, precioInicial, enOferta, 0, null);
    }

    @Transactional
    public Producto crearProducto(String codigo, String nombre, String descripcion, String talle,
                                  String subCategoriaId, double precioInicial, boolean enOferta,
                                  MultipartFile archivoImagen) {
        return crearProducto(codigo, nombre, descripcion, talle, subCategoriaId, precioInicial, enOferta, 0, archivoImagen);
    }

    @Transactional
    public Producto crearProducto(String codigo, String nombre, String descripcion, String talle,
                                  String subCategoriaId, double precioInicial, boolean enOferta,
                                  int stock, MultipartFile archivoImagen) {
        if (archivoImagen == null || archivoImagen.isEmpty()) {
            throw new IllegalArgumentException("La imagen del producto es obligatoria");
        }
        return crearProductoInterno(codigo, nombre, descripcion, talle, subCategoriaId, precioInicial, enOferta, stock, archivoImagen);
    }

    private Producto crearProductoInterno(String codigo, String nombre, String descripcion, String talle,
                                          String subCategoriaId, double precioInicial, boolean enOferta,
                                          int stock, MultipartFile archivoImagen) {
        validar(codigo, nombre, subCategoriaId);
        if (precioInicial <= 0) {
            throw new IllegalArgumentException("El precio inicial debe ser mayor a cero");
        }

        String codigoLimpio = codigo.trim();
        Optional<Producto> existente = productoRepository.findByCodigoAndEliminadoFalse(codigoLimpio);
        if (existente.isPresent()) {
            throw new IllegalArgumentException("Ya existe un producto activo con el código: " + codigoLimpio);
        }

        SubCategoria subCategoria = subCategoriaService.buscarPorId(subCategoriaId);

        List<Imagen> imagenes = new ArrayList<>();
        if (archivoImagen != null && !archivoImagen.isEmpty() && imagenService != null) {
            Imagen img = imagenService.guardarImagen(archivoImagen, TipoImagen.PRODUCTO);
            if (img != null) {
                imagenes.add(img);
            }
        }

        //Patron builder
        Producto producto = Producto.builder()
                .codigo(codigoLimpio)
                .nombre(nombre.trim())
                .descripcion(descripcion != null ? descripcion.trim() : null)
                .talle(talle != null ? talle.trim() : null)
                .subCategoria(subCategoria)
                .enOferta(enOferta)
                .stock(Math.max(0, stock))
                .imagenes(imagenes)
                .eliminado(false)
                .build();

        Producto productoGuardado = productoRepository.save(producto);

        // Crear vigencia de precio inicial
        vigenciaPrecioService.crearVigenciaPrecio(productoGuardado.getId(), precioInicial, LocalDate.now());

        return productoGuardado;
    }

    @Transactional
    public Producto modificarProducto(String id, String nombre, String descripcion, String talle,
                                      String subCategoriaId, Boolean enOferta) {
        return modificarProducto(id, nombre, descripcion, talle, subCategoriaId, enOferta, null);
    }

    @Transactional
    public Producto modificarProducto(String id, String nombre, String descripcion, String talle,
                                      String subCategoriaId, Boolean enOferta, Integer stock) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del producto no puede ser nulo o vacío");
        }

        Producto producto = buscarPorId(id);

        if (nombre != null && !nombre.trim().isEmpty()) {
            producto.setNombre(nombre.trim());
        }
        if (descripcion != null) {
            producto.setDescripcion(descripcion.trim());
        }
        if (talle != null) {
            producto.setTalle(talle.trim());
        }
        if (subCategoriaId != null && !subCategoriaId.trim().isEmpty()) {
            SubCategoria subCategoria = subCategoriaService.buscarPorId(subCategoriaId);
            producto.setSubCategoria(subCategoria);
        }
        if (enOferta != null) {
            producto.setEnOferta(enOferta);
        }
        if (stock != null && stock >= 0) {
            producto.setStock(stock);
        }

        return productoRepository.save(producto);
    }

    @Transactional
    public void eliminarProducto(String id) {
        Producto producto = buscarPorId(id);
        producto.setEliminado(true);
        productoRepository.save(producto);
    }

    @Transactional(readOnly = true)
    public Producto buscarPorId(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del producto no puede ser nulo o vacío");
        }
        return productoRepository.findActive(id)
                .or(() -> productoRepository.findById(id).filter(p -> !p.isEliminado()))
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el producto activo con ID: " + id));
    }

    @Transactional(readOnly = true)
    public Producto buscarPorCodigo(String codigo) {
        if (codigo == null || codigo.trim().isEmpty()) {
            throw new IllegalArgumentException("El código del producto no puede ser nulo o vacío");
        }
        return productoRepository.findByCodigoAndEliminadoFalse(codigo.trim())
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el producto activo con código: " + codigo));
    }

    @Transactional(readOnly = true)
    public List<Producto> listarActivos() {
        return productoRepository.findByEliminadoFalse();
    }

    @Transactional(readOnly = true)
    public List<Producto> listarTodos() {
        return productoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Producto> listarPorSubCategoria(String subCategoriaId) {
        if (subCategoriaId == null || subCategoriaId.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID de la subcategoría no puede ser nulo o vacío");
        }
        return productoRepository.findBySubCategoriaIdAndEliminadoFalse(subCategoriaId);
    }

    @Transactional(readOnly = true)
    public List<Producto> listarEnOferta() {
        return productoRepository.findByEnOfertaTrueAndEliminadoFalse();
    }

    @Transactional(readOnly = true)
    public Producto marcarEnOferta(String id, boolean enOferta) {
        Producto producto = buscarPorId(id);
        producto.setEnOferta(enOferta);
        return productoRepository.save(producto);
    }

    // ==================== GESTIÓN DE PRECIOS ====================
    @Transactional
    public VigenciaPrecio actualizarPrecio(String productoId, double nuevoPrecio) {
        buscarPorId(productoId); // Validar existencia activa
        return vigenciaPrecioService.actualizarPrecio(productoId, nuevoPrecio);
    }

    @Transactional(readOnly = true)
    public double obtenerPrecioActual(String productoId) {
        buscarPorId(productoId); // Validar existencia activa
        return vigenciaPrecioService.obtenerPrecioActual(productoId);
    }

    @Transactional
    public double aplicarAumentoPorInflacion(String productoId, double porcentaje) {
        if (porcentaje <= 0) {
            throw new IllegalArgumentException("El porcentaje de aumento debe ser mayor a cero");
        }
        double precioActual = obtenerPrecioActual(productoId);
        double nuevoPrecio = Math.round((precioActual * (1.0 + (porcentaje / 100.0))) * 100.0) / 100.0;
        actualizarPrecio(productoId, nuevoPrecio);
        return nuevoPrecio;
    }

    @Transactional
    public void aplicarAumentoGeneralPorInflacion(double porcentaje) {
        if (porcentaje <= 0) {
            throw new IllegalArgumentException("El porcentaje de aumento debe ser mayor a cero");
        }
        List<Producto> productosActivos = listarActivos();
        for (Producto producto : productosActivos) {
            try {
                aplicarAumentoPorInflacion(producto.getId(), porcentaje);
            } catch (Exception ignored) {
                // Si algún producto no tiene precio asignado, se continúa con los demás
            }
        }
    }

    // ==================== GESTIÓN DE STOCK ====================

    public boolean esStockCritico(int stockActual, int stockTotal) {
        if (stockTotal <= 0) {
            throw new IllegalArgumentException("El stock total de referencia debe ser mayor a cero");
        }
        if (stockActual < 0) {
            throw new IllegalArgumentException("El stock actual no puede ser negativo");
        }
        return ((double) stockActual / stockTotal) < 0.20;
    }

    public int disminuirStock(int stockActual, int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad a disminuir debe ser mayor a cero");
        }
        if (stockActual < cantidad) {
            throw new IllegalArgumentException("Stock insuficiente: stock actual (" + stockActual + ") menor a la cantidad solicitada (" + cantidad + ")");
        }
        return stockActual - cantidad;
    }

    public int aumentarStock(int stockActual, int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad a aumentar debe ser mayor a cero");
        }
        if (stockActual < 0) {
            throw new IllegalArgumentException("El stock actual no puede ser negativo");
        }
        return stockActual + cantidad;
    }

    // ==================== MÉTODOS HELPER PARA VISTA Y NEGOCIO ====================

    @Transactional(readOnly = true)
    public double obtenerPrecioActual(Producto producto) {
        if (producto == null) return 0.0;
        if (producto.getId() != null) {
            try {
                return obtenerPrecioActual(producto.getId());
            } catch (Exception ignored) {
            }
        }
        return producto.getPrecioActual() != null ? producto.getPrecioActual() : 0.0;
    }

    public String obtenerNombreCategoria(Producto producto) {
        if (producto == null) return "Indumentaria";
        if (producto.getSubCategoria() != null && producto.getSubCategoria().getCategoria() != null) {
            return producto.getSubCategoria().getCategoria().getNombre();
        }
        if (producto.getSubCategoria() != null) {
            return producto.getSubCategoria().getNombre();
        }
        return "Indumentaria";
    }

    public int obtenerStock(Producto producto) {
        return (producto != null) ? producto.getStock() : 0;
    }

    public String obtenerImagenUrl(Producto producto) {
        if (producto != null && producto.getImagenes() != null && !producto.getImagenes().isEmpty()) {
            for (Imagen img : producto.getImagenes()) {
                if (img != null && !img.isEliminado() && img.getId() != null) {
                    return "/imagen/" + img.getId();
                }
            }
        }
        return "/shop/img/product/p1.jpg";
    }

    @Transactional(readOnly = true)
    public Producto prepararParaVista(Producto producto) {
        if (producto == null) return null;
        try {
            double precio = obtenerPrecioActual(producto.getId());
            producto.setPrecioActual(precio);
        } catch (Exception ignored) {
            if (producto.getPrecioActual() == null) {
                producto.setPrecioActual(0.0);
            }
        }
        return producto;
    }

    @Transactional(readOnly = true)
    public List<Producto> prepararParaVista(List<Producto> productos) {
        if (productos == null) return java.util.Collections.emptyList();
        productos.forEach(this::prepararParaVista);
        return productos;
    }
}
