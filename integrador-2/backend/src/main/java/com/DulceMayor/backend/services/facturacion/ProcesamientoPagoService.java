package com.DulceMayor.backend.services.facturacion;

import com.DulceMayor.backend.config.ConflictoException;
import com.DulceMayor.backend.config.RecursoNoEncontradoException;
import com.DulceMayor.backend.config.ValidacionException;
import com.DulceMayor.backend.dtos.facturacion.AprobarTransferenciaRequest;
import com.DulceMayor.backend.dtos.facturacion.DatosBancariosEmpresaResponse;
import com.DulceMayor.backend.dtos.facturacion.InformarTransferenciaRequest;
import com.DulceMayor.backend.dtos.facturacion.IniciarPagoQrResponse;
import com.DulceMayor.backend.dtos.facturacion.MercadoPagoWebhookPayload;
import com.DulceMayor.backend.dtos.facturacion.PagoAprobadoEvent;
import com.DulceMayor.backend.dtos.facturacion.PagoDetalleResponse;
import com.DulceMayor.backend.dtos.facturacion.PagoRechazadoEvent;
import com.DulceMayor.backend.dtos.facturacion.RechazarPagoRequest;
import com.DulceMayor.backend.dtos.facturacion.RegistrarCobroEfectivoRequest;
import com.DulceMayor.backend.entities.facturacion.EstadoPago;
import com.DulceMayor.backend.entities.facturacion.Factura;
import com.DulceMayor.backend.entities.facturacion.FormaDePago;
import com.DulceMayor.backend.entities.facturacion.Pago;
import com.DulceMayor.backend.entities.facturacion.TipoPago;
import com.DulceMayor.backend.entities.facturacion.WebhookNotificationLog;
import com.DulceMayor.backend.entities.pedido.EstadoPedido;
import com.DulceMayor.backend.entities.pedido.Pedido;
import com.DulceMayor.backend.repositories.facturacion.WebhookNotificationLogRepository;
import com.DulceMayor.backend.services.facturacion.adapter.MercadoPagoPaymentDto;
import com.DulceMayor.backend.services.facturacion.adapter.MercadoPagoPort;
import com.DulceMayor.backend.services.facturacion.strategy.PagoStrategy;
import com.DulceMayor.backend.services.facturacion.strategy.PagoStrategyFactory;
import com.DulceMayor.backend.services.pedido.HistorialEstadoPedidoService;
import com.DulceMayor.backend.services.pedido.PedidoService;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio orquestador central para la gestión transaccional de pagos (Mercado Pago QR,
 * Transferencia manual y Efectivo contra entrega) y su integración atómica con facturación electrónica y stock.
 *
 * <p>Cumple con {@code 03-arquitectura.md} y {@code GENERAL.md} coordinando los servicios sin dependencias circulares.</p>
 */
@Service
public class ProcesamientoPagoService {

    private static final Logger log = LoggerFactory.getLogger(ProcesamientoPagoService.class);

    private final PagoService pagoService;
    private final PedidoService pedidoService;
    private final FacturaService facturaService;
    private final FormaDePagoService formaDePagoService;
    private final HistorialEstadoPedidoService historialEstadoPedidoService;
    private final PagoStrategyFactory pagoStrategyFactory;
    private final MercadoPagoPort mercadoPagoPort;
    private final WebhookNotificationLogService webhookNotificationLogService;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${app.empresa.banco.nombre:Banco Galicia}")
    private String bancoNombre;

    @Value("${app.empresa.banco.titular:Dulce Mayor S.A.}")
    private String bancoTitular;

    @Value("${app.empresa.banco.cuit:30-71234567-8}")
    private String bancoCuit;

    @Value("${app.empresa.banco.cbu:0070123420000012345678}")
    private String bancoCbu;

    @Value("${app.empresa.banco.alias:DULCE.MAYOR.MZA}")
    private String bancoAlias;

