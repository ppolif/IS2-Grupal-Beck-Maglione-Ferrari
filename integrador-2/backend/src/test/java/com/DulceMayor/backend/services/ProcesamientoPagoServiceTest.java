package com.DulceMayor.backend.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.DulceMayor.backend.dtos.facturacion.AprobarTransferenciaRequest;
import com.DulceMayor.backend.dtos.facturacion.InformarTransferenciaRequest;
import com.DulceMayor.backend.dtos.facturacion.IniciarPagoQrResponse;
import com.DulceMayor.backend.dtos.facturacion.MercadoPagoWebhookPayload;
import com.DulceMayor.backend.dtos.facturacion.RechazarPagoRequest;
import com.DulceMayor.backend.dtos.facturacion.RegistrarCobroEfectivoRequest;
import com.DulceMayor.backend.entities.facturacion.EstadoPago;
import com.DulceMayor.backend.entities.facturacion.Factura;
import com.DulceMayor.backend.entities.facturacion.FormaDePago;
import com.DulceMayor.backend.entities.facturacion.Pago;
import com.DulceMayor.backend.entities.facturacion.TipoPago;
import com.DulceMayor.backend.entities.pedido.EstadoPedido;
import com.DulceMayor.backend.entities.pedido.Pedido;
import com.DulceMayor.backend.services.facturacion.FacturaService;
import com.DulceMayor.backend.services.facturacion.FormaDePagoService;
import com.DulceMayor.backend.services.facturacion.PagoService;
import com.DulceMayor.backend.services.facturacion.ProcesamientoPagoService;
import com.DulceMayor.backend.services.facturacion.WebhookNotificationLogService;
import com.DulceMayor.backend.services.facturacion.adapter.MercadoPagoPaymentDto;
import com.DulceMayor.backend.services.facturacion.adapter.MercadoPagoPort;
import com.DulceMayor.backend.services.facturacion.strategy.PagoStrategy;
import com.DulceMayor.backend.services.facturacion.strategy.PagoStrategyFactory;
import com.DulceMayor.backend.services.pedido.HistorialEstadoPedidoService;
import com.DulceMayor.backend.services.pedido.PedidoService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class ProcesamientoPagoServiceTest {

    @Mock
    private PagoService pagoService;

    @Mock
    private PedidoService pedidoService;

    @Mock
    private FacturaService facturaService;

    @Mock
    private FormaDePagoService formaDePagoService;

    @Mock
    private HistorialEstadoPedidoService historialEstadoPedidoService;

    @Mock
    private PagoStrategyFactory pagoStrategyFactory;

    @Mock
    private MercadoPagoPort mercadoPagoPort;

    @Mock
    private WebhookNotificationLogService webhookNotificationLogService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private PagoStrategy qrStrategy;

    @Mock
    private PagoStrategy trfStrategy;

    private ProcesamientoPagoService service;

    @BeforeEach
    void setUp() {
        service = new ProcesamientoPagoService(
                pagoService,
                pedidoService,
                facturaService,
                formaDePagoService,
                historialEstadoPedidoService,
                pagoStrategyFactory,
                mercadoPagoPort,
                webhookNotificationLogService,
                eventPublisher
        );
    }

    @Test
    @DisplayName("iniciarPagoQr: genera QR interoperable y registra evento en historial")
    void iniciarPagoQrExitoso() {
        Pedido pedido = Pedido.builder()
                .id("ped-100")
                .numeroPedido(100L)
                .total(15000.0)
                .estado(EstadoPedido.CARGADO)
                .build();

        FormaDePago formaQr = FormaDePago.builder()
                .id("fp-qr")
                .tipoPago(TipoPago.BILLETERA_VIRTUAL)
                .build();

        IniciarPagoQrResponse qrResponse = new IniciarPagoQrResponse(
                "ped-100",
                100L,
                15000.0,
                "00020101021243650016COM.MERCADOLIBRE020130636384",
                "data:image/png;base64,iVBORw0KG...",
                LocalDateTime.now().plusMinutes(15),
                900L
        );

        when(pedidoService.obtenerEntidad("ped-100")).thenReturn(pedido);
        when(formaDePagoService.obtenerOCrearPorTipo(TipoPago.BILLETERA_VIRTUAL)).thenReturn(formaQr);
        when(pagoStrategyFactory.obtenerEstrategia(TipoPago.BILLETERA_VIRTUAL)).thenReturn(qrStrategy);
        when(qrStrategy.iniciarPago(pedido)).thenReturn(qrResponse);

        IniciarPagoQrResponse res = service.iniciarPagoQr("ped-100");

        assertNotNull(res);
        assertEquals("ped-100", res.pedidoId());
        assertEquals(100L, res.numeroPedido());
        assertNotNull(res.qrData());
        assertNotNull(res.qrImagenBase64());
        assertEquals(formaQr, pedido.getFormaDePago());
        verify(historialEstadoPedidoService).registrarEstado(any(), any(), any(), any());
    }

    @Test
    @DisplayName("procesarWebhookMercadoPago: ignora notificaciones duplicadas (idempotencia)")
    void webhookIdempotencia() {
        MercadoPagoWebhookPayload payload = new MercadoPagoWebhookPayload(
                "payment.created",
                "v1",
                new MercadoPagoWebhookPayload.Data("pay-999"),
                "2026-11-09T10:00:00Z",
                123L,
                true,
                "payment",
                "user-1"
        );

        when(mercadoPagoPort.validarFirmaWebhook("ts=123,v1=abc", "req-1", "pay-999")).thenReturn(true);
        when(webhookNotificationLogService.yaFueProcesado("pay-999")).thenReturn(true);

        service.procesarWebhookMercadoPago("ts=123,v1=abc", "req-1", payload);

        verify(mercadoPagoPort, never()).consultarPago(anyString());
        verify(pagoService, never()).guardarPago(any());
    }

    @Test
    @DisplayName("procesarWebhookMercadoPago: pago approved genera Factura y marca pedido como FACTURADO")
    void webhookPagoAprobado() {
        MercadoPagoWebhookPayload payload = new MercadoPagoWebhookPayload(
                "payment.created",
                "v1",
                new MercadoPagoWebhookPayload.Data("pay-777"),
                "2026-11-09T10:00:00Z",
                456L,
                true,
                "payment",
                "user-1"
        );

        Pedido pedido = Pedido.builder()
                .id("ped-200")
                .numeroPedido(200L)
                .total(25000.0)
                .estado(EstadoPedido.CARGADO)
                .build();

        Factura factura = Factura.builder()
                .id("fac-200")
                .numeroFactura(2001L)
                .total(25000.0)
                .build();

        MercadoPagoPaymentDto mpPayment = new MercadoPagoPaymentDto(
                "pay-777",
                "approved",
                "accredited",
                25000.0,
                "ARS",
                "ped-200",
                LocalDateTime.now()
        );

        FormaDePago formaQr = FormaDePago.builder()
                .id("fp-qr")
                .tipoPago(TipoPago.BILLETERA_VIRTUAL)
                .build();

        when(mercadoPagoPort.validarFirmaWebhook("ts=123,v1=abc", "req-2", "pay-777")).thenReturn(true);
        when(webhookNotificationLogService.yaFueProcesado("pay-777")).thenReturn(false);
        when(mercadoPagoPort.consultarPago("pay-777")).thenReturn(Optional.of(mpPayment));
        when(pedidoService.obtenerEntidad("ped-200")).thenReturn(pedido);
        when(formaDePagoService.obtenerOCrearPorTipo(TipoPago.BILLETERA_VIRTUAL)).thenReturn(formaQr);
        when(facturaService.emitirFacturaConPagoAprobado(pedido)).thenReturn(factura);

        service.procesarWebhookMercadoPago("ts=123,v1=abc", "req-2", payload);

        verify(pagoService).guardarPago(any(Pago.class));
        verify(facturaService).emitirFacturaConPagoAprobado(pedido);
        verify(pedidoService).marcarFacturado(any(), any());
        verify(webhookNotificationLogService).marcarProcesado(any());
    }

    @Test
    @DisplayName("informarTransferencia: guarda pago PENDIENTE con datos del comprobante")
    void informarTransferenciaExitoso() {
        Pedido pedido = Pedido.builder()
                .id("ped-300")
                .total(50000.0)
                .estado(EstadoPedido.CARGADO)
                .build();

        FormaDePago formaTrf = FormaDePago.builder()
                .id("fp-trf")
                .tipoPago(TipoPago.TRANSFERENCIA)
                .build();

        when(pedidoService.obtenerEntidad("ped-300")).thenReturn(pedido);
        when(formaDePagoService.obtenerOCrearPorTipo(TipoPago.TRANSFERENCIA)).thenReturn(formaTrf);
        when(pagoStrategyFactory.obtenerEstrategia(TipoPago.TRANSFERENCIA)).thenReturn(trfStrategy);

        InformarTransferenciaRequest req = new InformarTransferenciaRequest(
                "TRF-123456",
                "Banco Santander",
                "Comprobante adjunto"
        );

        service.informarTransferencia("ped-300", req, "cliente_test");

        verify(pagoService).guardarPago(any(Pago.class));
        verify(historialEstadoPedidoService).registrarEstado(any(), any(), any(), any());
    }

    @Test
    @DisplayName("aprobarTransferencia: confirma pago manual, genera Factura y marca FACTURADO")
    void aprobarTransferenciaExitoso() {
        Pedido pedido = Pedido.builder()
                .id("ped-400")
                .total(18000.0)
                .estado(EstadoPedido.CARGADO)
                .build();

        Pago pago = Pago.builder()
                .id("pag-1")
                .pedido(pedido)
                .monto(18000.0)
                .estadoPago(EstadoPago.PENDIENTE)
                .build();

        Factura factura = Factura.builder()
                .id("fac-400")
                .total(18000.0)
                .build();

        when(pagoService.obtenerEntidad("pag-1")).thenReturn(pago);
        when(facturaService.emitirFacturaConPagoAprobado(pedido)).thenReturn(factura);

        service.aprobarTransferencia("pag-1", new AprobarTransferenciaRequest("Acreditado en Galicia"), "admin_tesoreria");

        assertEquals(EstadoPago.APROBADO, pago.getEstadoPago());
        assertEquals(factura, pago.getFactura());
        verify(pagoService).guardarPago(pago);
        verify(pedidoService).marcarFacturado(any(), any());
    }

    @Test
    @DisplayName("rechazarPago: cancela pago y no emite factura")
    void rechazarPagoExitoso() {
        Pedido pedido = Pedido.builder()
                .id("ped-500")
                .total(12000.0)
                .estado(EstadoPedido.CARGADO)
                .build();

        Pago pago = Pago.builder()
                .id("pag-2")
                .pedido(pedido)
                .monto(12000.0)
                .estadoPago(EstadoPago.PENDIENTE)
                .observacion("Transferencia inicial")
                .build();

        when(pagoService.obtenerEntidad("pag-2")).thenReturn(pago);

        service.rechazarPago("pag-2", new RechazarPagoRequest("Fondos no acreditados"), "admin_tesoreria");

        assertEquals(EstadoPago.RECHAZADO, pago.getEstadoPago());
        verify(facturaService, never()).emitirFacturaConPagoAprobado(any());
        verify(pagoService).guardarPago(pago);
    }

    @Test
    @DisplayName("registrarCobroEfectivoEnEntrega: valida monto exacto, genera Factura y cierra pedido")
    void registrarCobroEfectivoExitoso() {
        Pedido pedido = Pedido.builder()
                .id("ped-600")
                .total(8500.0)
                .estado(EstadoPedido.EN_REPARTO)
                .build();

        FormaDePago formaEfectivo = FormaDePago.builder()
                .id("fp-efe")
                .tipoPago(TipoPago.EFECTIVO)
                .build();

        Factura factura = Factura.builder()
                .id("fac-600")
                .total(8500.0)
                .build();

        when(pedidoService.obtenerEntidad("ped-600")).thenReturn(pedido);
        when(formaDePagoService.obtenerOCrearPorTipo(TipoPago.EFECTIVO)).thenReturn(formaEfectivo);
        when(facturaService.emitirFacturaConPagoAprobado(pedido)).thenReturn(factura);

        RegistrarCobroEfectivoRequest req = new RegistrarCobroEfectivoRequest(
                "ped-600",
                8500.0,
                "Cobrado en mano por chofer"
        );

        service.registrarCobroEfectivoEnEntrega(req, "distribuidor_1");

        verify(pagoService).guardarPago(any(Pago.class));
        verify(facturaService).emitirFacturaConPagoAprobado(pedido);
        verify(pedidoService).marcarEntregado(eq("ped-600"), anyString());
    }
}
