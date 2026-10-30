package com.DulceMayor.backend.services.pedido;

import com.DulceMayor.backend.config.AccesoDenegadoException;
import com.DulceMayor.backend.config.ConflictoException;
import com.DulceMayor.backend.config.UsuarioPrincipal;
import com.DulceMayor.backend.config.ValidacionException;
import com.DulceMayor.backend.dtos.pedido.AnularPedidoRequest;
import com.DulceMayor.backend.dtos.pedido.DetallePedidoRequest;
import com.DulceMayor.backend.dtos.pedido.HistorialEstadoPedidoResponse;
import com.DulceMayor.backend.dtos.pedido.PedidoEstadoCambiadoEvent;
import com.DulceMayor.backend.dtos.pedido.PedidoMapper;
import com.DulceMayor.backend.dtos.pedido.PedidoMiCuentaPage;
import com.DulceMayor.backend.dtos.pedido.PedidoPage;
import com.DulceMayor.backend.dtos.pedido.PedidoRequest;
import com.DulceMayor.backend.dtos.pedido.PedidoResponse;
import com.DulceMayor.backend.dtos.pedido.PedidoUpdateRequest;
import com.DulceMayor.backend.dtos.producto.VigenciaPrecioResponse;
import com.DulceMayor.backend.entities.RolUsuario;
import com.DulceMayor.backend.entities.pedido.DetallePedido;
import com.DulceMayor.backend.entities.pedido.EstadoPedido;
import com.DulceMayor.backend.entities.pedido.Pedido;
import com.DulceMayor.backend.entities.persona.Distribuidor;
import com.DulceMayor.backend.entities.persona.Minorista;
import com.DulceMayor.backend.entities.persona.Preventor;
import com.DulceMayor.backend.entities.persona.PuntoDeVenta;
import com.DulceMayor.backend.entities.producto.Producto;
import com.DulceMayor.backend.entities.producto.Promocion;
import com.DulceMayor.backend.repositories.pedido.PedidoRepository;
import com.DulceMayor.backend.services.BaseService;
import com.DulceMayor.backend.services.persona.EmpleadoService;
import com.DulceMayor.backend.services.persona.MinoristaService;
import com.DulceMayor.backend.services.persona.PuntoDeVentaService;
import com.DulceMayor.backend.services.producto.ProductoService;
import com.DulceMayor.backend.services.producto.PromocionService;
import com.DulceMayor.backend.services.producto.VigenciaPrecioService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de negocio central para la gestión del ciclo de vida de {@link Pedido}
 * según {@code 01-dominio.md}, {@code 02-reglas-negocio.md} y {@code 04-openapi.yaml}.
 */
@Service
public class PedidoService extends BaseService<Pedido, PedidoRequest, PedidoResponse> {

    private final PedidoRepository pedidoRepository;
    private final PedidoLecturaService pedidoLecturaService;
    private final HistorialEstadoPedidoService historialEstadoPedidoService;
    private final ProductoService productoService;
    private final VigenciaPrecioService vigenciaPrecioService;
    private final PromocionService promocionService;
    private final PuntoDeVentaService puntoDeVentaService;
    private final EmpleadoService empleadoService;
    private final MinoristaService minoristaService;
    private final ApplicationEventPublisher eventPublisher;

