package com.DulceMayor.backend.controllers.facturacion;

import com.DulceMayor.backend.config.UsuarioPrincipal;
import com.DulceMayor.backend.dtos.facturacion.AprobarTransferenciaRequest;
import com.DulceMayor.backend.dtos.facturacion.DatosBancariosEmpresaResponse;
import com.DulceMayor.backend.dtos.facturacion.InformarTransferenciaRequest;
import com.DulceMayor.backend.dtos.facturacion.IniciarPagoQrResponse;
import com.DulceMayor.backend.dtos.facturacion.MercadoPagoWebhookPayload;
import com.DulceMayor.backend.dtos.facturacion.PagoDetalleResponse;
import com.DulceMayor.backend.dtos.facturacion.RechazarPagoRequest;
import com.DulceMayor.backend.dtos.facturacion.RegistrarCobroEfectivoRequest;
import com.DulceMayor.backend.services.facturacion.ProcesamientoPagoService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para operaciones de pasarela de pagos: QR dinámico de Mercado Pago,
 * Webhooks de notificación, transferencias bancarias y cobros en efectivo en entrega.
 */
@RestController
public class PagoOperacionController {

    private final ProcesamientoPagoService procesamientoPagoService;

    public PagoOperacionController(ProcesamientoPagoService procesamientoPagoService) {
        this.procesamientoPagoService = procesamientoPagoService;
    }

    @PostMapping({"/pedidos/{id}/pagos/qr/iniciar", "/api/v1/pedidos/{id}/pagos/qr/iniciar"})
    @PreAuthorize("hasAnyRole('JEFE', 'ADMINISTRATIVO', 'MINORISTA', 'PREVENTISTA')")
    public ResponseEntity<IniciarPagoQrResponse> iniciarPagoQr(@PathVariable String id) {
        IniciarPagoQrResponse res = procesamientoPagoService.iniciarPagoQr(id);
        return ResponseEntity.ok(res);
    }

    @GetMapping({"/pedidos/{id}/pagos/qr/estado", "/api/v1/pedidos/{id}/pagos/qr/estado"})
    @PreAuthorize("hasAnyRole('JEFE', 'ADMINISTRATIVO', 'MINORISTA', 'PREVENTISTA')")
    public ResponseEntity<IniciarPagoQrResponse> consultarEstadoQr(@PathVariable String id) {
        IniciarPagoQrResponse res = procesamientoPagoService.consultarEstadoQr(id);
        return ResponseEntity.ok(res);
    }

    @PostMapping({"/pagos/webhook/mercadopago", "/api/v1/pagos/webhook/mercadopago"})
    public ResponseEntity<Void> recibirWebhookMercadoPago(
            @RequestHeader(value = "x-signature", required = false) String xSignature,
            @RequestHeader(value = "x-request-id", required = false) String xRequestId,
            @RequestBody(required = false) MercadoPagoWebhookPayload payload) {

        procesamientoPagoService.procesarWebhookMercadoPago(xSignature, xRequestId, payload);
        return ResponseEntity.ok().build();
    }

    @PostMapping({"/pedidos/{id}/pagos/transferencia/informar", "/api/v1/pedidos/{id}/pagos/transferencia/informar"})
    @PreAuthorize("hasAnyRole('JEFE', 'ADMINISTRATIVO', 'MINORISTA', 'PREVENTISTA')")
    public ResponseEntity<Void> informarTransferencia(
            @PathVariable String id,
            @Valid @RequestBody InformarTransferenciaRequest request) {
        String usuarioActual = obtenerNombreUsuarioActual();
        procesamientoPagoService.informarTransferencia(id, request, usuarioActual);
        return ResponseEntity.ok().build();
    }

    @GetMapping({"/pagos/datos-bancarios", "/api/v1/pagos/datos-bancarios"})
    public ResponseEntity<DatosBancariosEmpresaResponse> obtenerDatosBancarios() {
        return ResponseEntity.ok(procesamientoPagoService.obtenerDatosBancariosEmpresa());
    }

    @GetMapping({"/pagos/transferencias-pendientes", "/api/v1/pagos/transferencias-pendientes"})
    @PreAuthorize("hasAnyRole('JEFE', 'ADMINISTRATIVO')")
    public ResponseEntity<List<PagoDetalleResponse>> listarTransferenciasPendientes() {
        return ResponseEntity.ok(procesamientoPagoService.listarTransferenciasPendientes());
    }

    @PostMapping({"/pagos/{id}/aprobar", "/api/v1/pagos/{id}/aprobar"})
    @PreAuthorize("hasAnyRole('JEFE', 'ADMINISTRATIVO')")
    public ResponseEntity<Void> aprobarTransferencia(
            @PathVariable String id,
            @Valid @RequestBody(required = false) AprobarTransferenciaRequest request) {
        String usuarioActual = obtenerNombreUsuarioActual();
        procesamientoPagoService.aprobarTransferencia(id, request, usuarioActual);
        return ResponseEntity.ok().build();
    }

    @PostMapping({"/pagos/{id}/rechazar", "/api/v1/pagos/{id}/rechazar"})
    @PreAuthorize("hasAnyRole('JEFE', 'ADMINISTRATIVO')")
    public ResponseEntity<Void> rechazarPago(
            @PathVariable String id,
            @Valid @RequestBody RechazarPagoRequest request) {
        String usuarioActual = obtenerNombreUsuarioActual();
        procesamientoPagoService.rechazarPago(id, request, usuarioActual);
        return ResponseEntity.ok().build();
    }

    @PostMapping({"/pagos/efectivo/registrar-cobro", "/api/v1/pagos/efectivo/registrar-cobro"})
    @PreAuthorize("hasAnyRole('JEFE', 'ADMINISTRATIVO', 'DISTRIBUIDOR')")
    public ResponseEntity<Void> registrarCobroEfectivo(
            @Valid @RequestBody RegistrarCobroEfectivoRequest request) {
        String usuarioActual = obtenerNombreUsuarioActual();
        procesamientoPagoService.registrarCobroEfectivoEnEntrega(request, usuarioActual);
        return ResponseEntity.ok().build();
    }

    private String obtenerNombreUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UsuarioPrincipal up) {
            return up.getUsername();
        }
        return "sistema";
    }
}
