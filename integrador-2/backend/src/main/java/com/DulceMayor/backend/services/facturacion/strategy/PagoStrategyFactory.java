package com.DulceMayor.backend.services.facturacion.strategy;

import com.DulceMayor.backend.config.ValidacionException;
import com.DulceMayor.backend.entities.facturacion.TipoPago;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Fábrica creacional que resuelve la estrategia de pago correspondiente según el tipo seleccionado.
 *
 * <p><b>Patrón GoF aplicado: Factory Method (Creacional)</b></p>
 */
@Component
public class PagoStrategyFactory {

    private final Map<TipoPago, PagoStrategy> estrategias = new EnumMap<>(TipoPago.class);

    public PagoStrategyFactory(List<PagoStrategy> listaEstrategias) {
        for (PagoStrategy estrategia : listaEstrategias) {
            estrategias.put(estrategia.getTipoPago(), estrategia);
        }
    }

    /**
     * Resuelve y retorna la estrategia de pago para el {@link TipoPago} especificado.
     *
     * @param tipo Identificador del medio de pago.
     * @return Implementación concreta de {@link PagoStrategy}.
     * @throws ValidacionException si no existe una estrategia registrada para el medio solicitado.
     */
    public PagoStrategy obtenerEstrategia(TipoPago tipo) {
        if (tipo == null) {
            throw new ValidacionException("El medio de pago es obligatorio");
        }
        PagoStrategy estrategia = estrategias.get(tipo);
        if (estrategia == null) {
            throw new ValidacionException("Medio de pago no soportado por el motor de cobros: " + tipo);
        }
        return estrategia;
    }
}
