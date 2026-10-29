package com.DulceMayor.backend.services.producto;

import com.DulceMayor.backend.config.ConflictoException;
import com.DulceMayor.backend.config.RecursoNoEncontradoException;
import com.DulceMayor.backend.config.ValidacionException;
import com.DulceMayor.backend.dtos.BaseMapper;
import com.DulceMayor.backend.dtos.producto.ProductoCreadoEvent;
import com.DulceMayor.backend.dtos.producto.ProductoMapper;
import com.DulceMayor.backend.dtos.producto.ProductoPage;
import com.DulceMayor.backend.dtos.producto.ProductoRequest;
import com.DulceMayor.backend.dtos.producto.ProductoResponse;
import com.DulceMayor.backend.entities.Imagen;
import com.DulceMayor.backend.entities.producto.Producto;
import com.DulceMayor.backend.entities.producto.SubCategoria;
import com.DulceMayor.backend.repositories.BaseRepository;
import com.DulceMayor.backend.repositories.producto.ProductoRepository;
import java.util.List;

import com.DulceMayor.backend.services.BaseService;
import com.DulceMayor.backend.services.ImagenService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de negocio para la administración del catálogo de productos.
 *
 * <p><b>Patrón GoF aplicado: Template Method (Comportamiento)</b></p>
 * <ul>
 *   <li><b>Participante GoF:</b> ConcreteClass (Clase Concreta).</li>
 *   <li><b>Clase Base / AbstractClass:</b> {@link BaseService}.</li>
 *   <li><b>Operaciones primitivas y Hooks implementados:</b>
 *     <ul>
 *       <li>{@link #prepararRequest(ProductoRequest)}: normaliza código, nombre y cadenas de entrada.</li>
 *       <li>{@link #validarCrear(ProductoRequest)}: valida unicidad de código comercial e integridad referencial de subcategoría e imagen.</li>
 *       <li>{@link #antesDePersistir(Producto, ProductoRequest)}: asigna las asociaciones foráneas hacia {@link SubCategoria} e {@link Imagen}.</li>
 *       <li>{@link #despuesDeCrear(Producto, ProductoRequest)}: hook ejecutado tras la persistencia que actúa como disparador del patrón Observer.</li>
 *       <li>{@link #validarModificar(String, ProductoRequest, Producto)}: valida duplicados excluyendo el propio ID activo.</li>
 *       <li>{@link #actualizarEntidad(ProductoRequest, Producto)}: actualiza los atributos mutables y relaciones del producto.</li>
 *       <li>{@link #aResponse(Producto)}: hook de proyección enriquecido con stock y precio en tiempo real.</li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * <p><b>Patrón GoF aplicado: Observer (Comportamiento)</b></p>
 * <ul>
 *   <li><b>Participante GoF:</b> Subject / ConcreteSubject (Sujeto / Emisor de Eventos).</li>
 *   <li><b>Mecanismo:</b> {@link ApplicationEventPublisher} de Spring Framework.</li>
 *   <li><b>Evento emitido:</b> {@link ProductoCreadoEvent}.</li>
 *   <li><b>Observadores (ConcreteObservers):</b> {@link StockService} y {@link VigenciaPrecioService}.</li>
 *   <li><b>Rol en la arquitectura:</b> Al concretarse el alta de un {@link Producto} en el método hook
 *       {@link #despuesDeCrear(Producto, ProductoRequest)}, se emite el evento de dominio. Esto permite
 *       que el stock inicial y la primera vigencia de precio se creen de forma 100% desacoplada,
 *       sin que {@code ProductoService} conozca ni tenga dependencias directas sobre {@code StockService}
 *       ni {@code VigenciaPrecioService}, evitando dependencias circulares y respetando el principio de
 *       responsabilidad única.</li>
 * </ul>
 */
@Service
public class ProductoService extends BaseService<Producto, ProductoRequest, ProductoResponse> {

    private final ProductoRepository productoRepository;
    private final SubCategoriaService subCategoriaService;
    private final ImagenService imagenService;
    private final ApplicationEventPublisher eventPublisher;
    private final ProductoLecturaService productoLecturaService;

    public ProductoService(
            ProductoRepository productoRepository,
            ProductoMapper productoMapper,
            SubCategoriaService subCategoriaService,
            ImagenService imagenService,
            ApplicationEventPublisher eventPublisher,
            ProductoLecturaService productoLecturaService) {
        super(productoRepository, productoMapper, "Producto");
        this.productoRepository = productoRepository;
        this.subCategoriaService = subCategoriaService;
        this.imagenService = imagenService;
        this.eventPublisher = eventPublisher;
        this.productoLecturaService = productoLecturaService;
    }

    @Override
    protected ProductoRequest prepararRequest(ProductoRequest request) {
        if (request == null) {
            return null;
        }
        return new ProductoRequest(
                request.codigo() != null ? request.codigo().trim() : null,
                request.nombre() != null ? request.nombre().trim() : null,
                request.descripcion() != null ? request.descripcion().trim() : null,
                request.presentacion() != null ? request.presentacion().trim() : null,
                request.unidadesPorBulto(),
                request.enOferta() != null ? request.enOferta() : false,
                request.imagenId() != null && !request.imagenId().isBlank() ? request.imagenId().trim() : null,
                request.subCategoriaId() != null ? request.subCategoriaId().trim() : null,
                request.precioInicial(),
                request.stockInicial()
        );
    }

    @Override
    protected void validarCrear(ProductoRequest request) {
        validarCamposObligatorios(request);
        if (productoRepository.existsByCodigoIgnoreCaseAndEliminadoFalse(request.codigo())) {
            throw new ConflictoException(
                    "DUPLICADO",
                    "Ya existe un producto con el código '" + request.codigo() + "'"
            );
        }

        //corroboramos existencia y sino damos error
        subCategoriaService.obtenerEntidad(request.subCategoriaId());
        if (request.imagenId() != null) {
            imagenService.obtenerEntidad(request.imagenId());
        }
    }

    @Override
    protected void validarModificar(String id, ProductoRequest request, Producto existente) {
        validarCamposObligatorios(request);
        if (productoRepository.existsByCodigoIgnoreCaseAndIdNotAndEliminadoFalse(request.codigo(), id)) {
            throw new ConflictoException(
                    "DUPLICADO",
                    "Ya existe otro producto con el código '" + request.codigo() + "'"
            );
        }
        subCategoriaService.obtenerEntidad(request.subCategoriaId());
        if (request.imagenId() != null) {
            imagenService.obtenerEntidad(request.imagenId());
        }
    }

    @Override
    protected void antesDePersistir(Producto entidad, ProductoRequest request) {
        SubCategoria subCategoria = subCategoriaService.obtenerEntidad(request.subCategoriaId());
        entidad.setSubCategoria(subCategoria);

        if (request.imagenId() != null) {
            Imagen imagen = imagenService.obtenerEntidad(request.imagenId());
            entidad.setImagen(imagen);
        } else {
            entidad.setImagen(null);
        }
    }

    @Override
    protected void despuesDeCrear(Producto entidad, ProductoRequest request) {
        // Notifica de forma desacoplada la creación del producto para inicializar precio y stock
        eventPublisher.publishEvent(new ProductoCreadoEvent(
                entidad.getId(),
                request.precioInicial(),
                request.stockInicial()
        ));
    }

    @Override
    protected ProductoResponse aResponse(Producto entidad) {
        ///le agregamos la info que falta desde productoLecturaService
        return productoLecturaService.enriquecerResponse(entidad);
    }

    @Override
    protected List<Producto> buscarEntidadesActivas() {
        return productoRepository.findAllByEliminadoFalseOrderByNombreAsc();
    }

    ///le delegamos la "verdadera busqueda" con filtros a la clase de producto que es de solo lectura
    @Transactional(readOnly = true)
    public ProductoPage listarPaginado(
            String q,
            String categoriaId,
            String subCategoriaId,
            Boolean enOferta,
            Boolean soloConStock,
            Pageable pageable) {
        return productoLecturaService.listarPaginado(q, categoriaId, subCategoriaId, enOferta, soloConStock, pageable);
    }

    @Transactional(readOnly = true)
    public ProductoResponse buscarPorCodigo(String codigo) {
        Producto producto = productoRepository.findByCodigoIgnoreCaseAndEliminadoFalse(codigo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado con código: " + codigo));
        return aResponse(producto);
    }

    //lo unico que no es obligatorio es la imagen
    private void validarCamposObligatorios(ProductoRequest request) {
        if (request == null) {
            throw new ValidacionException("Los datos del producto son obligatorios");
        }
        if (request.codigo() == null || request.codigo().isBlank()) {
            throw new ValidacionException("El código del producto es obligatorio");
        }
        if (request.nombre() == null || request.nombre().isBlank()) {
            throw new ValidacionException("El nombre del producto es obligatorio");
        }
        if (request.presentacion() == null || request.presentacion().isBlank()) {
            throw new ValidacionException("La presentación del producto es obligatoria");
        }
        if (request.unidadesPorBulto() == null || request.unidadesPorBulto() < 1) {
            throw new ValidacionException("Las unidades por bulto deben ser al menos 1");
        }
        if (request.subCategoriaId() == null || request.subCategoriaId().isBlank()) {
            throw new ValidacionException("El id de la subcategoría es obligatorio");
        }
    }
}

