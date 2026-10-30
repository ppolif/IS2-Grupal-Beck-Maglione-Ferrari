package com.DulceMayor.backend.controllers.pedido;

import com.DulceMayor.backend.controllers.BaseController;
import com.DulceMayor.backend.dtos.pedido.AnularPedidoRequest;
import com.DulceMayor.backend.dtos.pedido.HistorialEstadoPedidoResponse;
import com.DulceMayor.backend.dtos.pedido.PedidoMiCuentaPage;
import com.DulceMayor.backend.dtos.pedido.PedidoPage;
import com.DulceMayor.backend.dtos.pedido.PedidoRequest;
import com.DulceMayor.backend.dtos.pedido.PedidoResponse;
import com.DulceMayor.backend.dtos.pedido.PedidoUpdateRequest;
import com.DulceMayor.backend.dtos.pedido.ProcesamientoPedidoResponse;
import com.DulceMayor.backend.services.pedido.PedidoService;
import com.DulceMayor.backend.services.pedido.ProcesamientoPedidoService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para la gestión integral de pedidos, su trazabilidad de estados,
 * su procesamiento/facturación y las consultas del portal del minorista según {@code 04-openapi.yaml}.
 *
 * <p><b>Patrón GoF aplicado: Template Method (Comportamiento)</b></p>
 * <ul>
 *   <li><b>Participante GoF:</b> ConcreteClass que extiende {@link BaseController}.</li>
 *   <li><b>Implementación:</b> Reutiliza {@link #ejecutarCrear(Object)} (con generación automática
 *       del encabezado {@code Location}) y delega las operaciones especializadas de negocio a
 *       {@link PedidoService} y {@link ProcesamientoPedidoService}.</li>
 * </ul>
 */
@RestController
public class PedidoController extends BaseController<PedidoService, PedidoRequest, PedidoResponse> {

    private final ProcesamientoPedidoService procesamientoPedidoService;

    public PedidoController(
            PedidoService pedidoService,
            ProcesamientoPedidoService procesamientoPedidoService) {
        super(pedidoService);
        this.procesamientoPedidoService = procesamientoPedidoService;
    }

    @GetMapping({"/pedidos", "/api/v1/pedidos"})
    @PreAuthorize("hasAnyRole('JEFE', 'ADMINISTRATIVO', 'PREVENTISTA')")
    public ResponseEntity<PedidoPage> listarPedido(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String preventorId,
            @RequestParam(required = false) String distribuidorId,
            @RequestParam(required = false) String puntoDeVentaId,
            @RequestParam(required = false) String minoristaId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            Pageable pageable) {
        return ResponseEntity.ok(service.listarPaginado(
                estado,
                preventorId,
                distribuidorId,
                puntoDeVentaId,
                minoristaId,
                desde,
                hasta,
                pageable
        ));
    }

    @PostMapping({"/pedidos", "/api/v1/pedidos"})
    @PreAuthorize("hasAnyRole('JEFE', 'PREVENTISTA')")
    public ResponseEntity<PedidoResponse> crearPedido(@RequestBody PedidoRequest request) {
        return ejecutarCrear(request);
    }

    @GetMapping({"/pedidos/{id}", "/api/v1/pedidos/{id}"})
    @PreAuthorize("hasAnyRole('JEFE', 'ADMINISTRATIVO', 'PREVENTISTA')")
    public ResponseEntity<PedidoResponse> buscarPedido(@PathVariable String id) {
        return ResponseEntity.ok(service.buscarPedido(id));
    }

    @PutMapping({"/pedidos/{id}", "/api/v1/pedidos/{id}"})
    @PreAuthorize("hasAnyRole('JEFE', 'PREVENTISTA')")
    public ResponseEntity<PedidoResponse> modificarPedido(
            @PathVariable String id,
            @RequestBody PedidoUpdateRequest request) {
        return ResponseEntity.ok(service.modificarPedido(id, request));
    }

    @PostMapping({"/pedidos/{id}/anular", "/api/v1/pedidos/{id}/anular"})
    @PreAuthorize("hasAnyRole('JEFE', 'ADMINISTRATIVO')")
    public ResponseEntity<PedidoResponse> anularPedido(
            @PathVariable String id,
            @RequestBody(required = false) AnularPedidoRequest request) {
        return ResponseEntity.ok(service.anularPedido(id, request));
    }

    @GetMapping({"/pedidos/{id}/historial", "/api/v1/pedidos/{id}/historial"})
    @PreAuthorize("hasAnyRole('JEFE', 'ADMINISTRATIVO', 'PREVENTISTA', 'MINORISTA')")
    public ResponseEntity<List<HistorialEstadoPedidoResponse>> listarHistorialPorPedido(@PathVariable String id) {
        return ResponseEntity.ok(service.listarHistorialPorPedido(id));
    }

    @PostMapping({"/pedidos/{id}/procesar", "/api/v1/pedidos/{id}/procesar"})
    @PreAuthorize("hasAnyRole('JEFE', 'ADMINISTRATIVO')")
    public ResponseEntity<ProcesamientoPedidoResponse> procesarPedido(@PathVariable String id) {
        return ResponseEntity.ok(procesamientoPedidoService.procesarPedido(id));
    }

    @GetMapping({"/mi-cuenta/pedidos", "/api/v1/mi-cuenta/pedidos"})
    @PreAuthorize("hasRole('MINORISTA')")
    public ResponseEntity<PedidoMiCuentaPage> listarPedidoPorCliente(
            @RequestParam(required = false) String estado,
            Pageable pageable) {
        return ResponseEntity.ok(service.listarMiCuenta(estado, pageable));
    }

    @GetMapping({"/mi-cuenta/pedidos/{id}", "/api/v1/mi-cuenta/pedidos/{id}"})
    @PreAuthorize("hasRole('MINORISTA')")
    public ResponseEntity<PedidoResponse> buscarPedidoDeMiCuenta(@PathVariable String id) {
        return ejecutarBuscar(id);
    }
}

