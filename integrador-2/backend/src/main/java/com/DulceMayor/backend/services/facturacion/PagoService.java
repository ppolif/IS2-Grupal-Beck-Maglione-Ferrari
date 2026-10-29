package com.DulceMayor.backend.services.facturacion;

import com.DulceMayor.backend.config.ConflictoException;
import com.DulceMayor.backend.config.ValidacionException;
import com.DulceMayor.backend.dtos.facturacion.PagoMapper;
import com.DulceMayor.backend.dtos.facturacion.PagoRequest;
import com.DulceMayor.backend.dtos.facturacion.PagoResponse;
import com.DulceMayor.backend.entities.facturacion.EstadoFactura;
import com.DulceMayor.backend.entities.facturacion.Factura;
import com.DulceMayor.backend.entities.facturacion.FormaDePago;
import com.DulceMayor.backend.entities.facturacion.Pago;
import com.DulceMayor.backend.repositories.facturacion.PagoRepository;
import com.DulceMayor.backend.services.BaseService;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de negocio para la gestión de {@link Pago} sobre una {@link Factura}.
 *
 * <p><b>Patrón GoF aplicado: Template Method (Comportamiento)</b></p>
 * <ul>
 *   <li><b>Participante GoF:</b> ConcreteClass (Subclase Concreta) que extiende {@link BaseService}.</li>
 *   <li><b>Hooks redefinidos:</b> {@link #prepararRequest(PagoRequest)} y {@link #despuesDeEliminar(Pago)}.</li>
 *   <li><b>Reglas de negocio (sección 7.2 de {@code 02-reglas-negocio.md}):</b>
 *     <ul>
 *       <li>Permite pagos parciales sobre una factura en estado {@code PENDIENTE_PAGO}.</li>
 *       <li>Rechaza pagos sobre facturas {@code ANULADA} o {@code PAGADA}.</li>
 *       <li>Rechaza pagos cuyo monto supere el saldo pendiente ({@code 409 CONFLICTO}).</li>
 *       <li>Cuando la suma de pagos iguala el total de la factura, actualiza su estado a {@code PAGADA}.</li>
 *       <li>Al eliminar lógicamente un pago, recalcula el saldo y revierte el estado a {@code PENDIENTE_PAGO} si corresponde.</li>
 *     </ul>
 *   </li>
 * </ul>
 */
@Service
public class PagoService extends BaseService<Pago, PagoRequest, PagoResponse> {

    private final PagoRepository pagoRepository;
    private final FacturaService facturaService;
    private final FacturaLecturaService facturaLecturaService;
    private final FormaDePagoService formaDePagoService;

    public PagoService(
            PagoRepository pagoRepository,
            PagoMapper pagoMapper,
            FacturaService facturaService,
            FacturaLecturaService facturaLecturaService,
            FormaDePagoService formaDePagoService) {
        super(pagoRepository, pagoMapper, "Pago");
        this.pagoRepository = pagoRepository;
        this.facturaService = facturaService;
        this.facturaLecturaService = facturaLecturaService;
        this.formaDePagoService = formaDePagoService;
    }

    /**
     * Registra un pago total o parcial sobre una factura existente y actualiza el estado de la factura.
     */
    @Transactional
    public PagoResponse registrarPago(String facturaId, PagoRequest request) {
        PagoRequest reqPreparado = prepararRequest(request);
        if (reqPreparado == null) {
            throw new ValidacionException("Los datos del pago son obligatorios");
        }
        if (reqPreparado.formaDePagoId() == null || reqPreparado.formaDePagoId().isBlank()) {
            throw new ValidacionException("El id de la forma de pago es obligatorio");
        }
        if (reqPreparado.monto() == null || reqPreparado.monto() <= 0) {
            throw new ValidacionException("El monto del pago debe ser mayor a cero");
        }

        Factura factura = facturaService.obtenerEntidad(facturaId);
        if (factura.getEstado() == EstadoFactura.ANULADA) {
            throw new ConflictoException("CONFLICTO", "No se puede registrar un pago sobre una factura anulada");
        }
        if (factura.getEstado() == EstadoFactura.PAGADA) {
            throw new ConflictoException("CONFLICTO", "La factura ya se encuentra pagada en su totalidad");
        }

        FormaDePago formaDePago = formaDePagoService.obtenerEntidad(reqPreparado.formaDePagoId());

        double totalPagadoActual = facturaLecturaService.obtenerTotalPagado(facturaId);
        double saldoPendiente = Math.max(0.0, factura.getTotal() - totalPagadoActual);

        if (reqPreparado.monto() > saldoPendiente + 0.001) {
            throw new ConflictoException(
                    "CONFLICTO",
                    "El monto del pago (" + reqPreparado.monto() + ") supera el saldo pendiente de la factura (" + saldoPendiente + ")"
            );
        }

        Pago pago = Pago.builder()
                .factura(factura)
                .formaDePago(formaDePago)
                .monto(reqPreparado.monto())
                .fechaPago(LocalDateTime.now())
                .referencia(reqPreparado.referencia())
                .observacion(reqPreparado.observacion())
                .eliminado(false)
                .build();

        Pago guardado = pagoRepository.save(pago);

        double nuevoTotalPagado = totalPagadoActual + guardado.getMonto();
        facturaService.actualizarEstadoSegunPagos(factura, nuevoTotalPagado);

        return mapper.aResponse(guardado);
    }

    /**
     * Lista los pagos activos registrados para una factura específica.
     */
    @Transactional(readOnly = true)
    public List<PagoResponse> listarPorFactura(String facturaId) {
        facturaService.obtenerEntidad(facturaId);
        return mapper.aResponseList(pagoRepository.findAllByFacturaIdAndEliminadoFalseOrderByFechaPagoAsc(facturaId));
    }

    @Override
    protected PagoRequest prepararRequest(PagoRequest request) {
        if (request == null) {
            return null;
        }
        String formaId = request.formaDePagoId() != null ? request.formaDePagoId().trim() : null;
        String ref = (request.referencia() != null && !request.referencia().isBlank())
                ? request.referencia().trim()
                : null;
        String obs = (request.observacion() != null && !request.observacion().isBlank())
                ? request.observacion().trim()
                : null;
        return new PagoRequest(formaId, request.monto(), ref, obs);
    }

    @Override
    protected void despuesDeEliminar(Pago entidad) {
        Factura factura = entidad.getFactura();
        if (factura != null && factura.getId() != null) {
            double nuevoTotalPagado = facturaLecturaService.obtenerTotalPagado(factura.getId());
            facturaService.actualizarEstadoSegunPagos(factura, nuevoTotalPagado);
        }
    }
}

