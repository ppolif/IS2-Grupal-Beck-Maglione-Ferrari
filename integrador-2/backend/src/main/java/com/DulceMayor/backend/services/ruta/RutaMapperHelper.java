package com.DulceMayor.backend.services.ruta;

import com.DulceMayor.backend.dtos.persona.EmpleadoMapper;
import com.DulceMayor.backend.dtos.persona.PuntoDeVentaMapper;
import com.DulceMayor.backend.dtos.persona.PuntoDeVentaResponse;
import com.DulceMayor.backend.dtos.ruta.ParadaResponse;
import com.DulceMayor.backend.dtos.ruta.RutaResponse;
import com.DulceMayor.backend.entities.pedido.EstadoPedido;
import com.DulceMayor.backend.entities.pedido.Pedido;
import com.DulceMayor.backend.entities.persona.PuntoDeVenta;
import com.DulceMayor.backend.entities.ruta.Ruta;
import com.DulceMayor.backend.entities.ruta.RutaDistribuidor;
import com.DulceMayor.backend.entities.ruta.RutaPreventor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Componente ensamblador / proyector encargado de transformar entidades polimórficas de {@link Ruta}
 * (sea {@link RutaPreventor} o {@link RutaDistribuidor}) en el DTO consolidado {@link RutaResponse}
 * con sus respectivas listas de {@link ParadaResponse}.
 *
 * <p><b>Estrategia de Proyección en Memoria:</b></p>
 * Construye la vista de paradas calculadas requerida por el frontend para renderizar marcadores
 * y trayectorias sobre el mapa interactivo de <b>OpenStreetMap</b>, sin necesidad de persistir
 * una entidad intermedia "Parada" en la base de datos.
 */
@Component
@RequiredArgsConstructor
public class RutaMapperHelper {

    private final EmpleadoMapper empleadoMapper;
    private final PuntoDeVentaMapper puntoDeVentaMapper;

    /**
     * Mapea una entidad {@link Ruta} a su correspondiente {@link RutaResponse}.
     * Determina las paradas y su estado de completitud según el tipo concreto de ruta.
     *
     * @param ruta Entidad a transformar.
     * @return DTO {@link RutaResponse} completo.
     */
    public RutaResponse aResponse(Ruta ruta) {
        if (ruta == null) {
            return null;
        }

        PuntoDeVentaResponse paradaActualRes = ruta.getParadaActual() != null
                ? puntoDeVentaMapper.aResponse(ruta.getParadaActual())
                : null;

        List<ParadaResponse> paradas;
        if (ruta instanceof RutaPreventor preventor) {
            paradas = mapearParadasPreventor(preventor);
        } else if (ruta instanceof RutaDistribuidor distribuidor) {
            paradas = mapearParadasDistribuidor(distribuidor);
        } else {
            paradas = Collections.emptyList();
        }

        return new RutaResponse(
                ruta.getId(),
                ruta.getTipo(),
                ruta.getFecha(),
                ruta.getEstado(),
                paradaActualRes,
                empleadoMapper.aResumen(ruta.getResponsable()),
                paradas
        );
    }

    /**
     * Proyecta las paradas de un preventor evaluando si el ID del punto de venta se encuentra
     * presente en la lista cronológica de {@code visitadosIds}.
     */
    private List<ParadaResponse> mapearParadasPreventor(RutaPreventor ruta) {
        List<ParadaResponse> resultado = new ArrayList<>();
        List<PuntoDeVenta> puntos = ruta.getPuntosDeVenta();
        List<String> visitados = ruta.getVisitadosIds() != null ? ruta.getVisitadosIds() : Collections.emptyList();

        for (int i = 0; i < puntos.size(); i++) {
            PuntoDeVenta pdv = puntos.get(i);
            boolean completada = visitados.contains(pdv.getId());
            resultado.add(new ParadaResponse(
                    i + 1,
                    completada,
                    puntoDeVentaMapper.aResponse(pdv),
                    null
            ));
        }
        return resultado;
    }

    /**
     * Proyecta las paradas de un distribuidor evaluando el estado del pedido asociado a cada parada
     * (si el pedido ya se encuentra en {@link EstadoPedido#ENTREGADO}, la parada figura como completada).
     */
    private List<ParadaResponse> mapearParadasDistribuidor(RutaDistribuidor ruta) {
        List<ParadaResponse> resultado = new ArrayList<>();
        List<Pedido> pedidos = ruta.getPedidos();

        for (int i = 0; i < pedidos.size(); i++) {
            Pedido pedido = pedidos.get(i);
            boolean completada = pedido.getEstado() == EstadoPedido.ENTREGADO;
            resultado.add(new ParadaResponse(
                    i + 1,
                    completada,
                    puntoDeVentaMapper.aResponse(pedido.getPuntoDeVenta()),
                    pedido.getId()
            ));
        }
        return resultado;
    }
}

