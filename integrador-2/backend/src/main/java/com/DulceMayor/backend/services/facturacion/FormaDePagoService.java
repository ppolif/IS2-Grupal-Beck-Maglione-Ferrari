package com.DulceMayor.backend.services.facturacion;

import com.DulceMayor.backend.config.ValidacionException;
import com.DulceMayor.backend.dtos.facturacion.FormaDePagoMapper;
import com.DulceMayor.backend.dtos.facturacion.FormaDePagoRequest;
import com.DulceMayor.backend.dtos.facturacion.FormaDePagoResponse;
import com.DulceMayor.backend.entities.facturacion.FormaDePago;
import com.DulceMayor.backend.repositories.facturacion.FormaDePagoRepository;
import com.DulceMayor.backend.services.BaseService;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Servicio de negocio para la administración del catálogo de formas de pago.
 * Aplica el patrón Template Method heredando de {@link BaseService}.
 */
@Service
public class FormaDePagoService extends BaseService<FormaDePago, FormaDePagoRequest, FormaDePagoResponse> {

    private static final int LONGITUD_MAXIMA_OBSERVACION = 255;

    private final FormaDePagoRepository formaDePagoRepository;

    public FormaDePagoService(
            FormaDePagoRepository formaDePagoRepository,
            FormaDePagoMapper formaDePagoMapper) {
        super(formaDePagoRepository, formaDePagoMapper, "Forma de pago");
        this.formaDePagoRepository = formaDePagoRepository;
    }

    @Override
    protected FormaDePagoRequest prepararRequest(FormaDePagoRequest request) {
        if (request == null) {
            return null;
        }
        String obsLimpia = (request.observacion() != null && !request.observacion().isBlank())
                ? request.observacion().trim()
                : null;
        return new FormaDePagoRequest(request.tipoPago(), obsLimpia);
    }

    @Override
    protected void validarCrear(FormaDePagoRequest request) {
        validarFormato(request);
    }

    @Override
    protected void validarModificar(String id, FormaDePagoRequest request, FormaDePago existente) {
        validarFormato(request);
    }

    @Override
    protected List<FormaDePago> buscarEntidadesActivas() {
        return formaDePagoRepository.findAllByEliminadoFalseOrderByTipoPagoAsc();
    }

    private void validarFormato(FormaDePagoRequest request) {
        if (request == null) {
            throw new ValidacionException("Los datos de la forma de pago son obligatorios");
        }
        if (request.tipoPago() == null) {
            throw new ValidacionException("El tipo de pago es obligatorio");
        }
        if (request.observacion() != null && request.observacion().length() > LONGITUD_MAXIMA_OBSERVACION) {
            throw new ValidacionException("La observación no puede superar los " + LONGITUD_MAXIMA_OBSERVACION + " caracteres");
        }
    }
}

