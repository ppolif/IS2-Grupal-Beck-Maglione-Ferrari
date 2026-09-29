package com.example.zero.services;

import com.example.zero.entidades.compra.Detalle;
import com.example.zero.entidades.compra.Stock;
import com.example.zero.repositories.DetalleRepository;
import com.example.zero.repositories.StockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

/**
 * Servicio de negocio para la gestión, cálculo y auditoría de Stock.
 * Cumple con el rol de Experto en Información para existencias de productos.
 */
@Service
public class StockService {

    private final StockRepository stockRepository;
    private final DetalleRepository detalleRepository;

    public StockService(StockRepository stockRepository, DetalleRepository detalleRepository) {
        this.stockRepository = stockRepository;
        this.detalleRepository = detalleRepository;
    }

    /**
     * Calcula el stock actual de un producto a partir del último registro trazable
     * en la tabla Stock (ordenado cronológicamente por la fecha de factura).
     * Si no posee movimientos previos registrados en la tabla Stock, retorna 0.
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
        return ultimoStock.map(stock -> Math.max(0, stock.getCantidadActual())).orElse(0);
    }

    /**
     * Busca el último registro de Stock asociado a un producto.
     *
     * @param productoId ID del producto
     * @return Optional con la entidad Stock
     */
    @Transactional(readOnly = true)
    public Optional<Stock> buscarStockActual(String productoId) {
        if (productoId == null || productoId.trim().isEmpty()) {
            return Optional.empty();
        }
        return stockRepository.findUltimoStockPorProducto(productoId.trim());
    }

    /**
     * Busca un registro de stock por ID.
     */
    @Transactional(readOnly = true)
    public Optional<Stock> buscarStock(String id) {
        if (id == null || id.trim().isEmpty()) {
            return Optional.empty();
        }
        return stockRepository.findActive(id.trim());
    }

    /**
     * Lista todos los registros de stock activos.
     */
    @Transactional(readOnly = true)
    public List<Stock> listarStock() {
        return stockRepository.findByEliminadoFalse();
    }

    /**
     * Valida los datos requeridos para crear/modificar un stock.
     */
    public void validar(Detalle detalle, int cantidadActual) {
        if (detalle == null) {
            throw new IllegalArgumentException("El detalle de factura no puede ser nulo al registrar stock");
        }
        if (cantidadActual < 0) {
            throw new IllegalArgumentException("El balance de stock no puede ser negativo");
        }
    }

    /**
     * Crea o actualiza el registro histórico de Stock asociado a un Detalle.
     * Si ya existe un registro de Stock vinculado a dicho Detalle, actualiza sus
     * existencias y observación para garantizar idempotencia y evitar violaciones
     * de la restricción UNIQUE sobre detalle_id.
     *
     * @param detalle Detalle de factura vinculado al movimiento
     * @param nuevoBalance Balance resultante de existencias tras el movimiento
     * @param observacion Motivo o referencia del movimiento (ej. Egreso por Venta o Ingreso por Compra)
     * @return entidad Stock persistida
     */
    @Transactional
    public Stock crearStock(Detalle detalle, int nuevoBalance, String observacion) {
        validar(detalle, nuevoBalance);

        Optional<Stock> stockExistente = (detalle.getId() != null)
                ? stockRepository.findByDetalleId(detalle.getId())
                : Optional.empty();

        Stock stock = stockExistente.orElseGet(() -> Stock.builder().detalle(detalle).build());
        stock.setDetalle(detalle);
        stock.setCantidadActual(nuevoBalance);
        stock.setObservacion(observacion != null ? observacion.trim() : "Movimiento de stock");
        stock.setEliminado(false);

        return stockRepository.save(stock);
    }

    @Transactional
    public Stock crearStock(String detalleId, int nuevoBalance, String observacion) {
        if (detalleId == null || detalleId.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del detalle no puede ser nulo o vacío");
        }
        Detalle detalle = detalleRepository.findById(detalleId.trim())
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el detalle con ID: " + detalleId));
        return crearStock(detalle, nuevoBalance, observacion);
    }

    @Transactional
    public Stock modificarStock(String id, int cantidadActual, String observacion) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID de stock no puede ser nulo o vacío");
        }
        if (cantidadActual < 0) {
            throw new IllegalArgumentException("La cantidad actual no puede ser negativa");
        }

        Stock stock = stockRepository.findActive(id.trim())
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el registro de stock con ID: " + id));

        stock.setCantidadActual(cantidadActual);
        if (observacion != null) {
            stock.setObservacion(observacion.trim());
        }
        return stockRepository.save(stock);
    }

    @Transactional
    public void eliminarStock(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID de stock no puede ser nulo o vacío");
        }
        Stock stock = stockRepository.findActive(id.trim())
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el registro de stock con ID: " + id));
        stock.setEliminado(true);
        stockRepository.save(stock);
    }

    // ==================== LÓGICA DE OPERACIONES DE STOCK ====================

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

    public String getEstadoStock(int stock) {
        if (stock > 50) {
            return "BIEN";
        } else if (stock >= 20) {
            return "REGULAR";
        } else {
            return "MALO";
        }
    }

    public int getCantidadFaltanteStock(int stock) {
        return Math.max(0, 50 - stock);
    }

    public String getUrlWhatsAppReposicion(String nombre, String codigo, int stock) {
        int faltante = getCantidadFaltanteStock(stock);
        String mensaje = String.format(
                "Hola! Desde ZERO Tienda Oficial deseamos hacer un pedido de reposición para el producto: %s (Código: %s). Solicitamos %d unidades para alcanzar el nivel óptimo de 50 unidades (Stock actual: %d).",
                nombre != null ? nombre : "Producto",
                codigo != null ? codigo : "",
                faltante,
                stock
        );
        try {
            return "https://web.whatsapp.com/send?phone=5492613072339&text=" + URLEncoder.encode(mensaje, StandardCharsets.UTF_8.toString());
        } catch (Exception e) {
            return "https://web.whatsapp.com/send?phone=5492613072339";
        }
    }
}