    public ProcesamientoPagoService(
            PagoService pagoService,
            PedidoService pedidoService,
            FacturaService facturaService,
            FormaDePagoService formaDePagoService,
            HistorialEstadoPedidoService historialEstadoPedidoService,
            PagoStrategyFactory pagoStrategyFactory,
            MercadoPagoPort mercadoPagoPort,
            WebhookNotificationLogService webhookNotificationLogService,
            ApplicationEventPublisher eventPublisher) {
        this.pagoService = pagoService;
        this.pedidoService = pedidoService;
        this.facturaService = facturaService;
        this.formaDePagoService = formaDePagoService;
        this.historialEstadoPedidoService = historialEstadoPedidoService;
        this.pagoStrategyFactory = pagoStrategyFactory;
        this.mercadoPagoPort = mercadoPagoPort;
        this.webhookNotificationLogService = webhookNotificationLogService;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Inicia o regenera el código QR dinámico de Mercado Pago para un pedido.
     */
    @Transactional
    public IniciarPagoQrResponse iniciarPagoQr(String pedidoId) {
        Pedido pedido = pedidoService.obtenerEntidad(pedidoId);
        pedidoService.validarAccesoPedido(pedido);

        if (pedido.getEstado() == EstadoPedido.ANULADO) {
            throw new ConflictoException("CONFLICTO", "No se puede iniciar el pago de un pedido anulado");
        }
        if (pedido.getEstado() == EstadoPedido.FACTURADO || pedido.getEstado() == EstadoPedido.ENTREGADO) {
            throw new ConflictoException("CONFLICTO", "El pedido ya se encuentra facturado o completado");
        }

        FormaDePago formaPago = formaDePagoService.obtenerOCrearPorTipo(TipoPago.BILLETERA_VIRTUAL);
        pedido.setFormaDePago(formaPago);

        PagoStrategy estrategia = pagoStrategyFactory.obtenerEstrategia(TipoPago.BILLETERA_VIRTUAL);
        IniciarPagoQrResponse respuesta = estrategia.iniciarPago(pedido);

        historialEstadoPedidoService.registrarEstado(
                pedido,
                pedido.getEstado(),
                "sistema",
                "Código QR dinámico de Mercado Pago generado (vence: " + respuesta.fechaExpiracion() + ")"
        );

        return respuesta;
    }

    /**
     * Limpieza programada de pedidos abandonados generados mediante Mercado Pago QR
     * que expiraron sin registrar acreditación de pago.
     */
    @Scheduled(fixedDelayString = "${app.mercadopago.cleanup-interval-ms:60000}")
    @Transactional
    public void limpiarPedidosQrExpirados() {
        LocalDateTime ahora = LocalDateTime.now();
        List<Pedido> pedidosExpirados = pedidoService.listarPedidosQrExpirados(ahora);
        for (Pedido pedido : pedidosExpirados) {
            try {
                if (!pagoService.tienePagoAprobadoPorPedido(pedido.getId())) {
                    log.info("Limpieza automática: cancelando pedido expirado con QR abandonado: ID={}, NroPedido={}",
                            pedido.getId(), pedido.getNumeroPedido());
                    pedidoService.eliminar(pedido.getId());
                }
            } catch (Exception ex) {
                log.error("Error al limpiar pedido QR expirado ID {}: {}", pedido.getId(), ex.getMessage());
            }
        }
    }

    /**
     * Consulta el estado de pago del QR para un pedido y retorna la imagen Base64 del QR real de Mercado Pago.
     */
    @Transactional
    public IniciarPagoQrResponse consultarEstadoQr(String pedidoId) {
        Pedido pedido = pedidoService.obtenerEntidad(pedidoId);
        pedidoService.validarAccesoPedido(pedido);
        List<Pago> pagos = pagoService.buscarEntidadesPorPedido(pedidoId);
        boolean estaAprobado = pagos.stream().anyMatch(p -> p.getEstadoPago() == EstadoPago.APROBADO)
                || pedido.getEstado() == EstadoPedido.FACTURADO
                || pedido.getEstado() == EstadoPedido.EN_REPARTO
                || pedido.getEstado() == EstadoPedido.ENTREGADO;

        String qrDataReal = pedido.getQrData();
        if ((qrDataReal == null || qrDataReal.isBlank()) && mercadoPagoPort != null) {
            String titulo = "Pedido #" + pedido.getNumeroPedido() + " - Dulce Mayor";
            var ordenMp = mercadoPagoPort.crearOrdenQrDinamica(
                    pedido.getId(),
                    titulo,
                    pedido.getTotal(),
                    15
            );
            if (ordenMp != null && ordenMp.qrData() != null && !ordenMp.qrData().isBlank()) {
                qrDataReal = ordenMp.qrData();
                pedido.setQrData(qrDataReal);
            }
        }

        String qrImagenBase64 = (qrDataReal != null && !qrDataReal.isBlank())
                ? QrCodeGeneratorHelper.generarQrBase64(qrDataReal)
                : null;

        LocalDateTime exp = pedido.getFechaExpiracionPago();
        long segundos = (exp != null) ? ChronoUnit.SECONDS.between(LocalDateTime.now(), exp) : 0;

        return new IniciarPagoQrResponse(
                pedido.getId(),
                pedido.getNumeroPedido(),
                pedido.getTotal(),
                estaAprobado ? "APROBADO" : "PENDIENTE",
                qrImagenBase64,
                exp,
                Math.max(0, segundos)
        );
    }

    /**
     * Procesa la notificación Webhook entrante de Mercado Pago con validación de firma e idempotencia.
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void procesarWebhookMercadoPago(
            String xSignature,
            String xRequestId,
            MercadoPagoWebhookPayload payload) {
        procesarWebhookMercadoPago(xSignature, xRequestId, null, null, null, null, payload);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void procesarWebhookMercadoPago(
            String xSignature,
            String xRequestId,
            String dataIdParam,
            String idParam,
            String typeParam,
            String topicParam,
            MercadoPagoWebhookPayload payload) {

        String dataId = null;
        if (dataIdParam != null && !dataIdParam.isBlank()) {
            dataId = dataIdParam.trim();
        } else if (idParam != null && !idParam.isBlank()) {
            dataId = idParam.trim();
        } else if (payload != null && payload.data() != null && payload.data().id() != null) {
            dataId = payload.data().id().trim();
        } else if (payload != null && payload.id() != null) {
            dataId = String.valueOf(payload.id());
        }

        if (dataId == null || dataId.isBlank()) {
            log.warn("Webhook de Mercado Pago recibido con payload o data.id vacío");
            return;
        }

        String action = payload != null && payload.action() != null
                ? payload.action()
                : (topicParam != null ? topicParam : "payment.updated");
        String type = typeParam != null && !typeParam.isBlank()
                ? typeParam
                : (payload != null && payload.type() != null ? payload.type() : topicParam);
        String rawBody = payload != null ? payload.toString() : ("data.id=" + dataId + ", type=" + type);

        // 1. Validar firma criptográfica
        boolean firmaValida = mercadoPagoPort.validarFirmaWebhook(xSignature, xRequestId, dataId);
        if (!firmaValida) {
            log.warn("Firma de webhook x-signature no validada para dataId: {}. Procediendo con verificación activa en API oficial de MP...", dataId);
        }

        // 2. Control estricto de Idempotencia
        if (webhookNotificationLogService.yaFueProcesado(dataId)) {
            log.info("Webhook para dataId {} ya fue procesado anteriormente (Idempotencia)", dataId);
            return;
        }

        WebhookNotificationLog logEntry = webhookNotificationLogService.registrarRecibido(
                dataId, xRequestId, action, type, xSignature, rawBody
        );

        // 3. Verificación Activa: Consulta obligatoria a la API oficial de Mercado Pago
        Optional<MercadoPagoPaymentDto> pagoDtoOpt = mercadoPagoPort.consultarPago(dataId);
        if (pagoDtoOpt.isEmpty()) {
            log.warn("No se pudo obtener información de pago en Mercado Pago para ID: {}", dataId);
            webhookNotificationLogService.marcarFallido(logEntry, "Pago no encontrado en Mercado Pago");
            return;
        }

        MercadoPagoPaymentDto pagoDto = pagoDtoOpt.get();

        if ("approved".equalsIgnoreCase(pagoDto.status())) {
            String pedidoId = pagoDto.externalReference();
            if (pedidoId == null || pedidoId.isBlank()) {
                log.warn("El pago {} no posee external_reference (pedidoId)", dataId);
                return;
            }

            try {
                Pedido pedido = pedidoService.obtenerEntidad(pedidoId);
                confirmarCobroYFacturar(pedido, pagoDto.id(), pagoDto.transactionAmount(),
                        TipoPago.BILLETERA_VIRTUAL, "Mercado Pago Webhook (" + dataId + ")");

                webhookNotificationLogService.marcarProcesado(logEntry);
            } catch (Exception ex) {
                log.error("Error al confirmar cobro y facturar para pedido {}: {}", pedidoId, ex.getMessage());
                webhookNotificationLogService.marcarFallido(logEntry, ex.getMessage());
                throw ex;
            }
        } else if ("rejected".equalsIgnoreCase(pagoDto.status()) || "cancelled".equalsIgnoreCase(pagoDto.status())) {
            log.info("Pago {} en Mercado Pago en estado: {}", dataId, pagoDto.status());
            if (pagoDto.externalReference() != null) {
                eventPublisher.publishEvent(new PagoRechazadoEvent(
                        pagoDto.externalReference(),
                        "Pago rechazado por Mercado Pago: " + pagoDto.statusDetail()
                ));
            }
            webhookNotificationLogService.marcarProcesado(logEntry);
        }
    }

    /**
     * El cliente minorista informa que realizó la transferencia bancaria con el número de comprobante.
     */
    @Transactional
    public void informarTransferencia(String pedidoId, InformarTransferenciaRequest request, String usuarioActual) {
        Pedido pedido = pedidoService.obtenerEntidad(pedidoId);
        pedidoService.validarAccesoPedido(pedido);

        if (pedido.getEstado() == EstadoPedido.ANULADO || pedido.getEstado() == EstadoPedido.FACTURADO) {
            throw new ConflictoException("CONFLICTO", "El pedido no se encuentra disponible para registrar pagos");
        }

        FormaDePago formaPago = formaDePagoService.obtenerOCrearPorTipo(TipoPago.TRANSFERENCIA);
        pedido.setFormaDePago(formaPago);

        PagoStrategy estrategia = pagoStrategyFactory.obtenerEstrategia(TipoPago.TRANSFERENCIA);
        estrategia.iniciarPago(pedido);

        Pago pagoPendiente = Pago.builder()
                .pedido(pedido)
                .formaDePago(formaPago)
                .monto(pedido.getTotal())
                .estadoPago(EstadoPago.PENDIENTE)
                .fechaPago(LocalDateTime.now())
                .referencia(request.comprobante())
                .observacion("Banco emisor: " + (request.bancoEmisor() != null ? request.bancoEmisor() : "-")
                        + " | Obs: " + (request.observacion() != null ? request.observacion() : ""))
                .usuarioAuditor(usuarioActual)
                .eliminado(false)
                .build();
        pagoService.guardarPago(pagoPendiente);

        historialEstadoPedidoService.registrarEstado(
                pedido,
                pedido.getEstado(),
                usuarioActual,
                "Transferencia informada por cliente. Comprobante: " + request.comprobante()
        );
    }

    /**
     * El personal administrativo aprueba manualmente una transferencia bancaria verificada en el banco.
     */
    @Transactional
    public void aprobarTransferencia(String pagoId, AprobarTransferenciaRequest request, String usuarioAdmin) {
        Pago pago = pagoService.obtenerEntidad(pagoId);
        if (pago.getEstadoPago() != EstadoPago.PENDIENTE) {
            throw new ConflictoException("TRANSICION_INVALIDA", "Solo se pueden aprobar pagos en estado PENDIENTE");
        }

        Pedido pedido = pago.getPedido();
        if (pedido == null) {
            throw new ConflictoException("CONFLICTO", "El pago no tiene pedido asociado");
        }

        pago.setEstadoPago(EstadoPago.APROBADO);
        pago.setUsuarioAuditor(usuarioAdmin);
        if (request != null && request.observacion() != null && !request.observacion().isBlank()) {
            pago.setObservacion(pago.getObservacion() + " | Aprobación: " + request.observacion().trim());
        }

        // Emisión oficial de factura y descuento de stock
        Factura factura = facturaService.emitirFacturaConPagoAprobado(pedido);
        pago.setFactura(factura);
        pagoService.guardarPago(pago);

        pedidoService.marcarFacturado(pedido, "Pago por transferencia bancaria aprobado por: " + usuarioAdmin);

        historialEstadoPedidoService.registrarEstado(
                pedido,
                EstadoPedido.FACTURADO,
                usuarioAdmin,
                "Transferencia verificada y aprobada en cuenta bancaria"
        );

        eventPublisher.publishEvent(new PagoAprobadoEvent(
                pago.getId(),
                pedido.getId(),
                factura.getId(),
                pago.getMonto(),
                TipoPago.TRANSFERENCIA,
                pago.getReferencia()
        ));
    }

    /**
     * Rechazo de un pago o transferencia por datos inválidos o comprobante apócrifo.
     */
    @Transactional
    public void rechazarPago(String pagoId, RechazarPagoRequest request, String usuarioAdmin) {
        Pago pago = pagoService.obtenerEntidad(pagoId);
        if (pago.getEstadoPago() != EstadoPago.PENDIENTE) {
            throw new ConflictoException("TRANSICION_INVALIDA", "Solo se pueden rechazar pagos en estado PENDIENTE");
        }

        pago.setEstadoPago(EstadoPago.RECHAZADO);
        pago.setUsuarioAuditor(usuarioAdmin);
        pago.setObservacion(pago.getObservacion() + " | Rechazado: " + request.motivo());
        pagoService.guardarPago(pago);

        Pedido pedido = pago.getPedido();
        if (pedido != null) {
            pedido.setFechaExpiracionPago(null);
            historialEstadoPedidoService.registrarEstado(
                    pedido,
                    pedido.getEstado(),
                    usuarioAdmin,
                    "Pago rechazado por tesorería: " + request.motivo()
            );
            eventPublisher.publishEvent(new PagoRechazadoEvent(pedido.getId(), request.motivo()));
        }
    }

    /**
     * Registra la cobranza física en efectivo contra entrega por parte del distribuidor.
     */
    @Transactional
    public void registrarCobroEfectivoEnEntrega(RegistrarCobroEfectivoRequest request, String usuarioDistribuidor) {
        if (request == null || request.pedidoId() == null || request.pedidoId().isBlank()) {
            throw new ValidacionException("El identificador del pedido es obligatorio");
        }

        Pedido pedido = pedidoService.obtenerEntidad(request.pedidoId());
        if (pedido.getEstado() != EstadoPedido.EN_REPARTO && pedido.getEstado() != EstadoPedido.FACTURADO) {
            throw new ConflictoException("TRANSICION_INVALIDA",
                    "Solo se puede cobrar en efectivo pedidos EN_REPARTO o FACTURADOS. Estado actual: " + pedido.getEstado());
        }

        double montoCobrado = request.monto() != null ? request.monto() : pedido.getTotal();
        if (Math.abs(montoCobrado - pedido.getTotal()) > 0.01) {
            throw new ValidacionException("El monto cobrado en efectivo ($" + montoCobrado +
                    ") no coincide con el total exacto del pedido ($" + pedido.getTotal() + ")");
        }

        FormaDePago formaPago = formaDePagoService.obtenerOCrearPorTipo(TipoPago.EFECTIVO);
        pedido.setFormaDePago(formaPago);

        Factura factura = facturaService.emitirFacturaConPagoAprobado(pedido);

        Pago pago = Pago.builder()
                .pedido(pedido)
                .factura(factura)
                .formaDePago(formaPago)
                .monto(montoCobrado)
                .estadoPago(EstadoPago.APROBADO)
                .fechaPago(LocalDateTime.now())
                .referencia("Cobro contra entrega")
                .observacion(request.observacion() != null ? request.observacion() : "Cobrado por distribuidor en comercio")
                .usuarioAuditor(usuarioDistribuidor)
                .eliminado(false)
                .build();
        pagoService.guardarPago(pago);

        pedidoService.marcarFacturado(
                pedido,
                "Pedido facturado en efectivo por distribuidor: " + usuarioDistribuidor
        );

        pedidoService.marcarEntregado(
                pedido.getId(),
                request.observacion() != null && !request.observacion().isBlank()
                        ? request.observacion()
                        : "Pedido entregado y cobrado en efectivo en punto de venta"
        );

        eventPublisher.publishEvent(new PagoAprobadoEvent(
                pago.getId(),
                pedido.getId(),
                factura.getId(),
                pago.getMonto(),
                TipoPago.EFECTIVO,
                "Efectivo contra entrega"
        ));
    }

    /**
     * Método central de acreditación y emisión atómica de comprobante fiscal contra pago confirmado.
     */
    private void confirmarCobroYFacturar(
            Pedido pedido,
            String mpPaymentId,
            Double montoCobrado,
            TipoPago tipoPago,
            String referencia) {

        FormaDePago formaPago = formaDePagoService.obtenerOCrearPorTipo(tipoPago);
        pedido.setFormaDePago(formaPago);

        Factura factura = facturaService.emitirFacturaConPagoAprobado(pedido);

        double montoFinal = (montoCobrado != null && montoCobrado > 0) ? montoCobrado : pedido.getTotal();

        Pago pago = Pago.builder()
                .pedido(pedido)
                .factura(factura)
                .formaDePago(formaPago)
                .monto(montoFinal)
                .estadoPago(EstadoPago.APROBADO)
                .fechaPago(LocalDateTime.now())
                .referencia(referencia)
                .mpPaymentId(mpPaymentId)
                .mpStatus("approved")
                .usuarioAuditor("Mercado Pago Webhook")
                .eliminado(false)
                .build();
        pagoService.guardarPago(pago);

        pedidoService.marcarFacturado(pedido, "Cobro acreditado automáticamente vía " + tipoPago.name() + " (" + referencia + ")");

        eventPublisher.publishEvent(new PagoAprobadoEvent(
                pago.getId(),
                pedido.getId(),
                factura.getId(),
                montoFinal,
                tipoPago,
                referencia
        ));
    }

    /**
     * Consulta de datos bancarios oficiales para el portal del cliente.
     */
    @Transactional(readOnly = true)
    public DatosBancariosEmpresaResponse obtenerDatosBancariosEmpresa() {
        return new DatosBancariosEmpresaResponse(
                bancoNombre,
                bancoTitular,
                bancoCuit,
                bancoCbu,
                bancoAlias,
                24
        );
    }

    /**
     * Lista las transferencias bancarias pendientes de verificación para el dashboard administrativo.
     */
    @Transactional(readOnly = true)
    public List<PagoDetalleResponse> listarTransferenciasPendientes() {
        List<Pago> pagos = pagoService.listarTransferenciasPendientes();
        List<PagoDetalleResponse> resultado = new ArrayList<>();

        for (Pago p : pagos) {
            Pedido ped = p.getPedido();
            String pdvNombre = "-";
            String minoristaNombre = "-";
            Long numPedido = null;
            if (ped != null) {
                numPedido = ped.getNumeroPedido();
                if (ped.getPuntoDeVenta() != null) {
                    pdvNombre = ped.getPuntoDeVenta().getNombreComercio();
                    if (ped.getPuntoDeVenta().getMinorista() != null) {
                        minoristaNombre = (ped.getPuntoDeVenta().getMinorista().getNombre() + " "
                                + ped.getPuntoDeVenta().getMinorista().getApellido()).trim();
                    }
                }
            }

            resultado.add(new PagoDetalleResponse(
                    p.getId(),
                    ped != null ? ped.getId() : null,
                    numPedido,
                    p.getFactura() != null ? p.getFactura().getId() : null,
                    p.getFactura() != null ? p.getFactura().getNumeroFactura() : null,
                    pdvNombre,
                    minoristaNombre,
                    p.getFormaDePago() != null ? p.getFormaDePago().getTipoPago() : TipoPago.TRANSFERENCIA,
                    p.getFormaDePago() != null ? p.getFormaDePago().getTipoPago().name() : "TRANSFERENCIA",
                    p.getMonto(),
                    p.getEstadoPago(),
                    p.getFechaPago(),
                    p.getReferencia(),
                    p.getMpPaymentId(),
                    p.getMpStatus(),
                    p.getObservacion(),
                    p.getUsuarioAuditor()
            ));
        }

        return resultado;
    }
}