    public PedidoService(
            PedidoRepository pedidoRepository,
            PedidoMapper pedidoMapper,
            PedidoLecturaService pedidoLecturaService,
            HistorialEstadoPedidoService historialEstadoPedidoService,
            ProductoService productoService,
            VigenciaPrecioService vigenciaPrecioService,
            PromocionService promocionService,
            PuntoDeVentaService puntoDeVentaService,
            EmpleadoService empleadoService,
            MinoristaService minoristaService,
            ApplicationEventPublisher eventPublisher) {
        super(pedidoRepository, pedidoMapper, "Pedido");
        this.pedidoRepository = pedidoRepository;
        this.pedidoLecturaService = pedidoLecturaService;
        this.historialEstadoPedidoService = historialEstadoPedidoService;
        this.productoService = productoService;
        this.vigenciaPrecioService = vigenciaPrecioService;
        this.promocionService = promocionService;
        this.puntoDeVentaService = puntoDeVentaService;
        this.empleadoService = empleadoService;
        this.minoristaService = minoristaService;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Carga un nuevo pedido en estado {@link EstadoPedido#CARGADO}, vinculando el punto de venta,
     * el preventor asignado, resolviendo precios vigentes y registrando su primer historial.
     */
    @Override
    @Transactional
    public PedidoResponse crear(PedidoRequest request) {
        if (request == null) {
            throw new ValidacionException("Los datos del pedido son obligatorios");
        }
        if (request.puntoDeVentaId() == null || request.puntoDeVentaId().isBlank()) {
            throw new ValidacionException("El identificador del punto de venta es obligatorio");
        }
        if (request.detalles() == null || request.detalles().isEmpty()) {
            throw new ValidacionException("El pedido debe contener al menos un detalle");
        }

        PuntoDeVenta puntoDeVenta = puntoDeVentaService.obtenerEntidad(request.puntoDeVentaId().trim());
        Preventor preventor = resolverPreventorParaCreacion(request.preventorId(), puntoDeVenta);
        CalculoPedido calculo = calcularLineasYTotales(request.detalles(), request.codigoPromocion());

        Long siguienteNumero = pedidoRepository.findFirstByOrderByNumeroPedidoDesc()
                .map(p -> p.getNumeroPedido() + 1L)
                .orElse(1L);

        Pedido pedido = Pedido.builder()
                .numeroPedido(siguienteNumero)
                .fechaHoraCarga(LocalDateTime.now())
                .puntoDeVenta(puntoDeVenta)
                .preventor(preventor)
                .distribuidor(null)
                .promocion(calculo.promocion())
                .subtotal(calculo.subtotal())
                .descuento(calculo.descuento())
                .total(calculo.total())
                .estado(EstadoPedido.CARGADO)
                .detalles(calculo.detalles())
                .eliminado(false)
                .build();

        Pedido guardado = pedidoRepository.save(pedido);
        String usuarioActual = obtenerNombreUsuarioActual();
        historialEstadoPedidoService.registrarEstado(
                guardado,
                EstadoPedido.CARGADO,
                usuarioActual,
                "Pedido cargado"
        );

        return aResponse(guardado);
    }

    /**
     * Modifica los ítems y/o el código de promoción de un pedido.
     * Solo está permitido mientras el pedido permanece en estado {@link EstadoPedido#CARGADO}.
     */
    @Transactional
    public PedidoResponse modificarPedido(String id, PedidoUpdateRequest request) {
        if (request == null || request.detalles() == null || request.detalles().isEmpty()) {
            throw new ValidacionException("El pedido debe contener al menos un detalle");
        }

        Pedido existente = obtenerEntidad(id);
        verificarAcceso(existente);

        if (existente.getEstado() != EstadoPedido.CARGADO) {
            throw new ConflictoException(
                    "TRANSICION_INVALIDA",
                    "Solo se puede modificar un pedido en estado CARGADO. Estado actual: " + existente.getEstado()
            );
        }

        CalculoPedido calculo = calcularLineasYTotales(request.detalles(), request.codigoPromocion());

        existente.getDetalles().clear();
        existente.getDetalles().addAll(calculo.detalles());
        existente.setPromocion(calculo.promocion());
        existente.setSubtotal(calculo.subtotal());
        existente.setDescuento(calculo.descuento());
        existente.setTotal(calculo.total());

        Pedido guardado = pedidoRepository.save(existente);
        return aResponse(guardado);
    }

    /**
     * Anula administrativamente un pedido siempre que se encuentre en {@link EstadoPedido#CARGADO}
     * o {@link EstadoPedido#SIN_STOCK}.
     */
    @Transactional
    public PedidoResponse anularPedido(String id, AnularPedidoRequest request) {
        Pedido pedido = obtenerEntidad(id);
        if (pedido.getEstado() != EstadoPedido.CARGADO && pedido.getEstado() != EstadoPedido.SIN_STOCK) {
            throw new ConflictoException(
                    "TRANSICION_INVALIDA",
                    "No se puede anular un pedido en estado " + pedido.getEstado()
            );
        }

        String observacion = (request != null && request.observacion() != null && !request.observacion().isBlank())
                ? request.observacion().trim()
                : "Pedido anulado administrativamente";
        String usuarioActual = obtenerNombreUsuarioActual();

        pedido.setEstado(EstadoPedido.ANULADO);
        Pedido guardado = pedidoRepository.save(pedido);

        historialEstadoPedidoService.registrarEstado(guardado, EstadoPedido.ANULADO, usuarioActual, observacion);
        publicarEventoEstado(guardado, usuarioActual, observacion);

        return aResponse(guardado);
    }

    /**
     * Marca el pedido en {@link EstadoPedido#SIN_STOCK} durante el procesamiento cuando algún ítem carece de stock.
     */
    @Transactional
    public PedidoResponse marcarSinStock(Pedido pedido, String observacion) {
        String usuarioActual = obtenerNombreUsuarioActual();
        pedido.setEstado(EstadoPedido.SIN_STOCK);
        Pedido guardado = pedidoRepository.save(pedido);

        historialEstadoPedidoService.registrarEstado(guardado, EstadoPedido.SIN_STOCK, usuarioActual, observacion);
        publicarEventoEstado(guardado, usuarioActual, observacion);

        return aResponse(guardado);
    }

    /**
     * Marca el pedido en {@link EstadoPedido#FACTURADO} una vez emitida su factura y descontado el stock.
     */
    @Transactional
    public PedidoResponse marcarFacturado(Pedido pedido, String observacion) {
        String usuarioActual = obtenerNombreUsuarioActual();
        pedido.setEstado(EstadoPedido.FACTURADO);
        Pedido guardado = pedidoRepository.save(pedido);

        historialEstadoPedidoService.registrarEstado(guardado, EstadoPedido.FACTURADO, usuarioActual, observacion);
        publicarEventoEstado(guardado, usuarioActual, observacion);

        return aResponse(guardado);
    }

    /**
     * Transiciona un pedido {@link EstadoPedido#FACTURADO} a {@link EstadoPedido#EN_REPARTO} asignándole el distribuidor.
     */
    @Transactional
    public PedidoResponse asignarAReparto(String id, String distribuidorId, String observacion) {
        if (distribuidorId == null || distribuidorId.isBlank()) {
            throw new ValidacionException("El identificador del distribuidor es obligatorio para pasar a EN_REPARTO");
        }
        Pedido pedido = obtenerEntidad(id);
        if (pedido.getEstado() != EstadoPedido.FACTURADO) {
            throw new ConflictoException(
                    "TRANSICION_INVALIDA",
                    "Solo un pedido FACTURADO puede pasar a EN_REPARTO. Estado actual: " + pedido.getEstado()
            );
        }

        Distribuidor distribuidor = empleadoService.obtenerDistribuidor(distribuidorId.trim());
        String usuarioActual = obtenerNombreUsuarioActual();
        String obs = (observacion != null && !observacion.isBlank())
                ? observacion.trim()
                : "Asignado a reparto del distribuidor " + distribuidor.getLegajo();

        pedido.setDistribuidor(distribuidor);
        pedido.setEstado(EstadoPedido.EN_REPARTO);
        Pedido guardado = pedidoRepository.save(pedido);

        historialEstadoPedidoService.registrarEstado(guardado, EstadoPedido.EN_REPARTO, usuarioActual, obs);
        publicarEventoEstado(guardado, usuarioActual, obs);

        return aResponse(guardado);
    }

    /**
     * Transiciona un pedido {@link EstadoPedido#EN_REPARTO} a {@link EstadoPedido#ENTREGADO} al completarse la entrega.
     */
    @Transactional
    public PedidoResponse marcarEntregado(String id, String observacion) {
        Pedido pedido = obtenerEntidad(id);
        if (pedido.getEstado() != EstadoPedido.EN_REPARTO) {
            throw new ConflictoException(
                    "TRANSICION_INVALIDA",
                    "Solo un pedido EN_REPARTO puede marcarse como ENTREGADO. Estado actual: " + pedido.getEstado()
            );
        }

        String usuarioActual = obtenerNombreUsuarioActual();
        String obs = (observacion != null && !observacion.isBlank())
                ? observacion.trim()
                : "Pedido entregado en punto de venta";

        pedido.setEstado(EstadoPedido.ENTREGADO);
        Pedido guardado = pedidoRepository.save(pedido);

        historialEstadoPedidoService.registrarEstado(guardado, EstadoPedido.ENTREGADO, usuarioActual, obs);
        publicarEventoEstado(guardado, usuarioActual, obs);

        return aResponse(guardado);
    }

    /**
     * Busca un pedido individual aplicando validación de visibilidad si el usuario es {@code PREVENTISTA} o {@code MINORISTA}.
     */
    @Transactional(readOnly = true)
    public PedidoResponse buscarPedido(String id) {
        Pedido pedido = obtenerEntidad(id);
        verificarAcceso(pedido);
        return aResponse(pedido);
    }

    /**
     * Devuelve el historial cronológico de estados de un pedido.
     */
    @Transactional(readOnly = true)
    public List<HistorialEstadoPedidoResponse> listarHistorialPorPedido(String id) {
        Pedido pedido = obtenerEntidad(id);
        verificarAcceso(pedido);
        return historialEstadoPedidoService.listarHistorialPorPedido(id);
    }

    /**
     * Listado paginado de pedidos con filtros. Si quien consulta es {@code PREVENTISTA}, solo visualiza sus propios pedidos.
     */
    @Transactional(readOnly = true)
    public PedidoPage listarPaginado(
            String estadoStr,
            String preventorId,
            String distribuidorId,
            String puntoDeVentaId,
            String minoristaId,
            LocalDate desde,
            LocalDate hasta,
            Pageable pageable) {

        EstadoPedido estado = parsearEstado(estadoStr);
        String filtroPreventor = preventorId;
        UsuarioPrincipal principal = obtenerPrincipalActual();
        if (principal != null && principal.getRol() == RolUsuario.PREVENTISTA) {
            Preventor miPreventor = empleadoService.obtenerPreventorPorUsuarioId(principal.getId());
            filtroPreventor = miPreventor.getId();
        }

        return pedidoLecturaService.listarPaginado(
                estado,
                filtroPreventor,
                distribuidorId,
                puntoDeVentaId,
                minoristaId,
                desde,
                hasta,
                pageable
        );
    }

    /**
     * Listado paginado de pedidos para el portal "Mi cuenta" del minorista autenticado.
     */
    @Transactional(readOnly = true)
    public PedidoMiCuentaPage listarMiCuenta(String estadoStr, Pageable pageable) {
        EstadoPedido estado = parsearEstado(estadoStr);
        UsuarioPrincipal principal = obtenerPrincipalActual();
        String minoristaId = null;
        if (principal != null && principal.getRol() == RolUsuario.MINORISTA) {
            minoristaId = minoristaService.buscarPorUsuarioId(principal.getId())
                    .map(Minorista::getId)
                    .orElse(null);
        }
        return pedidoLecturaService.listarMiCuenta(estado, minoristaId, pageable);
    }

    @Override
    protected PedidoResponse aResponse(Pedido entidad) {
        return pedidoLecturaService.enriquecerResponse(entidad);
    }

    @Override
    protected List<Pedido> buscarEntidadesActivas() {
        return pedidoRepository.findAllByEliminadoFalseOrderByFechaHoraCargaDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponse> listar() {
        List<Pedido> entidades = buscarEntidadesActivas();
        List<PedidoResponse> resultado = new ArrayList<>();
        for (Pedido p : entidades) {
            resultado.add(aResponse(p));
        }
        return resultado;
    }

    private CalculoPedido calcularLineasYTotales(List<DetallePedidoRequest> items, String codigoPromocion) {
        List<DetallePedido> detalles = new ArrayList<>();
        double subtotalPedido = 0.0;
        int totalUnidadesPedido = 0;

        for (DetallePedidoRequest item : items) {
            if (item == null || item.productoId() == null || item.productoId().isBlank()) {
                throw new ValidacionException("El identificador del producto en cada detalle es obligatorio");
            }
            if (item.cantidad() == null || item.cantidad() < 1) {
                throw new ValidacionException("La cantidad de cada detalle debe ser al menos 1");
            }

            Producto producto = productoService.obtenerEntidad(item.productoId().trim());
            VigenciaPrecioResponse vigencia = vigenciaPrecioService.buscarVigente(producto.getId());
            double precioUnitario = vigencia.precio();
            double subtotalLinea = item.cantidad() * precioUnitario;

            DetallePedido detalle = DetallePedido.builder()
                    .producto(producto)
                    .cantidad(item.cantidad())
                    .precioUnitario(precioUnitario)
                    .subtotal(subtotalLinea)
                    .eliminado(false)
                    .build();

            detalles.add(detalle);
            subtotalPedido += subtotalLinea;
            totalUnidadesPedido += item.cantidad();
        }

        Promocion promocion = null;
        double descuento = 0.0;

        if (codigoPromocion != null && !codigoPromocion.isBlank()) {
            promocion = promocionService.obtenerVigentePorCodigoParaPedido(codigoPromocion);

            if (promocion.getProducto() != null) {
                String prodPromoId = promocion.getProducto().getId();
                int unidadesProductoPromocionado = detalles.stream()
                        .filter(d -> d.getProducto().getId().equals(prodPromoId))
                        .mapToInt(DetallePedido::getCantidad)
                        .sum();

                if (unidadesProductoPromocionado == 0) {
                    throw new ValidacionException(
                            "El pedido no incluye el producto requerido por la promoción '" + promocion.getCodigo() + "'"
                    );
                }
                if (promocion.getCantidadMinima() > 0 && unidadesProductoPromocionado < promocion.getCantidadMinima()) {
                    throw new ValidacionException(
                            "No se alcanza la cantidad mínima (" + promocion.getCantidadMinima()
                                    + ") del producto promocionado para el código '" + promocion.getCodigo() + "'"
                    );
                }
            } else if (promocion.getCantidadMinima() > 0 && totalUnidadesPedido < promocion.getCantidadMinima()) {
                throw new ValidacionException(
                        "No se alcanza la cantidad mínima total de unidades (" + promocion.getCantidadMinima()
                                + ") para aplicar la promoción '" + promocion.getCodigo() + "'"
                );
            }

            descuento = subtotalPedido * (promocion.getPorcentajeDescuento() / 100.0);
        }

        double total = Math.max(0.0, subtotalPedido - descuento);
        return new CalculoPedido(detalles, promocion, subtotalPedido, descuento, total);
    }

    private Preventor resolverPreventorParaCreacion(String preventorIdRequest, PuntoDeVenta puntoDeVenta) {
        UsuarioPrincipal principal = obtenerPrincipalActual();
        if (principal != null && principal.getRol() == RolUsuario.PREVENTISTA) {
            return empleadoService.obtenerPreventorPorUsuarioId(principal.getId());
        }
        if (preventorIdRequest != null && !preventorIdRequest.isBlank()) {
            return empleadoService.obtenerPreventor(preventorIdRequest.trim());
        }
        if (puntoDeVenta.getPreventor() != null) {
            return puntoDeVenta.getPreventor();
        }
        throw new ValidacionException("Debe especificar un preventor para el pedido o asignarlo al punto de venta");
    }

    private void verificarAcceso(Pedido pedido) {
        UsuarioPrincipal principal = obtenerPrincipalActual();
        if (principal == null) {
            return;
        }
        if (principal.getRol() == RolUsuario.PREVENTISTA) {
            Preventor miPreventor = empleadoService.obtenerPreventorPorUsuarioId(principal.getId());
            if (pedido.getPreventor() == null || !pedido.getPreventor().getId().equals(miPreventor.getId())) {
                throw new AccesoDenegadoException("No tiene permisos para acceder a un pedido asignado a otro preventista");
            }
        }
        if (principal.getRol() == RolUsuario.MINORISTA) {
            java.util.Optional<Minorista> minoristaOpt = minoristaService.buscarPorUsuarioId(principal.getId());
            if (minoristaOpt.isPresent()) {
                Minorista miMinorista = minoristaOpt.get();
                if (pedido.getPuntoDeVenta() == null
                        || pedido.getPuntoDeVenta().getMinorista() == null
                        || !pedido.getPuntoDeVenta().getMinorista().getId().equals(miMinorista.getId())) {
                    throw new AccesoDenegadoException("No tiene permisos para acceder a un pedido de otro cliente");
                }
            }
        }
    }

    private EstadoPedido parsearEstado(String estadoStr) {
        if (estadoStr == null || estadoStr.isBlank()) {
            return null;
        }
        try {
            return EstadoPedido.valueOf(estadoStr.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new ValidacionException("Estado de pedido inválido: " + estadoStr);
        }
    }

    private void publicarEventoEstado(Pedido pedido, String usuario, String observacion) {
        eventPublisher.publishEvent(new PedidoEstadoCambiadoEvent(
                pedido.getId(),
                pedido.getNumeroPedido(),
                pedido.getEstado(),
                usuario,
                observacion
        ));
    }

    private UsuarioPrincipal obtenerPrincipalActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UsuarioPrincipal up) {
            return up;
        }
        return null;
    }

    private String obtenerNombreUsuarioActual() {
        UsuarioPrincipal principal = obtenerPrincipalActual();
        if (principal != null) {
            return principal.getUsername();
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName() != null && !auth.getName().isBlank()) {
            return auth.getName();
        }
        return "sistema";
    }

    private record CalculoPedido(
            List<DetallePedido> detalles,
            Promocion promocion,
            double subtotal,
            double descuento,
            double total
    ) {
    }
}
