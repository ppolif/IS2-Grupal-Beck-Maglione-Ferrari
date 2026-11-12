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
import com.DulceMayor.backend.entities.facturacion.FormaDePago;
import com.DulceMayor.backend.entities.facturacion.TipoPago;
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
import com.DulceMayor.backend.services.facturacion.FormaDePagoService;
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
    private final FormaDePagoService formaDePagoService;
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
            FormaDePagoService formaDePagoService,
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
        this.formaDePagoService = formaDePagoService;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Valida la presencia de datos obligatorios al dar de alta un pedido:
     * que el request no sea nulo, que se haya provisto el punto de venta y que contenga al menos un detalle.
     *
     * <p><b>Cuándo se usa:</b> Durante {@code crear(request)} como hook de validación previo a mapear y persistir.</p>
     */
    @Override
    protected void validarCrear(PedidoRequest request) {
        if (request == null) {
            throw new ValidacionException("Los datos del pedido son obligatorios");
        }
        if (request.puntoDeVentaId() == null || request.puntoDeVentaId().isBlank()) {
            throw new ValidacionException("Debe registrar y seleccionar un punto de venta antes de realizar un pedido");
        }
        if (request.detalles() == null || request.detalles().isEmpty()) {
            throw new ValidacionException("El pedido debe contener al menos un detalle");
        }
    }

    /**
     * Hook del Template Method ejecutado antes de persistir la entidad en base de datos.
     * Se encarga de:
     * 1. Resolver la entidad {@link PuntoDeVenta} seleccionada obligatoriamente.
     * 2. Determinar el {@link Preventor} responsable (si es preventista autenticado, asigna el suyo).
     * 3. Calcular precios vigentes, promociones aplicables, subtotales, descuentos y total.
     * 4. Asignar el siguiente número de pedido autoincremental correlativo.
     * 5. Establecer la fecha/hora de carga y el estado inicial {@link EstadoPedido#CARGADO}.
     *
     * <p><b>Cuándo se usa:</b> Durante {@code crear(request)}, justo antes de llamar a {@code repository.save}.</p>
     */
    @Override
    protected void antesDePersistir(Pedido entidad, PedidoRequest request) {
        if (request.puntoDeVentaId() == null || request.puntoDeVentaId().isBlank()) {
            throw new ValidacionException("Debe registrar y seleccionar un punto de venta antes de realizar un pedido");
        }
        PuntoDeVenta puntoDeVenta = puntoDeVentaService.obtenerEntidad(request.puntoDeVentaId().trim());
        Preventor preventor = resolverPreventorParaCreacion(request.preventorId(), puntoDeVenta);
        CalculoPedido calculo = calcularLineasYTotales(request.detalles(), request.codigoPromocion());

        Long siguienteNumero = pedidoRepository.findFirstByOrderByNumeroPedidoDesc()
                .map(p -> p.getNumeroPedido() + 1L)
                .orElse(1L);

        entidad.setNumeroPedido(siguienteNumero);
        entidad.setFechaHoraCarga(LocalDateTime.now());
        entidad.setPuntoDeVenta(puntoDeVenta);
        entidad.setPreventor(preventor);
        entidad.setDistribuidor(null);
        entidad.setPromocion(calculo.promocion());
        entidad.setSubtotal(calculo.subtotal());
        entidad.setDescuento(calculo.descuento());
        entidad.setTotal(calculo.total());
        entidad.setEstado(EstadoPedido.CARGADO);
        entidad.setDetalles(calculo.detalles());
        if (request.tipoPago() != null) {
            FormaDePago formaDePago = formaDePagoService.obtenerOCrearPorTipo(request.tipoPago());
            entidad.setFormaDePago(formaDePago);
        }
    }

    /**
     * Lista todos los pedidos pendientes de asignación y armado de hoja de ruta logística:
     * pedidos en estado {@link EstadoPedido#FACTURADO} sin distribuidor asignado, así como pedidos
     * en estado {@link EstadoPedido#CARGADO} con modalidad de pago en {@link TipoPago#EFECTIVO}
     * (cuya facturación se realiza por el distribuidor al momento de la entrega).
     *
     * @return Lista de {@link Pedido} pendientes de reparto no eliminados ordenados por fecha de carga ascendente.
     */
    @Transactional(readOnly = true)
    public List<Pedido> listarFacturadosPendientes() {
        List<Pedido> pendientes = pedidoRepository.buscarPendientesDeReparto();
        if (pendientes != null && !pendientes.isEmpty()) {
            return pendientes;
        }
        return pedidoRepository.findAllByEstadoAndEliminadoFalseOrderByFechaHoraCargaAsc(EstadoPedido.FACTURADO).stream()
                .filter(p -> p.getDistribuidor() == null)
                .toList();
    }

    /**
     * Hook del Template Method ejecutado con posterioridad a la persistencia del pedido.
     * Registra en la bitácora histórica de estados el primer registro: estado {@link EstadoPedido#CARGADO}.
     *
     * <p><b>Cuándo se usa:</b> Durante {@code crear(request)}, una vez que el pedido ya fue insertado en la base de datos.</p>
     */
    @Override
    protected void despuesDeCrear(Pedido guardado, PedidoRequest request) {
        String usuarioActual = obtenerNombreUsuarioActual();
        historialEstadoPedidoService.registrarEstado(
                guardado,
                EstadoPedido.CARGADO,
                usuarioActual,
                "Pedido cargado"
        );
    }

    /**
     * Modifica los ítems y/o el código de promoción de un pedido existente.
     * Recalcula los precios unitarios vigentes, subtotales, descuentos y el total general.
     * Solo se permite la modificación si el pedido se encuentra estrictamente en estado {@link EstadoPedido#CARGADO}.
     *
     * <p><b>Cuándo se usa:</b> Es invocado por el endpoint {@code PUT /pedidos/{id}} cuando el cliente o el preventista
     * necesita ajustar las cantidades, agregar/quitar productos o cambiar el cupón de descuento antes de que el pedido sea procesado.</p>
     *
     * @param id Identificador único del pedido a modificar.
     * @param request Datos de los nuevos ítems y código de promoción.
     * @return {@link PedidoResponse} con la información del pedido actualizada.
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
     * Anula administrativamente un pedido, transicionándolo a {@link EstadoPedido#ANULADO}.
     * Solo es válido para pedidos en estado {@link EstadoPedido#CARGADO} o {@link EstadoPedido#SIN_STOCK}.
     * Registra el cambio en la bitácora de auditoría histórica y emite un evento de dominio.
     *
     * <p><b>Cuándo se usa:</b> Es invocado por el endpoint {@code POST /pedidos/{id}/anular} cuando un administrador
     * o usuario autorizado decide cancelar un pedido que no ha sido facturado ni enviado a reparto.</p>
     *
     * @param id Identificador único del pedido a anular.
     * @param request Motivo u observación de la anulación.
     * @return {@link PedidoResponse} con el pedido actualizado a estado ANULADO.
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

        /// si el pedido es anulado, se registra
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
     * Marca un pedido en estado {@link EstadoPedido#SIN_STOCK}, registrando la auditoría correspondiente
     * y emitiendo el evento de cambio de estado.
     *
     * <p><b>Cuándo se usa:</b> Es invocado internamente por {@code ProcesamientoPedidoService} durante la ejecución del proceso
     * de facturación/procesamiento cuando detecta que no hay existencias suficientes de stock en inventario para cubrir los ítems del pedido.</p>
     *
     * @param pedido Entidad del pedido a transicionar.
     * @param observacion Detalle de los productos faltantes o motivo del quiebre de stock.
     * @return {@link PedidoResponse} actualizado.
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
     * Marca un pedido en estado {@link EstadoPedido#FACTURADO}, registrando la auditoría correspondiente
     * y emitiendo el evento de cambio de estado.
     *
     * <p><b>Cuándo se usa:</b> Es invocado internamente por {@code ProcesamientoPedidoService} una vez que se emitió
     * exitosamente la Factura asociada y se descontó el stock de los productos involucrados.</p>
     *
     * @param pedido Entidad del pedido a transicionar.
     * @param observacion Detalle u observación de la facturación.
     * @return {@link PedidoResponse} actualizado.
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
     * Transiciona un pedido desde {@link EstadoPedido#FACTURADO} hacia {@link EstadoPedido#EN_REPARTO},
     * asignando el {@link Distribuidor} responsable del flete físico y registrando el evento en el historial.
     *
     * <p><b>Cuándo se usa:</b> Es invocado por el endpoint {@code POST /pedidos/{id}/asignar-reparto} cuando el área
     * de logística y expedición arma la carga y asigna el repartidor que llevará el pedido al punto de venta.</p>
     *
     * @param id Identificador único del pedido.
     * @param distribuidorId Identificador del empleado distribuidor.
     * @param observacion Notas adicionales de entrega o de hoja de ruta.
     * @return {@link PedidoResponse} con el pedido en estado EN_REPARTO y distribuidor asignado.
     */
    @Transactional
    public PedidoResponse asignarAReparto(String id, String distribuidorId, String observacion) {
        if (distribuidorId == null || distribuidorId.isBlank()) {
            throw new ValidacionException("El identificador del distribuidor es obligatorio para pasar a EN_REPARTO");
        }
        Pedido pedido = obtenerEntidad(id);
        boolean esEfectivoCargado = pedido.getEstado() == EstadoPedido.CARGADO
                && pedido.getFormaDePago() != null
                && pedido.getFormaDePago().getTipoPago() == TipoPago.EFECTIVO;
        if (pedido.getEstado() != EstadoPedido.FACTURADO && !esEfectivoCargado) {
            throw new ConflictoException(
                    "TRANSICION_INVALIDA",
                    "Solo un pedido FACTURADO o en EFECTIVO pendiente de cobro puede pasar a EN_REPARTO. Estado actual: " + pedido.getEstado()
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
     * Transiciona un pedido desde {@link EstadoPedido#EN_REPARTO} a su estado final {@link EstadoPedido#ENTREGADO},
     * registrando el evento en el historial de estados.
     *
     * <p><b>Cuándo se usa:</b> Es invocado por el endpoint {@code POST /pedidos/{id}/entregar} una vez que el distribuidor
     * confirma la entrega física y conformidad de la mercadería en el punto de venta del minorista.</p>
     *
     * @param id Identificador único del pedido entregado.
     * @param observacion Constancia de entrega o comentarios del receptor.
     * @return {@link PedidoResponse} con el pedido en estado ENTREGADO.
     */
    @Transactional
    public PedidoResponse marcarEntregado(String id, String observacion) {
        Pedido pedido = obtenerEntidad(id);
        if (pedido.getEstado() != EstadoPedido.EN_REPARTO && pedido.getEstado() != EstadoPedido.FACTURADO) {
            throw new ConflictoException(
                    "TRANSICION_INVALIDA",
                    "Solo un pedido EN_REPARTO o recién FACTURADO en entrega puede marcarse como ENTREGADO. Estado actual: " + pedido.getEstado()
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
     * Recupera el detalle completo de un pedido por su ID, aplicando control de acceso según el rol autenticado.
     *
     * <p><b>Cuándo se usa:</b> Es invocado por el endpoint {@code GET /pedidos/{id}} al abrir el detalle de un pedido
     * en el panel administrativo, preventa o consulta de cliente.</p>
     *
     * @param id Identificador único del pedido.
     * @return {@link PedidoResponse} con la cabecera, líneas, importes y estado.
     */
    @Transactional(readOnly = true)
    public PedidoResponse buscarPedido(String id) {
        Pedido pedido = obtenerEntidad(id);
        verificarAcceso(pedido);
        return aResponse(pedido);
    }

    /**
     * Valida que el usuario autenticado tenga permisos para acceder u operar sobre el pedido.
     *
     * @param pedido Entidad del pedido a verificar.
     */
    @Transactional(readOnly = true)
    public void validarAccesoPedido(Pedido pedido) {
        verificarAcceso(pedido);
    }

    /**
     * Recupera la línea de tiempo completa y cronológica de cambios de estado que atravesó un pedido.
     *
     * <p><b>Cuándo se usa:</b> Es invocado por el endpoint {@code GET /pedidos/{id}/historial} para auditoría,
     * trazabilidad y seguimiento del pedido en el frontend.</p>
     *
     * @param id Identificador único del pedido.
     * @return Lista de {@link HistorialEstadoPedidoResponse} ordenados cronológicamente.
     */
    @Transactional(readOnly = true)
    public List<HistorialEstadoPedidoResponse> listarHistorialPorPedido(String id) {
        Pedido pedido = obtenerEntidad(id);
        verificarAcceso(pedido);
        return historialEstadoPedidoService.listarHistorialPorPedido(id);
    }

    /**
     * Realiza un listado paginado y multicriterio de pedidos (por estado, preventor, distribuidor, punto de venta,
     * minorista y rango de fechas de carga). Si el usuario autenticado tiene rol {@code PREVENTISTA}, fuerza el filtrado
     * para que solo acceda a los pedidos que tiene a su cargo.
     *
     * <p><b>Cuándo se usa:</b> Es invocado por el endpoint {@code GET /pedidos} en las pantallas de gestión y bandejas
     * de pedidos de administración y preventistas.</p>
     *
     * @param estadoStr Estado del pedido como texto (opcional).
     * @param preventorId ID del preventor a filtrar (opcional).
     * @param distribuidorId ID del distribuidor asignado (opcional).
     * @param puntoDeVentaId ID del punto de venta de destino (opcional).
     * @param minoristaId ID del minorista dueño del punto de venta (opcional).
     * @param desde Fecha mínima de carga (opcional).
     * @param hasta Fecha máxima de carga (opcional).
     * @param pageable Parámetros de paginación y ordenamiento.
     * @return {@link PedidoPage} con la página de resultados y metadatos.
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
     * Lista de manera paginada y resumida los pedidos pertenecientes exclusivamente al minorista logueado.
     *
     * <p><b>Cuándo se usa:</b> Es invocado por el endpoint {@code GET /pedidos/mi-cuenta} en el portal de autogestión
     * del cliente minorista para consultar el estado de sus compras históricas.</p>
     *
     * @param estadoStr Filtro por estado de pedido (opcional).
     * @param pageable Parámetros de paginación.
     * @return {@link PedidoMiCuentaPage} con los pedidos pertenecientes a los puntos de venta del minorista actual.
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
        if (minoristaId == null) {
            return new PedidoMiCuentaPage(
                    List.of(),
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    0,
                    0
            );
        }
        return pedidoLecturaService.listarMiCuenta(estado, minoristaId, pageable);
    }

    /**
     * Transforma una entidad {@link Pedido} a su DTO de salida {@link PedidoResponse}, enriqueciéndolo
     * con datos calculados y nombres representativos mediante {@link PedidoLecturaService}.
     *
     * <p><b>Cuándo se usa:</b> Es utilizado internamente por los métodos de {@link BaseService} y de esta clase
     * cada vez que se debe enviar un pedido como respuesta a la capa de controladores.</p>
     *
     * @param entidad Instancia de {@link Pedido}.
     * @return DTO {@link PedidoResponse} enriquecido.
     */
    @Override
    protected PedidoResponse aResponse(Pedido entidad) {
        return pedidoLecturaService.enriquecerResponse(entidad);
    }

    /**
     * Consulta y recupera todas las entidades de pedidos activas (que no hayan sido eliminadas lógicamente),
     * ordenadas cronológicamente de forma descendente por fecha de carga.
     *
     * <p><b>Cuándo se usa:</b> Es invocado por {@link #listar()} del Template Method de {@link BaseService}.</p>
     *
     * @return Lista de entidades {@link Pedido} activas.
     */
    @Override
    protected List<Pedido> buscarEntidadesActivas() {
        return pedidoRepository.findAllByEliminadoFalseOrderByFechaHoraCargaDesc();
    }

    /**
     * Lista completa sin paginación de todos los pedidos activos en el sistema, convertidos a DTO de respuesta.
     *
     * <p><b>Cuándo se usa:</b> Es la implementación concreta del método de lectura total definido en {@link BaseService},
     * utilizado cuando se requiere la colección global de pedidos sin filtros ni paginado.</p>
     *
     * @return Lista completa de {@link PedidoResponse}.
     */
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

    /**
     * Valida la existencia y precio vigente de cada producto solicitado, calcula los subtotales de cada línea
     * y aplica las reglas de negocio de la promoción provista (descuentos por producto específico o por volumen general).
     *
     * <p><b>Cuándo se usa:</b> Método auxiliar privado invocado al crear un nuevo pedido ({@link #antesDePersistir})
     * o al editarlo ({@link #modificarPedido}), garantizando consistencia en las fórmulas de cálculo de importes.</p>
     *
     * @param items Lista de ítems solicitados con su producto y cantidad.
     * @param codigoPromocion Código de cupón o promoción ingresado (opcional).
     * @return {@link CalculoPedido} con los detalles instanciados, subtotales, descuento y total neto.
     */
    private CalculoPedido calcularLineasYTotales(List<DetallePedidoRequest> items, String codigoPromocion) {
        List<DetallePedido> detalles = new ArrayList<>();
        double subtotalPedido = 0.0;
        int totalUnidadesPedido = 0;

        /// primero creamos todos los detalles y registramos el subtotal
        for (DetallePedidoRequest item : items) {
            if (item == null || item.productoId() == null || item.productoId().isBlank()) {
                throw new ValidacionException("El producto en cada detalle es obligatorio");
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

        /// verificamos la aplicacion de una promo
        Promocion promocion = null;
        double descuento = 0.0;

        if (codigoPromocion != null && !codigoPromocion.isBlank()) {
            promocion = promocionService.obtenerVigentePorCodigoParaPedido(codigoPromocion);

            /// promo por producto especifico
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
                /// promo por cant total de producots
            } else if (promocion.getCantidadMinima() > 0 && totalUnidadesPedido < promocion.getCantidadMinima()) {
                throw new ValidacionException(
                        "No se alcanza la cantidad mínima total de unidades (" + promocion.getCantidadMinima()
                                + ") para aplicar la promoción '" + promocion.getCodigo() + "'"
                );
            }

            descuento = subtotalPedido * (promocion.getPorcentajeDescuento() / 100.0);
        }

        double total = Math.max(0.0, subtotalPedido - descuento);
        /// guardamos en el registro los datos
        return new CalculoPedido(detalles, promocion, subtotalPedido, descuento, total);
    }

    /**
     * Resuelve cuál preventor debe quedar asociado al pedido:
     * 1. Si quien carga es un preventista autenticado, se asocia obligatoriamente a él mismo.
     * 2. Si se especificó un preventor explícito en el request (ej. por un Admin), se busca dicho preventor.
     * 3. Si no se especificó, hereda el preventor que tiene asignado el punto de venta de destino.
     * 4. En su defecto, queda nulo (pedido autogestionado en el portal minorista).
     *
     * <p><b>Cuándo se usa:</b> Durante {@link #antesDePersistir} al dar de alta un pedido nuevo.</p>
     *
     * @param preventorIdRequest ID del preventor enviado en el cuerpo de la petición (opcional).
     * @param puntoDeVenta Entidad del punto de venta receptor.
     * @return Entidad {@link Preventor} asignada o {@code null}.
     */
    private Preventor resolverPreventorParaCreacion(String preventorIdRequest, PuntoDeVenta puntoDeVenta) {
        UsuarioPrincipal principal = obtenerPrincipalActual();
        if (principal != null && principal.getRol() == RolUsuario.PREVENTISTA) {
            return empleadoService.obtenerPreventorPorUsuarioId(principal.getId());
        }
        if (preventorIdRequest != null && !preventorIdRequest.isBlank()) {
            return empleadoService.obtenerPreventor(preventorIdRequest.trim());
        }
        if (puntoDeVenta != null && puntoDeVenta.getPreventor() != null) {
            return puntoDeVenta.getPreventor();
        }
        // Si no se especificó y no hay preventor asignado al punto de venta (ej: pedido autogestionado web por Minorista o Admin)
        return null;
    }

    /**
     * Valida que el usuario autenticado tenga permisos legítimos sobre el pedido en cuestión:
     * - Un {@code PREVENTISTA} solo puede ver o modificar pedidos asignados a su legajo.
     * - Un {@code MINORISTA} solo puede acceder a pedidos cuyos puntos de venta le pertenezcan.
     * - Un {@code ADMIN} tiene visibilidad irrestricta.
     *
     * <p><b>Cuándo se usa:</b> Es llamado en {@link #buscarPedido}, {@link #modificarPedido} y {@link #listarHistorialPorPedido}
     * para aplicar aislamiento de datos por rol y prevenir accesos no autorizados.</p>
     *
     * @param pedido Pedido a evaluar.
     * @throws AccesoDenegadoException Si el usuario no tiene privilegios para acceder al pedido.
     */
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

    /**
     * Convierte una cadena de texto en un valor de la enumeración {@link EstadoPedido},
     * ignorando diferencias entre mayúsculas y minúsculas y recortando espacios en blanco.
     *
     * <p><b>Cuándo se usa:</b> En los métodos de filtrado y consulta ({@link #listarPaginado} y {@link #listarMiCuenta})
     * para convertir los parámetros de consulta HTTP (query params) a valores del enum del dominio.</p>
     *
     * @param estadoStr Texto con el nombre del estado (puede ser nulo o vacío).
     * @return Valor {@link EstadoPedido} parseado o {@code null} si el parámetro estaba en blanco.
     * @throws ValidacionException Si el texto recibido no coincide con ningún estado válido.
     */
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

    /**
     * Publica un evento de dominio {@link PedidoEstadoCambiadoEvent} a través del bus de eventos de Spring.
     *
     * <p><b>Cuándo se usa:</b> Se invoca cada vez que un pedido realiza una transición de estado
     * ({@link #anularPedido}, {@link #marcarSinStock}, {@link #marcarFacturado}, {@link #asignarAReparto}, {@link #marcarEntregado})
     * para que otros componentes del sistema (como WebSocket o notificaciones) reaccionen desacopladamente.</p>
     *
     * @param pedido Instancia del pedido en su nuevo estado.
     * @param usuario Nombre de usuario que ejecutó la acción.
     * @param observacion Motivo o nota aclaratoria del cambio.
     */
    private void publicarEventoEstado(Pedido pedido, String usuario, String observacion) {
        eventPublisher.publishEvent(new PedidoEstadoCambiadoEvent(
                pedido.getId(),
                pedido.getNumeroPedido(),
                pedido.getEstado(),
                usuario,
                observacion
        ));
    }

    /**
     * Extrae el objeto {@link UsuarioPrincipal} a partir del contexto de seguridad de Spring Security.
     *
     * <p><b>Cuándo se usa:</b> Es un método utilitario invocado en verificaciones de autorización, filtros personalizados
     * por rol y resolución de identidades de preventistas o minoristas logueados.</p>
     *
     * @return El {@link UsuarioPrincipal} de la sesión actual, o {@code null} si es una llamada no autenticada.
     */
    private UsuarioPrincipal obtenerPrincipalActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UsuarioPrincipal up) {
            return up;
        }
        return null;
    }

    /**
     * Obtiene el nombre de usuario (username) del actor autenticado en el contexto de seguridad.
     * Si no hay un usuario autenticado explícito o se trata de una tarea programada interna, retorna {@code "sistema"}.
     *
     * <p><b>Cuándo se usa:</b> Al registrar entradas de auditoría en {@link HistorialEstadoPedidoService}
     * y al emitir eventos de cambio de estado de pedidos.</p>
     *
     * @return Nombre de usuario del operador actual o {@code "sistema"}.
     */
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

    /**
     * Objeto de transferencia interno e inmutable utilizado para consolidar el resultado
     * de los cálculos financieros y validaciones de promociones de un pedido.
     *
     * <p><b>Cuándo se usa:</b> Retornado por {@link #calcularLineasYTotales} y consumido por
     * {@link #antesDePersistir} y {@link #modificarPedido}.</p>
     *
     * @param detalles Colección de entidades de detalle calculadas con sus precios vigentes.
     * @param promocion Promoción aplicada o {@code null}.
     * @param subtotal Suma algebraica de cantidades por precios unitarios vigentes.
     * @param descuento Importe monetario descontado según la promoción.
     * @param total Monto final resultante (subtotal menos descuento).
     */
    private record CalculoPedido(
            List<DetallePedido> detalles,
            Promocion promocion,
            double subtotal,
            double descuento,
            double total
    ) {
    }
}
