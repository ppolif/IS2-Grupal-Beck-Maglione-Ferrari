package com.example.zero.services;

import com.example.zero.entidades.compra.Detalle;
import com.example.zero.entidades.compra.Stock;
import com.example.zero.entidades.producto.Producto;
import com.example.zero.repositories.ProductoRepository;
import com.example.zero.repositories.StockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Servicio de negocio para la gestión, cálculo y auditoría de Stock.
 * Cumple con el rol de Experto en Información para existencias de productos.
 */
@Service
public class StockService {

    private final StockRepository stockRepository;
    private final ProductoRepository productoRepository;

    public StockService(StockRepository stockRepository, ProductoRepository productoRepository) {
        this.stockRepository = stockRepository;
        this.productoRepository = productoRepository;
    }

    /**
     * Calcula el stock actual de un producto a partir del último registro trazable
     * en la tabla Stock (ordenado cronológicamente por la fecha de factura).
     * Si no posee movimientos previos registrados en la tabla Stock, retorna
     * el stock inicial registrado en la entidad Producto o 0 si no tiene existencias.
     *
     * @param productoId ID del producto
     * @return existencias actuales disponibles
     */
    @Transactional(readOnly = true)
    public int calcularStockActual(String productoId) {
        if (productoId == null || productoId.trim().isEmpty()) {
            return 0;
        }

        Optional<Stock> ultimoStock = stockRepository.findUltimoStockPorProducto(productoId.trim());
        if (ultimoStock.isPresent()) {
            return Math.max(0, ultimoStock.get().getCantidadActual());
        }

        return productoRepository.findById(productoId.trim())
                .filter(p -> !p.isEliminado())
                .map(Producto::getStock)
                .orElse(0);
    }

    /**
     * Crea y persiste un nuevo registro histórico de Stock asociado a un Detalle.
     *
     * @param detalle Detalle de factura vinculado al movimiento
     * @param nuevoBalance Balance resultante de existencias tras el movimiento
     * @param observacion Motivo o referencia del movimiento (ej. Egreso por Venta)
     * @return entidad Stock persistida
     */
    @Transactional
    public Stock crearStock(Detalle detalle, int nuevoBalance, String observacion) {
        if (detalle == null) {
            throw new IllegalArgumentException("El detalle de factura no puede ser nulo al registrar stock");
        }
        if (nuevoBalance < 0) {
            throw new IllegalArgumentException("El balance de stock no puede ser negativo");
        }

        Stock stock = Stock.builder()
                .detalle(detalle)
                .cantidadActual(nuevoBalance)
                .observacion(observacion != null ? observacion.trim() : "Movimiento de stock")
                .eliminado(false)
                .build();

        return stockRepository.save(stock);
    }
}