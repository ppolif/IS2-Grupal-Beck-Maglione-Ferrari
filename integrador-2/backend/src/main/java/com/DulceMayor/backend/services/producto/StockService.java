package com.DulceMayor.backend.services.producto;

import com.DulceMayor.backend.config.ConflictoException;
import com.DulceMayor.backend.config.RecursoNoEncontradoException;
import com.DulceMayor.backend.dtos.producto.ProductoCreadoEvent;
import com.DulceMayor.backend.dtos.producto.StockAjusteRequest;
import com.DulceMayor.backend.dtos.producto.StockMapper;
import com.DulceMayor.backend.dtos.producto.StockPage;
import com.DulceMayor.backend.dtos.producto.StockReposicionRequest;
import com.DulceMayor.backend.dtos.producto.StockResponse;
import com.DulceMayor.backend.entities.facturacion.DetalleFactura;
import com.DulceMayor.backend.entities.producto.Producto;
import com.DulceMayor.backend.entities.producto.Stock;
import com.DulceMayor.backend.repositories.producto.StockRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de negocio para la administración del inventario y movimientos de stock.
 * <p>
 * Implementa la lógica de ledger/auditoría inmutable según las especificaciones actualizadas:
 * {@code Stock} no tiene atributo {@code producto}, sino que se relaciona únicamente con {@code DetalleFactura}
 * (navegando al producto desde allí).
 * Cada cambio en la existencia física (ingreso inicial, reposición, ajuste o egreso por venta)
 * inserta un nuevo registro en {@link Stock} con la nueva cantidad disponible, la marca temporal
 * y el detalle descriptivo de la operación.
 * </p>
 *
 * <p><b>Patrón GoF aplicado: Observer (Comportamiento)</b></p>
 * <ul>
 *   <li><b>Participante GoF:</b> ConcreteObserver (Observador Concreto).</li>
 *   <li><b>Sujeto / Publisher:</b> {@link ProductoService} (dispara {@link ProductoCreadoEvent}).</li>
 *   <li><b>Implementación:</b> Mediante {@link EventListener} en {@link #alCrearProducto(ProductoCreadoEvent)}.
 *       Cuando el sujeto publica el evento de creación de un producto, este observador reacciona de forma
 *       desacoplada registrando el primer movimiento de stock ("Ingreso - Stock inicial") con la cantidad
 *       configurada, sin que {@link ProductoService} tenga acoplamiento directo sobre este servicio.</li>
 * </ul>
 */
@Service
public class StockService {

    private final StockRepository stockRepository;
    private final StockMapper stockMapper;
    private final ProductoService productoService;

    public StockService(
            StockRepository stockRepository,
            StockMapper stockMapper,
            ProductoService productoService) {
        this.stockRepository = stockRepository;
        this.stockMapper = stockMapper;
        this.productoService = productoService;
    }

    /**
     * Manejador del evento de dominio emitido por el sujeto (Observer).
     * Registra el movimiento inicial de stock al crearse un producto.
     */
    @EventListener
    @Transactional
    public void alCrearProducto(ProductoCreadoEvent event) {
        if (event != null) {
            Producto producto = productoService.obtenerEntidad(event.productoId());
            int inicial = event.stockInicial() != null ? event.stockInicial() : 0;
            DetalleFactura detalleInicial = DetalleFactura.builder()
                    .producto(producto)
                    .cantidad(inicial)
                    .precioUnitario(event.precioInicial() != null ? event.precioInicial() : 0.0)
                    .subtotal(0.0)
                    .eliminado(false)
                    .build();
            Stock stockInicial = Stock.builder()
                    .cantidadActual(inicial)
                    .fechaHora(LocalDateTime.now())
                    .observacion("Ingreso - Stock inicial al crear el producto")
                    .detalleFactura(detalleInicial)
                    .eliminado(false)
                    .build();
            stockRepository.save(stockInicial);
        }
    }

    /**
     * Consulta el estado de stock actual a partir de su último movimiento registrado.
     */
    @Transactional(readOnly = true)
    public StockResponse buscarPorProducto(String productoId) {
        productoService.obtenerEntidad(productoId);
        Stock ultimoStock = obtenerEntidadPorProducto(productoId);
        return stockMapper.aResponse(ultimoStock);
    }

    /**
     * Registra un nuevo movimiento de ajuste de inventario (físico/merma).
     * Inserta un nuevo registro inmutable en el historial con la nueva cantidad y la observación correspondiente.
     */
    @Transactional
    public StockResponse modificarStock(String productoId, StockAjusteRequest request) {
        Producto producto = productoService.obtenerEntidad(productoId);
        Optional<Stock> ultimoOpt = stockRepository
                .findFirstByDetalleFacturaProductoIdAndEliminadoFalseOrderByFechaHoraDescIdDesc(productoId);

        int anterior = ultimoOpt.map(Stock::getCantidadActual).orElse(0);
        String observacion;
        if (request.observacion() != null && !request.observacion().isBlank()) {
            observacion = request.observacion().trim();
        } else {
            observacion = request.cantidadActual() >= anterior
                    ? "Ingreso - Ajuste de inventario"
                    : "Egreso - Ajuste de inventario";
        }

        DetalleFactura detalleAjuste = DetalleFactura.builder()
                .producto(producto)
                .cantidad(Math.abs(request.cantidadActual() - anterior))
                .precioUnitario(0.0)
                .subtotal(0.0)
                .eliminado(false)
                .build();

        Stock nuevoMovimiento = Stock.builder()
                .cantidadActual(request.cantidadActual())
                .fechaHora(LocalDateTime.now())
                .observacion(observacion)
                .detalleFactura(detalleAjuste)
                .eliminado(false)
                .build();

        Stock guardado = stockRepository.save(nuevoMovimiento);
        return stockMapper.aResponse(guardado);
    }

    /**
     * Registra un nuevo movimiento de reposición física de mercadería (Ingreso).
     * Suma la cantidad recibida a la última cantidad disponible e inserta el nuevo registro en el historial.
     */
    @Transactional
    public StockResponse reponerStock(String productoId, StockReposicionRequest request) {
        Producto producto = productoService.obtenerEntidad(productoId);
        int cantidadActualPrevia = stockRepository
                .findFirstByDetalleFacturaProductoIdAndEliminadoFalseOrderByFechaHoraDescIdDesc(productoId)
                .map(Stock::getCantidadActual)
                .orElse(0);

        int nuevaCantidad = cantidadActualPrevia + request.cantidad();
        String observacion = (request.observacion() != null && !request.observacion().isBlank())
                ? request.observacion().trim()
                : "Ingreso - Reposición de mercadería";

        DetalleFactura detalleReposicion = DetalleFactura.builder()
                .producto(producto)
                .cantidad(request.cantidad())
                .precioUnitario(0.0)
                .subtotal(0.0)
                .eliminado(false)
                .build();

        Stock nuevoMovimiento = Stock.builder()
                .cantidadActual(nuevaCantidad)
                .fechaHora(LocalDateTime.now())
                .observacion(observacion)
                .detalleFactura(detalleReposicion)
                .eliminado(false)
                .build();

        Stock guardado = stockRepository.save(nuevoMovimiento);
        return stockMapper.aResponse(guardado);
    }

    /**
     * Consulta paginada del historial de movimientos de stock con filtros.
     */
    @Transactional(readOnly = true)
    public StockPage listarStock(String q, Boolean soloSinStock, Pageable pageable) {
        Page<Stock> pagina = stockRepository.listarConFiltros(
                q != null && !q.isBlank() ? q.trim() : null,
                soloSinStock,
                pageable
        );
        return new StockPage(
                stockMapper.aResponseList(pagina.getContent()),
                pagina.getNumber(),
                pagina.getSize(),
                pagina.getTotalElements(),
                pagina.getTotalPages()
        );
    }

    /**
     * Obtiene la cantidad actual disponible de stock para un producto.
     */
    @Transactional(readOnly = true)
    public Integer obtenerStockActual(String productoId) {
        return stockRepository
                .findFirstByDetalleFacturaProductoIdAndEliminadoFalseOrderByFechaHoraDescIdDesc(productoId)
                .map(Stock::getCantidadActual)
                .orElse(0);
    }

    /**
     * Comprueba si el stock actual disponible cubre la cantidad solicitada.
     */
    @Transactional(readOnly = true)
    public boolean hayStock(String productoId, int cantidad) {
        return stockRepository
                .findFirstByDetalleFacturaProductoIdAndEliminadoFalseOrderByFechaHoraDescIdDesc(productoId)
                .map(s -> s.getCantidadActual() >= cantidad)
                .orElse(false);
    }

    /**
     * Registra un egreso de stock por venta (descuento atómico).
     * Valida que la existencia sea suficiente e inserta el nuevo movimiento en el historial indicando el egreso.
     * En el modelo UML, este movimiento se asocia a {@link DetalleFactura}.
     */
    @Transactional
    public void descontar(String productoId, int cantidad) {
        descontar(productoId, cantidad, null);
    }

    /**
     * Registra un egreso de stock por venta vinculándolo explícitamente a la línea {@link DetalleFactura}.
     */
    @Transactional
    public void descontar(String productoId, int cantidad, DetalleFactura detalleFactura) {
        Stock ultimoStock = obtenerEntidadPorProducto(productoId);
        if (!hayStock(productoId, cantidad)) {
            throw new ConflictoException(
                    "STOCK_INSUFICIENTE",
                    "Stock insuficiente. Disponible: " + ultimoStock.getCantidadActual() + ", Requerido: " + cantidad
            );
        }
        int nuevaCantidad = ultimoStock.getCantidadActual() - cantidad;

        DetalleFactura detalleAsociado = detalleFactura;
        if (detalleAsociado == null) {
            Producto producto = ultimoStock.getDetalleFactura() != null && ultimoStock.getDetalleFactura().getProducto() != null
                    ? ultimoStock.getDetalleFactura().getProducto()
                    : productoService.obtenerEntidad(productoId);
            detalleAsociado = DetalleFactura.builder()
                    .producto(producto)
                    .cantidad(cantidad)
                    .precioUnitario(0.0)
                    .subtotal(0.0)
                    .eliminado(false)
                    .build();
        }

        Stock egreso = Stock.builder()
                .cantidadActual(nuevaCantidad)
                .fechaHora(LocalDateTime.now())
                .observacion("Egreso - Descuento por venta")
                .detalleFactura(detalleAsociado)
                .eliminado(false)
                .build();

        stockRepository.save(egreso);
    }

    /**
     * Obtiene la entidad del último registro de stock (o lanza 404 si no existe).
     */
    public Stock obtenerEntidadPorProducto(String productoId) {
        return stockRepository
                .findFirstByDetalleFacturaProductoIdAndEliminadoFalseOrderByFechaHoraDescIdDesc(productoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Stock no encontrado para el producto con id: " + productoId));
    }
}
