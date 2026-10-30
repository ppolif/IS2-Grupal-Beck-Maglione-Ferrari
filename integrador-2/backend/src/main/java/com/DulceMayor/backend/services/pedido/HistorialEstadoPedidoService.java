package com.DulceMayor.backend.services.pedido;

import com.DulceMayor.backend.dtos.pedido.HistorialEstadoPedidoMapper;
import com.DulceMayor.backend.dtos.pedido.HistorialEstadoPedidoResponse;
import com.DulceMayor.backend.entities.pedido.EstadoPedido;
import com.DulceMayor.backend.entities.pedido.HistorialEstadoPedido;
import com.DulceMayor.backend.entities.pedido.Pedido;
import com.DulceMayor.backend.repositories.pedido.HistorialEstadoPedidoRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de negocio responsable de registrar y consultar la trazabilidad inmutable
 * de estados ({@link HistorialEstadoPedido}) de cada {@link Pedido} según {@code 01-dominio.md} (§3.5)
 * y {@code 02-reglas-negocio.md} (§1.2).
 */
@Service
public class HistorialEstadoPedidoService {

    private final HistorialEstadoPedidoRepository historialEstadoPedidoRepository;
    private final HistorialEstadoPedidoMapper historialEstadoPedidoMapper;

    public HistorialEstadoPedidoService(
            HistorialEstadoPedidoRepository historialEstadoPedidoRepository,
            HistorialEstadoPedidoMapper historialEstadoPedidoMapper) {
        this.historialEstadoPedidoRepository = historialEstadoPedidoRepository;
        this.historialEstadoPedidoMapper = historialEstadoPedidoMapper;
    }

    /**
     * Registra un nuevo cambio de estado para el pedido indicado.
     */
    @Transactional
    public HistorialEstadoPedidoResponse registrarEstado(
            Pedido pedido,
            EstadoPedido estado,
            String usuario,
            String observacion) {
        HistorialEstadoPedido registro = HistorialEstadoPedido.builder()
                .pedido(pedido)
                .estado(estado)
                .fechaHora(LocalDateTime.now())
                .usuario(usuario != null && !usuario.isBlank() ? usuario.trim() : "sistema")
                .observacion(observacion != null && !observacion.isBlank() ? observacion.trim() : null)
                .eliminado(false)
                .build();

        HistorialEstadoPedido guardado = historialEstadoPedidoRepository.save(registro);
        return historialEstadoPedidoMapper.aResponse(guardado);
    }

    /**
     * Devuelve el historial completo de estados de un pedido ordenado cronológicamente.
     */
    @Transactional(readOnly = true)
    public List<HistorialEstadoPedidoResponse> listarHistorialPorPedido(String pedidoId) {
        List<HistorialEstadoPedido> registros = historialEstadoPedidoRepository
                .findAllByPedidoIdAndEliminadoFalseOrderByFechaHoraAscIdAsc(pedidoId);
        return historialEstadoPedidoMapper.aResponseList(registros);
    }
}

