package com.DulceMayor.backend.services.estadistica;

import com.DulceMayor.backend.config.ValidacionException;
import com.DulceMayor.backend.dtos.estadistica.PedidosPorPreventorPorDiaResponse;
import com.DulceMayor.backend.dtos.estadistica.PuntoDiaCantidad;
import com.DulceMayor.backend.dtos.estadistica.SeriePreventorDia;
import com.DulceMayor.backend.dtos.estadistica.StockPorProductoItem;
import com.DulceMayor.backend.dtos.producto.ProductoResponse;
import com.DulceMayor.backend.entities.pedido.EstadoPedido;
import com.DulceMayor.backend.entities.pedido.Pedido;
import com.DulceMayor.backend.entities.persona.Preventor;
import com.DulceMayor.backend.repositories.pedido.PedidoRepository;
import com.DulceMayor.backend.repositories.persona.EmpleadoRepository;
import com.DulceMayor.backend.services.producto.ProductoService;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de solo lectura para la generación de series estadísticas y gráficos gerenciales
 * ({@code /estadisticas/**}) según las secciones 5.8 y 10.1 (regla 6) de {@code GENERAL.md}.
 */
@Service
@Transactional(readOnly = true)
public class EstadisticaLecturaService {

    private final PedidoRepository pedidoRepository;
    private final EmpleadoRepository empleadoRepository;
    private final ProductoService productoService;

    public EstadisticaLecturaService(
            PedidoRepository pedidoRepository,
            EmpleadoRepository empleadoRepository,
            ProductoService productoService) {
        this.pedidoRepository = pedidoRepository;
        this.empleadoRepository = empleadoRepository;
        this.productoService = productoService;
    }

    /**
     * Calcula la serie mensual de cantidad de pedidos por preventor por día
     * para el gráfico de barras de jefatura ({@code GET /estadisticas/pedidos-por-preventor-por-dia}).
     */
    public PedidosPorPreventorPorDiaResponse obtenerPedidosPorPreventorPorDia(Integer anio, Integer mes) {
        if (anio == null || anio < 2000 || anio > 2100) {
            throw new ValidacionException("El año debe estar comprendido entre 2000 y 2100");
        }
        if (mes == null || mes < 1 || mes > 12) {
            throw new ValidacionException("El mes debe estar comprendido entre 1 y 12");
        }

        YearMonth periodo = YearMonth.of(anio, mes);
        List<Preventor> preventoresActivos = empleadoRepository.findAllByTipoAndEliminadoFalse(Preventor.class);
        List<Pedido> todosLosPedidos = pedidoRepository.findAllByEliminadoFalseOrderByFechaHoraCargaDesc();

        Map<String, String> nombresPreventores = new LinkedHashMap<>();
        Map<String, Map<Integer, Integer>> conteoPorPreventorYDia = new LinkedHashMap<>();

        for (Preventor prev : preventoresActivos) {
            String nombreCompleto = (prev.getNombre() + " " + prev.getApellido()).trim();
            nombresPreventores.put(prev.getId(), nombreCompleto);
            conteoPorPreventorYDia.put(prev.getId(), new TreeMap<>());
        }

        for (Pedido pedido : todosLosPedidos) {
            if (pedido.getEstado() == EstadoPedido.ANULADO || pedido.getFechaHoraCarga() == null) {
                continue;
            }
            if (pedido.getPreventor() == null) {
                continue;
            }
            YearMonth ymPedido = YearMonth.from(pedido.getFechaHoraCarga());
            if (!periodo.equals(ymPedido)) {
                continue;
            }

            Preventor prev = pedido.getPreventor();
            String prevId = prev.getId();
            nombresPreventores.putIfAbsent(prevId, (prev.getNombre() + " " + prev.getApellido()).trim());
            Map<Integer, Integer> mapaDias = conteoPorPreventorYDia.computeIfAbsent(prevId, k -> new TreeMap<>());

            int dia = pedido.getFechaHoraCarga().getDayOfMonth();
            mapaDias.merge(dia, 1, Integer::sum);
        }

        List<SeriePreventorDia> series = new ArrayList<>();
        for (Map.Entry<String, Map<Integer, Integer>> entry : conteoPorPreventorYDia.entrySet()) {
            String prevId = entry.getKey();
            String prevNombre = nombresPreventores.getOrDefault(prevId, "Preventor");
            List<PuntoDiaCantidad> puntos = new ArrayList<>();
            for (Map.Entry<Integer, Integer> diaEntry : entry.getValue().entrySet()) {
                puntos.add(new PuntoDiaCantidad(diaEntry.getKey(), diaEntry.getValue()));
            }
            series.add(new SeriePreventorDia(prevId, prevNombre, puntos));
        }

        return new PedidosPorPreventorPorDiaResponse(anio, mes, series);
    }

    /**
     * Obtiene la distribución actual de unidades en stock por producto para el gráfico de torta
     * ({@code GET /estadisticas/stock-por-producto}).
     */
    public List<StockPorProductoItem> obtenerStockPorProducto() {
        List<ProductoResponse> productos = productoService.listar();
        List<StockPorProductoItem> resultado = new ArrayList<>();
        for (ProductoResponse prod : productos) {
            int stock = prod.stockActual() != null ? prod.stockActual() : 0;
            resultado.add(new StockPorProductoItem(
                    prod.id(),
                    prod.nombre(),
                    stock
            ));
        }
        return resultado;
    }
}

