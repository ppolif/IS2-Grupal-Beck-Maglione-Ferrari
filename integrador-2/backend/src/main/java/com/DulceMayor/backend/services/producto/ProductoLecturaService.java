package com.DulceMayor.backend.services.producto;

import com.DulceMayor.backend.dtos.producto.ProductoMapper;
import com.DulceMayor.backend.dtos.producto.ProductoPage;
import com.DulceMayor.backend.dtos.producto.ProductoResponse;
import com.DulceMayor.backend.entities.producto.Producto;
import com.DulceMayor.backend.repositories.producto.ProductoRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de lectura para el catálogo y productos.
 * <p>
 * Conforme a la sección 10.1 de {@code GENERAL.md}, delega las consultas de stock en {@link StockService}
 * y las de precios vigentes en {@link VigenciaPrecioService}, desacoplándose de los repositorios foráneos.
 * </p>
 */
@Service
@Transactional(readOnly = true)
public class ProductoLecturaService {

    private final ProductoRepository productoRepository;
    private final StockService stockService;
    private final VigenciaPrecioService vigenciaPrecioService;
    private final ProductoMapper productoMapper;

    public ProductoLecturaService(
            ProductoRepository productoRepository,
            @Lazy StockService stockService,
            @Lazy VigenciaPrecioService vigenciaPrecioService,
            ProductoMapper productoMapper) {
        this.productoRepository = productoRepository;
        this.stockService = stockService;
        this.vigenciaPrecioService = vigenciaPrecioService;
        this.productoMapper = productoMapper;
    }

    public Integer obtenerStockActual(String productoId) {
        return stockService.obtenerStockActual(productoId);
    }

    public Double obtenerPrecioVigente(String productoId) {
        return vigenciaPrecioService.obtenerPrecioVigente(productoId);
    }

    public ProductoResponse enriquecerResponse(Producto producto) {
        ProductoResponse base = productoMapper.aResponse(producto);

        ///a la base que ignora precio y stock le agregamos justamente esos campos
        Integer stock = obtenerStockActual(producto.getId());
        Double precio = obtenerPrecioVigente(producto.getId());
        return new ProductoResponse(
                base.id(),
                base.codigo(),
                base.nombre(),
                base.descripcion(),
                base.presentacion(),
                base.unidadesPorBulto(),
                base.enOferta(),
                base.imagenId(),
                base.subCategoriaId(),
                base.subCategoriaNombre(),
                base.categoriaId(),
                base.categoriaNombre(),

                //campos faltantes
                precio,
                stock
        );
    }

    public ProductoPage listarPaginado(
            String q,
            String categoriaId,
            String subCategoriaId,
            Boolean enOferta,
            Boolean soloConStock,
            Pageable pageable) {

        Page<Producto> pagina = productoRepository.buscarConFiltros(
                q != null && !q.isBlank() ? q.trim() : null,
                categoriaId != null && !categoriaId.isBlank() ? categoriaId : null,
                subCategoriaId != null && !subCategoriaId.isBlank() ? subCategoriaId : null,
                enOferta,
                soloConStock,
                pageable
        );

        List<ProductoResponse> contenido = new ArrayList<>();

        //vamos a extraer cada producto de la pagina que devuelve el repositorio
        for (Producto p : pagina.getContent()) {

            //convertir los productos de la bd que pasaron los filtros en response
            Integer stock = obtenerStockActual(p.getId());
            Double precio = obtenerPrecioVigente(p.getId());
            ProductoResponse base = productoMapper.aResponse(p);
            contenido.add(new ProductoResponse(
                    base.id(),
                    base.codigo(),
                    base.nombre(),
                    base.descripcion(),
                    base.presentacion(),
                    base.unidadesPorBulto(),
                    base.enOferta(),
                    base.imagenId(),
                    base.subCategoriaId(),
                    base.subCategoriaNombre(),
                    base.categoriaId(),
                    base.categoriaNombre(),
                    precio,
                    stock
            ));
        }

        //devolvemos la página con los metadatos reales calculados por la base de datos
        return new ProductoPage(
                contenido,
                pagina.getNumber(),
                pagina.getSize(),
                pagina.getTotalElements(),
                pagina.getTotalPages()
        );
    }
}

