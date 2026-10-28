package com.DulceMayor.backend.services;

import com.DulceMayor.backend.dtos.ProductoMapper;
import com.DulceMayor.backend.dtos.ProductoPage;
import com.DulceMayor.backend.dtos.ProductoResponse;
import com.DulceMayor.backend.entities.Producto;
import com.DulceMayor.backend.entities.Stock;
import com.DulceMayor.backend.entities.VigenciaPrecio;
import com.DulceMayor.backend.repositories.ProductoRepository;
import com.DulceMayor.backend.repositories.StockRepository;
import com.DulceMayor.backend.repositories.VigenciaPrecioRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de lectura para el catálogo y productos.
 * <p>
 * Conforme a la sección 10.1 (regla 6) de GENERAL.md y la exención en ArquitecturaTests,
 * las clases {@code *LecturaService} son de solo lectura y pueden consultar múltiples repositorios
 * para optimizar proyecciones con stock y precios en tiempo real.
 * </p>
 */
@Service
@Transactional(readOnly = true)
public class ProductoLecturaService {

    private final ProductoRepository productoRepository;
    private final StockRepository stockRepository;
    private final VigenciaPrecioRepository vigenciaPrecioRepository;
    private final ProductoMapper productoMapper;

    public ProductoLecturaService(
            ProductoRepository productoRepository,
            StockRepository stockRepository,
            VigenciaPrecioRepository vigenciaPrecioRepository,
            ProductoMapper productoMapper) {
        this.productoRepository = productoRepository;
        this.stockRepository = stockRepository;
        this.vigenciaPrecioRepository = vigenciaPrecioRepository;
        this.productoMapper = productoMapper;
    }

    public Integer obtenerStockActual(String productoId) {
        // En el nuevo modelo UML, Stock navega al producto a través de DetalleFactura.
        // Dado que DetalleFactura aún no está implementada, se consulta el último registro disponible:
        return stockRepository.findFirstByEliminadoFalseOrderByFechaHoraDescIdDesc()
                .map(Stock::getCantidadActual)
                .orElse(0);
    }

    public Double obtenerPrecioVigente(String productoId) {
        return vigenciaPrecioRepository.findByProductoIdAndFechaHastaIsNullAndEliminadoFalse(productoId)
                .map(VigenciaPrecio::getPrecio)
                .orElse(null);
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

