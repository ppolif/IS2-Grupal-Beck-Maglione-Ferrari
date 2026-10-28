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
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProductoService {

    private static final Logger logger = LoggerFactory.getLogger(ProductoService.class);

    @Value("${inflacion.porcentaje-bimestral:8.0}")
    private double porcentajeBimestral = 8.0;

    @Value("${inflacion.batch-size:100}")
    private int defaultBatchSize = 100;

    @PersistenceContext
    private EntityManager entityManager;

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
        return crearProductoInterno(codigo, nombre, descripcion, talle, subCategoriaId, precioInicial, enOferta, null);
    }

    @Transactional
    public Producto crearProducto(String codigo, String nombre, String descripcion, String talle,
                                  String subCategoriaId, double precioInicial, boolean enOferta,
                                  MultipartFile archivoImagen) {
        if (archivoImagen == null || archivoImagen.isEmpty()) {
            throw new IllegalArgumentException("La imagen del producto es obligatoria");
        }
        return crearProductoInterno(codigo, nombre, descripcion, talle, subCategoriaId, precioInicial, enOferta, archivoImagen);
    }

    private Producto crearProductoInterno(String codigo, String nombre, String descripcion, String talle,
                                          String subCategoriaId, double precioInicial, boolean enOferta,
                                          MultipartFile archivoImagen) {
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

        // Patron builder
        Producto producto = Producto.builder()
                .codigo(codigoLimpio)
                .nombre(nombre.trim())
                .descripcion(descripcion != null ? descripcion.trim() : null)
                .talle(talle != null ? talle.trim() : null)
                .subCategoria(subCategoria)
                .enOferta(enOferta)
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

    /**
     * Tarea programada para actualización bimestral de precios por inflación en Argentina.
     * Se ejecuta automáticamente cada 2 meses según la expresión cron configurada.
     */
    @Scheduled(cron = "${inflacion.cron:0 0 2 1 */2 ?}")
    public void actualizarPreciosPorInflacionProgramado() {
        logger.info("Iniciando tarea programada: Actualización bimestral de precios por inflación en Argentina ({}%).", porcentajeBimestral);
        try {
            int actualizados = aplicarAumentoGeneralPorInflacion(porcentajeBimestral, defaultBatchSize);
            logger.info("Tarea programada de inflación finalizada exitosamente. Total de productos actualizados: {}", actualizados);
        } catch (Exception e) {
            logger.error("Error al ejecutar tarea programada de actualización de precios por inflación: {}", e.getMessage(), e);
        }
    }

    /**
     * Aplica aumento general de precios por inflación utilizando paginación en base de datos.
     * Procesa lotes para soportar catálogos con volúmenes masivos de datos sin agotar la memoria.
     *
     * @param porcentaje Porcentaje de incremento (debe ser mayor a 0)
     * @param pageSize Tamaño de cada página / lote de productos
     * @return Cantidad total de productos actualizados
     */
    @Transactional
    public int aplicarAumentoGeneralPorInflacion(double porcentaje, int pageSize) {
        if (porcentaje <= 0) {
            throw new IllegalArgumentException("El porcentaje de aumento debe ser mayor a cero");
        }
        int tamanoPagina = (pageSize > 0) ? pageSize : defaultBatchSize;
        int pageNumber = 0;
        int totalActualizados = 0;
        Page<Producto> pagina;

        logger.info("Iniciando actualización masiva de precios por inflación: +{}% en lotes de {}", porcentaje, tamanoPagina);

        do {
            Pageable pageable = PageRequest.of(pageNumber, tamanoPagina, Sort.by("id").ascending());
            pagina = productoRepository.findByEliminadoFalse(pageable);
            List<Producto> lote = (pagina != null) ? pagina.getContent() : List.of();

            int totalPaginas = (pagina != null && pagina.getTotalPages() > 0) ? pagina.getTotalPages() : 1;
            logger.info("Procesando lote de precios - Página {} de {} ({} productos)",
                    pageNumber + 1, totalPaginas, lote.size());

            for (Producto producto : lote) {
                if (producto != null && producto.getId() != null) {
                    try {
                        aplicarAumentoPorInflacion(producto.getId(), porcentaje);
                        totalActualizados++;
                    } catch (Exception e) {
                        logger.warn("No se pudo actualizar el precio del producto ID {}: {}", producto.getId(), e.getMessage());
                    }
                }
            }

            // Liberar memoria del contexto de persistencia de Hibernate para no acumular entidades en memoria
            if (entityManager != null) {
                entityManager.flush();
                entityManager.clear();
            }

            pageNumber++;
        } while (pagina != null && pagina.hasNext());

        logger.info("Actualización masiva de precios finalizada. Total actualizados: {}", totalActualizados);
        return totalActualizados;
    }

    @Transactional
    public int aplicarAumentoGeneralPorInflacion(double porcentaje) {
        return aplicarAumentoGeneralPorInflacion(porcentaje, defaultBatchSize);
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void setPorcentajeBimestral(double porcentajeBimestral) {
        this.porcentajeBimestral = porcentajeBimestral;
    }

    public void setDefaultBatchSize(int defaultBatchSize) {
        this.defaultBatchSize = defaultBatchSize;
    }

    // ==================== MÉTODOS HELPER PARA VISTA Y NEGOCIO ====================

    @Transactional(readOnly = true)
    public double obtenerPrecioActual(Producto producto) {
        if (producto == null || producto.getId() == null) return 0.0;
        try {
            return vigenciaPrecioService.obtenerPrecioActual(producto.getId());
        } catch (Exception ignored) {
            return 0.0;
        }
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
}
