package com.DulceMayor.backend.services.reporte;

import com.DulceMayor.backend.config.ValidacionException;
import com.DulceMayor.backend.dtos.facturacion.FacturaResponse;
import com.DulceMayor.backend.dtos.reporte.ColumnaReporte;
import com.DulceMayor.backend.dtos.reporte.ReporteTabular;
import com.DulceMayor.backend.entities.contacto.Contacto;
import com.DulceMayor.backend.entities.contacto.ContactoCorreoElectronico;
import com.DulceMayor.backend.entities.contacto.ContactoTelefonico;
import com.DulceMayor.backend.entities.direccion.Departamento;
import com.DulceMayor.backend.entities.direccion.Direccion;
import com.DulceMayor.backend.entities.direccion.Localidad;
import com.DulceMayor.backend.entities.facturacion.EstadoFactura;
import com.DulceMayor.backend.entities.facturacion.Factura;
import com.DulceMayor.backend.entities.pedido.DetallePedido;
import com.DulceMayor.backend.entities.pedido.EstadoPedido;
import com.DulceMayor.backend.entities.pedido.Pedido;
import com.DulceMayor.backend.entities.persona.Minorista;
import com.DulceMayor.backend.entities.persona.Preventor;
import com.DulceMayor.backend.entities.persona.PuntoDeVenta;
import com.DulceMayor.backend.entities.producto.Producto;
import com.DulceMayor.backend.repositories.facturacion.FacturaRepository;
import com.DulceMayor.backend.repositories.pedido.PedidoRepository;
import com.DulceMayor.backend.repositories.persona.EmpleadoRepository;
import com.DulceMayor.backend.repositories.persona.MinoristaRepository;
import com.DulceMayor.backend.repositories.persona.PuntoDeVentaRepository;
import com.DulceMayor.backend.services.facturacion.FacturaLecturaService;
import com.DulceMayor.backend.services.reporte.exporter.ReporteExporter;
import com.DulceMayor.backend.services.reporte.exporter.ReporteExporterFactory;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de solo lectura para la generación de los 6 reportes gerenciales y operativos
 * ({@code /reportes/**}) y su exportación mediante {@link ReporteExporterFactory}
 * (secciones 5.7, 8 y 10.1 regla 6 de {@code GENERAL.md}).
 */
@Service
@Transactional(readOnly = true)
public class ReporteLecturaService {

    private final FacturaRepository facturaRepository;
    private final FacturaLecturaService facturaLecturaService;
    private final PedidoRepository pedidoRepository;
    private final MinoristaRepository minoristaRepository;
    private final PuntoDeVentaRepository puntoDeVentaRepository;
    private final EmpleadoRepository empleadoRepository;
    private final ReporteExporterFactory reporteExporterFactory;

    public ReporteLecturaService(
            FacturaRepository facturaRepository,
            FacturaLecturaService facturaLecturaService,
            PedidoRepository pedidoRepository,
            MinoristaRepository minoristaRepository,
            PuntoDeVentaRepository puntoDeVentaRepository,
            EmpleadoRepository empleadoRepository,
            ReporteExporterFactory reporteExporterFactory) {
        this.facturaRepository = facturaRepository;
        this.facturaLecturaService = facturaLecturaService;
        this.pedidoRepository = pedidoRepository;
        this.minoristaRepository = minoristaRepository;
        this.puntoDeVentaRepository = puntoDeVentaRepository;
        this.empleadoRepository = empleadoRepository;
        this.reporteExporterFactory = reporteExporterFactory;
    }

    /**
     * Genera el reporte tabular de montos facturados en un período ({@code GET /reportes/facturacion}).
     */
    public ReporteTabular generarReporteFacturacion(LocalDate desde, LocalDate hasta) {
        validarRangoFechas(desde, hasta);
        List<ColumnaReporte> columnas = List.of(
                new ColumnaReporte("numeroFactura", "Nro. Factura"),
                new ColumnaReporte("fecha", "Fecha"),
                new ColumnaReporte("comercio", "Comercio"),
                new ColumnaReporte("minorista", "Cliente Minorista"),
                new ColumnaReporte("estado", "Estado"),
                new ColumnaReporte("total", "Monto Total"),
                new ColumnaReporte("totalPagado", "Total Pagado"),
                new ColumnaReporte("saldo", "Saldo Pendiente")
        );

        List<Factura> facturas = facturaRepository.findAllByEliminadoFalseOrderByFechaFacturaDesc();
        List<Map<String, Object>> filas = new ArrayList<>();

        for (Factura f : facturas) {
            LocalDate fecha = f.getFechaFactura() != null ? f.getFechaFactura().toLocalDate() : null;
            if (!estaEnRango(fecha, desde, hasta)) {
                continue;
            }
            FacturaResponse enriquecida = facturaLecturaService.enriquecerResponse(f);
            String comercio = "-";
            String minoristaNombre = "-";
            if (f.getPedido() != null && f.getPedido().getPuntoDeVenta() != null) {
                PuntoDeVenta pdv = f.getPedido().getPuntoDeVenta();
                comercio = pdv.getNombreComercio() != null ? pdv.getNombreComercio() : "-";
                if (pdv.getMinorista() != null) {
                    minoristaNombre = (pdv.getMinorista().getNombre() + " " + pdv.getMinorista().getApellido()).trim();
                }
            }

            Map<String, Object> fila = new LinkedHashMap<>();
            fila.put("numeroFactura", f.getNumeroFactura());
            fila.put("fecha", fecha != null ? fecha.toString() : "-");
            fila.put("comercio", comercio);
            fila.put("minorista", minoristaNombre);
            fila.put("estado", f.getEstado() != null ? f.getEstado().name() : "-");
            fila.put("total", f.getTotal());
            fila.put("totalPagado", enriquecida.totalPagado());
            fila.put("saldo", enriquecida.saldo());
            filas.add(fila);
        }

        return new ReporteTabular(
                "Reporte de Montos Facturados",
                LocalDateTime.now(),
                desde,
                hasta,
                columnas,
                filas
        );
    }

    /**
     * Genera el reporte tabular de clientes minoristas y sus puntos de venta ({@code GET /reportes/minoristas}).
     */
    public ReporteTabular generarReporteMinoristas(LocalDate desde, LocalDate hasta) {
        validarRangoFechas(desde, hasta);
        List<ColumnaReporte> columnas = List.of(
                new ColumnaReporte("documento", "Documento"),
                new ColumnaReporte("minorista", "Cliente Minorista"),
                new ColumnaReporte("email", "Correo Electrónico"),
                new ColumnaReporte("telefono", "Teléfono"),
                new ColumnaReporte("cantidadComercios", "Cant. Comercios"),
                new ColumnaReporte("puntosDeVenta", "Puntos de Venta")
        );

        List<Minorista> minoristas = minoristaRepository.findAllByEliminadoFalse();
        List<Map<String, Object>> filas = new ArrayList<>();

        for (Minorista m : minoristas) {
            List<PuntoDeVenta> comercios = puntoDeVentaRepository.findAllByMinoristaIdAndEliminadoFalse(m.getId());
            String nombresComercios = comercios.isEmpty()
                    ? "Sin comercios"
                    : comercios.stream()
                            .map(PuntoDeVenta::getNombreComercio)
                            .collect(Collectors.joining(", "));

            Map<String, Object> fila = new LinkedHashMap<>();
            fila.put("documento", m.getTipoDocumento() + " " + m.getNumeroDocumento());
            fila.put("minorista", (m.getNombre() + " " + m.getApellido()).trim());
            fila.put("email", extraerEmail(m));
            fila.put("telefono", extraerTelefono(m));
            fila.put("cantidadComercios", comercios.size());
            fila.put("puntosDeVenta", nombresComercios);
            filas.add(fila);
        }

        return new ReporteTabular(
                "Reporte de Clientes Minoristas y Puntos de Venta",
                LocalDateTime.now(),
                desde,
                hasta,
                columnas,
                filas
        );
    }

    /**
     * Genera el reporte tabular de pedidos realizados y su estado de envío ({@code GET /reportes/pedidos}).
     */
    public ReporteTabular generarReportePedidos(LocalDate desde, LocalDate hasta, String estadoStr) {
        validarRangoFechas(desde, hasta);
        EstadoPedido estadoFiltro = parsearEstadoPedido(estadoStr);

        List<ColumnaReporte> columnas = List.of(
                new ColumnaReporte("numeroPedido", "Nro. Pedido"),
                new ColumnaReporte("fecha", "Fecha Carga"),
                new ColumnaReporte("comercio", "Punto de Venta"),
                new ColumnaReporte("preventor", "Preventor"),
                new ColumnaReporte("distribuidor", "Distribuidor"),
                new ColumnaReporte("estado", "Estado"),
                new ColumnaReporte("subtotal", "Subtotal"),
                new ColumnaReporte("descuento", "Descuento"),
                new ColumnaReporte("total", "Total")
        );

        List<Pedido> pedidos = pedidoRepository.findAllByEliminadoFalseOrderByFechaHoraCargaDesc();
        List<Map<String, Object>> filas = new ArrayList<>();

        for (Pedido p : pedidos) {
            LocalDate fecha = p.getFechaHoraCarga() != null ? p.getFechaHoraCarga().toLocalDate() : null;
            if (!estaEnRango(fecha, desde, hasta)) {
                continue;
            }
            if (estadoFiltro != null && p.getEstado() != estadoFiltro) {
                continue;
            }

            String comercio = p.getPuntoDeVenta() != null ? p.getPuntoDeVenta().getNombreComercio() : "-";
            String preventor = p.getPreventor() != null
                    ? (p.getPreventor().getNombre() + " " + p.getPreventor().getApellido()).trim()
                    : "Web / Autogestión";
            String distribuidor = p.getDistribuidor() != null
                    ? (p.getDistribuidor().getNombre() + " " + p.getDistribuidor().getApellido()).trim()
                    : "Sin asignar";

            Map<String, Object> fila = new LinkedHashMap<>();
            fila.put("numeroPedido", p.getNumeroPedido());
            fila.put("fecha", fecha != null ? fecha.toString() : "-");
            fila.put("comercio", comercio);
            fila.put("preventor", preventor);
            fila.put("distribuidor", distribuidor);
            fila.put("estado", p.getEstado() != null ? p.getEstado().name() : "-");
            fila.put("subtotal", p.getSubtotal());
            fila.put("descuento", p.getDescuento());
            fila.put("total", p.getTotal());
            filas.add(fila);
        }

        return new ReporteTabular(
                "Reporte de Pedidos y Estados de Envío",
                LocalDateTime.now(),
                desde,
                hasta,
                columnas,
                filas
        );
    }

    /**
     * Genera el ranking de golosinas más vendidas en el período ({@code GET /reportes/productos-mas-vendidos}).
     */
    public ReporteTabular generarReporteProductosMasVendidos(LocalDate desde, LocalDate hasta, Integer limite) {
        validarRangoFechas(desde, hasta);
        int limiteEfectivo = (limite != null && limite > 0) ? limite : 10;

        List<ColumnaReporte> columnas = List.of(
                new ColumnaReporte("codigo", "Código"),
                new ColumnaReporte("producto", "Producto"),
                new ColumnaReporte("presentacion", "Presentación"),
                new ColumnaReporte("unidadesVendidas", "Unidades Vendidas"),
                new ColumnaReporte("montoTotal", "Monto Total Vendido")
        );

        List<Pedido> pedidos = pedidoRepository.findAllByEliminadoFalseOrderByFechaHoraCargaDesc();
        Map<String, AcumuladorProducto> acumuladoPorProducto = new LinkedHashMap<>();

        for (Pedido p : pedidos) {
            if (p.getEstado() == EstadoPedido.ANULADO) {
                continue;
            }
            LocalDate fecha = p.getFechaHoraCarga() != null ? p.getFechaHoraCarga().toLocalDate() : null;
            if (!estaEnRango(fecha, desde, hasta) || p.getDetalles() == null) {
                continue;
            }
            for (DetallePedido det : p.getDetalles()) {
                Producto prod = det.getProducto();
                if (prod == null) {
                    continue;
                }
                AcumuladorProducto acc = acumuladoPorProducto.computeIfAbsent(
                        prod.getId(),
                        id -> new AcumuladorProducto(prod.getCodigo(), prod.getNombre(), prod.getPresentacion())
                );
                acc.unidadesVendidas += det.getCantidad();
                acc.montoTotal += det.getSubtotal();
            }
        }

        List<AcumuladorProducto> ordenados = acumuladoPorProducto.values().stream()
                .sorted(Comparator.comparingInt((AcumuladorProducto a) -> a.unidadesVendidas).reversed()
                        .thenComparing(Comparator.comparingDouble((AcumuladorProducto a) -> a.montoTotal).reversed()))
                .limit(limiteEfectivo)
                .toList();

        List<Map<String, Object>> filas = new ArrayList<>();
        for (AcumuladorProducto item : ordenados) {
            Map<String, Object> fila = new LinkedHashMap<>();
            fila.put("codigo", item.codigo);
            fila.put("producto", item.nombre);
            fila.put("presentacion", item.presentacion);
            fila.put("unidadesVendidas", item.unidadesVendidas);
            fila.put("montoTotal", item.montoTotal);
            filas.add(fila);
        }

        return new ReporteTabular(
                "Ranking de Productos Más Vendidos",
                LocalDateTime.now(),
                desde,
                hasta,
                columnas,
                filas
        );
    }

    /**
     * Genera el reporte de rendimiento de preventores ({@code GET /reportes/rendimiento-preventores}).
     */
    public ReporteTabular generarReporteRendimientoPreventores(LocalDate desde, LocalDate hasta) {
        validarRangoFechas(desde, hasta);
        List<ColumnaReporte> columnas = List.of(
                new ColumnaReporte("legajo", "Legajo"),
                new ColumnaReporte("preventor", "Preventor"),
                new ColumnaReporte("sucursal", "Sucursal"),
                new ColumnaReporte("cantidadPedidos", "Pedidos Gestionados"),
                new ColumnaReporte("pedidosEntregados", "Pedidos Entregados"),
                new ColumnaReporte("montoTotal", "Monto Total Vendido")
        );

        List<Preventor> preventores = empleadoRepository.findAllByTipoAndEliminadoFalse(Preventor.class);
        List<Pedido> pedidos = pedidoRepository.findAllByEliminadoFalseOrderByFechaHoraCargaDesc();

        Map<String, AcumuladorPreventor> mapa = new LinkedHashMap<>();
        for (Preventor pr : preventores) {
            String sucursal = pr.getMayorista() != null ? pr.getMayorista().getRazonSocial() : "-";
            mapa.put(pr.getId(), new AcumuladorPreventor(
                    pr.getLegajo(),
                    (pr.getNombre() + " " + pr.getApellido()).trim(),
                    sucursal
            ));
        }

        for (Pedido p : pedidos) {
            if (p.getEstado() == EstadoPedido.ANULADO || p.getPreventor() == null) {
                continue;
            }
            LocalDate fecha = p.getFechaHoraCarga() != null ? p.getFechaHoraCarga().toLocalDate() : null;
            if (!estaEnRango(fecha, desde, hasta)) {
                continue;
            }
            Preventor pr = p.getPreventor();
            AcumuladorPreventor acc = mapa.computeIfAbsent(
                    pr.getId(),
                    id -> new AcumuladorPreventor(
                            pr.getLegajo(),
                            (pr.getNombre() + " " + pr.getApellido()).trim(),
                            pr.getMayorista() != null ? pr.getMayorista().getRazonSocial() : "-"
                    )
            );
            acc.cantidadPedidos++;
            if (p.getEstado() == EstadoPedido.ENTREGADO) {
                acc.pedidosEntregados++;
            }
            acc.montoTotal += p.getTotal();
        }

        List<Map<String, Object>> filas = new ArrayList<>();
        for (AcumuladorPreventor acc : mapa.values()) {
            Map<String, Object> fila = new LinkedHashMap<>();
            fila.put("legajo", acc.legajo);
            fila.put("preventor", acc.nombre);
            fila.put("sucursal", acc.sucursal);
            fila.put("cantidadPedidos", acc.cantidadPedidos);
            fila.put("pedidosEntregados", acc.pedidosEntregados);
            fila.put("montoTotal", acc.montoTotal);
            filas.add(fila);
        }

        return new ReporteTabular(
                "Reporte de Rendimiento de Preventores",
                LocalDateTime.now(),
                desde,
                hasta,
                columnas,
                filas
        );
    }

    /**
     * Genera el histórico de ventas discriminado por departamento y preventor
     * ({@code GET /reportes/ventas-historico}).
     */
    public ReporteTabular generarReporteVentasHistorico(
            LocalDate desde,
            LocalDate hasta,
            String departamentoId,
            String preventorId) {
        validarRangoFechas(desde, hasta);
        List<ColumnaReporte> columnas = List.of(
                new ColumnaReporte("fecha", "Fecha"),
                new ColumnaReporte("numeroPedido", "Nro. Pedido"),
                new ColumnaReporte("departamento", "Departamento"),
                new ColumnaReporte("localidad", "Localidad"),
                new ColumnaReporte("comercio", "Comercio"),
                new ColumnaReporte("preventor", "Preventor"),
                new ColumnaReporte("estado", "Estado"),
                new ColumnaReporte("total", "Total Venta")
        );

        String deptoFiltro = (departamentoId != null && !departamentoId.isBlank()) ? departamentoId.trim() : null;
        String prevFiltro = (preventorId != null && !preventorId.isBlank()) ? preventorId.trim() : null;

        List<Pedido> pedidos = pedidoRepository.findAllByEliminadoFalseOrderByFechaHoraCargaDesc();
        List<Map<String, Object>> filas = new ArrayList<>();

        for (Pedido p : pedidos) {
            if (p.getEstado() == EstadoPedido.ANULADO) {
                continue;
            }
            LocalDate fecha = p.getFechaHoraCarga() != null ? p.getFechaHoraCarga().toLocalDate() : null;
            if (!estaEnRango(fecha, desde, hasta)) {
                continue;
            }
            if (prevFiltro != null && (p.getPreventor() == null || !prevFiltro.equals(p.getPreventor().getId()))) {
                continue;
            }

            PuntoDeVenta pdv = p.getPuntoDeVenta();
            Direccion dir = pdv != null ? pdv.getDireccion() : null;
            Localidad loc = dir != null ? dir.getLocalidad() : null;
            Departamento depto = loc != null ? loc.getDepartamento() : null;

            if (deptoFiltro != null && (depto == null || !deptoFiltro.equals(depto.getId()))) {
                continue;
            }

            Map<String, Object> fila = new LinkedHashMap<>();
            fila.put("fecha", fecha != null ? fecha.toString() : "-");
            fila.put("numeroPedido", p.getNumeroPedido());
            fila.put("departamento", depto != null ? depto.getNombre() : "-");
            fila.put("localidad", loc != null ? loc.getNombre() : "-");
            fila.put("comercio", pdv != null ? pdv.getNombreComercio() : "-");
            fila.put("preventor", p.getPreventor() != null
                    ? (p.getPreventor().getNombre() + " " + p.getPreventor().getApellido()).trim()
                    : "Web / Autogestión");
            fila.put("estado", p.getEstado() != null ? p.getEstado().name() : "-");
            fila.put("total", p.getTotal());
            filas.add(fila);
        }

        return new ReporteTabular(
                "Histórico de Ventas por Departamento y Preventor",
                LocalDateTime.now(),
                desde,
                hasta,
                columnas,
                filas
        );
    }

    /**
     * Resuelve el {@link ReporteExporter} mediante {@link ReporteExporterFactory} (Patrón Factory Method).
     */
    public ReporteExporter obtenerExporter(String formato, Set<String> formatosPermitidos) {
        return reporteExporterFactory.obtenerExporter(formato, formatosPermitidos);
    }

    private void validarRangoFechas(LocalDate desde, LocalDate hasta) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new ValidacionException("La fecha 'desde' no puede ser posterior a la fecha 'hasta'");
        }
    }

    private boolean estaEnRango(LocalDate fecha, LocalDate desde, LocalDate hasta) {
        if (fecha == null) {
            return desde == null && hasta == null;
        }
        if (desde != null && fecha.isBefore(desde)) {
            return false;
        }
        if (hasta != null && fecha.isAfter(hasta)) {
            return false;
        }
        return true;
    }

    private EstadoPedido parsearEstadoPedido(String estadoStr) {
        if (estadoStr == null || estadoStr.isBlank()) {
            return null;
        }
        try {
            return EstadoPedido.valueOf(estadoStr.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new ValidacionException("Estado de pedido inválido: " + estadoStr);
        }
    }

    private String extraerEmail(Minorista m) {
        if (m.getContactos() != null) {
            for (Contacto c : m.getContactos()) {
                if (!c.isEliminado() && c instanceof ContactoCorreoElectronico correo && correo.getEmail() != null) {
                    return correo.getEmail();
                }
            }
        }
        if (m.getUsuario() != null && m.getUsuario().getNombreUsuario() != null) {
            return m.getUsuario().getNombreUsuario();
        }
        return "-";
    }

    private String extraerTelefono(Minorista m) {
        if (m.getContactos() != null) {
            for (Contacto c : m.getContactos()) {
                if (!c.isEliminado() && c instanceof ContactoTelefonico tel && tel.getTelefono() != null) {
                    return tel.getTelefono();
                }
            }
        }
        return "-";
    }

    private static final class AcumuladorProducto {
        private final String codigo;
        private final String nombre;
        private final String presentacion;
        private int unidadesVendidas;
        private double montoTotal;

        private AcumuladorProducto(String codigo, String nombre, String presentacion) {
            this.codigo = codigo != null ? codigo : "-";
            this.nombre = nombre != null ? nombre : "-";
            this.presentacion = presentacion != null ? presentacion : "-";
        }
    }

    private static final class AcumuladorPreventor {
        private final String legajo;
        private final String nombre;
        private final String sucursal;
        private int cantidadPedidos;
        private int pedidosEntregados;
        private double montoTotal;

        private AcumuladorPreventor(String legajo, String nombre, String sucursal) {
            this.legajo = legajo != null ? legajo : "-";
            this.nombre = nombre != null ? nombre : "-";
            this.sucursal = sucursal != null ? sucursal : "-";
        }
    }
}

