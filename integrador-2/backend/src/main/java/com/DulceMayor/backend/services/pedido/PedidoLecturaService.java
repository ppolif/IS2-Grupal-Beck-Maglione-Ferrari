package com.DulceMayor.backend.services.pedido;

import com.DulceMayor.backend.dtos.facturacion.FacturaResponse;
import com.DulceMayor.backend.dtos.pedido.PedidoMapper;
import com.DulceMayor.backend.dtos.pedido.PedidoMiCuentaPage;
import com.DulceMayor.backend.dtos.pedido.PedidoPage;
import com.DulceMayor.backend.dtos.pedido.PedidoResponse;
import com.DulceMayor.backend.entities.pedido.EstadoPedido;
import com.DulceMayor.backend.entities.pedido.Pedido;
import com.DulceMayor.backend.repositories.pedido.PedidoRepository;
import com.DulceMayor.backend.services.facturacion.FacturaService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de solo lectura para proyectar pedidos resolviendo {@code facturaId} desde {@link FacturaService}
 * y ejecutar consultas paginadas.
 */
@Service
@Transactional(readOnly = true)
public class PedidoLecturaService {

    private final PedidoRepository pedidoRepository;
    private final FacturaService facturaService;
    private final PedidoMapper pedidoMapper;

    public PedidoLecturaService(
            PedidoRepository pedidoRepository,
            FacturaService facturaService,
            PedidoMapper pedidoMapper) {
        this.pedidoRepository = pedidoRepository;
        this.facturaService = facturaService;
        this.pedidoMapper = pedidoMapper;
    }

    /**
     * Proyecta y enriquece una entidad {@link Pedido} en su DTO de salida {@link PedidoResponse},
     * mapeando sus campos base y resolviendo si posee una {@code Factura} activa vinculada mediante {@link FacturaService}.
     *
     * <p><b>Cuándo se usa:</b> Cada vez que un pedido necesita ser presentado o devuelto hacia las capas superiores (controladores/APIs),
     * tanto en consultas puntuales (ej: {@code PedidoService.aResponse}, {@code buscarPedido}) como en el armado de páginas de resultados.</p>
     *
     * @param pedido Entidad del pedido a transformar.
     * @return {@link PedidoResponse} completo con importes, líneas y el identificador de la factura si existe.
     */
    public PedidoResponse enriquecerResponse(Pedido pedido) {
        PedidoResponse base = pedidoMapper.aResponse(pedido);
        String facturaId = pedido.getId() != null
                ? facturaService.buscarPorPedidoId(pedido.getId())
                        .map(FacturaResponse::id)
                        .orElse(null)
                : null;

        return new PedidoResponse(
                base.id(),
                base.numeroPedido(),
                base.fechaHoraCarga(),
                base.estado(),
                base.puntoDeVenta(),
                base.preventor(),
                base.distribuidor(),
                base.codigoPromocion(),
                base.subtotal(),
                base.descuento(),
                base.total(),
                base.detalles() != null ? base.detalles() : List.of(),
                facturaId
        );
    }

    /**
     * Ejecuta una consulta paginada de pedidos en la base de datos aplicando filtros dinámicos opcionales
     * (estado del pedido, preventista asignado, repartidor/distribuidor, punto de venta de destino,
     * minorista titular y rango de fechas de carga). Cada registro obtenido es proyectado y enriquecido con su factura.
     *
     * <p><b>Cuándo se usa:</b> Es invocado por {@code PedidoService.listarPaginado} cuando un administrador o preventista
     * accede al listado general de pedidos desde la interfaz de gestión (endpoint {@code GET /pedidos}).</p>
     *
     * @param estado Estado del pedido por el cual filtrar (opcional).
     * @param preventorId Identificador del preventista asignado (opcional).
     * @param distribuidorId Identificador del distribuidor a cargo del reparto (opcional).
     * @param puntoDeVentaId Identificador del punto de venta destinatario (opcional).
     * @param minoristaId Identificador del cliente minorista (opcional).
     * @param desde Fecha inicial del período de carga (opcional).
     * @param hasta Fecha final del período de carga (opcional).
     * @param pageable Configuración de paginado y ordenamiento (número de página, tamaño, dirección).
     * @return {@link PedidoPage} con la lista de pedidos enriquecidos y metadatos de paginación.
     */
    public PedidoPage listarPaginado(
            EstadoPedido estado,
            String preventorId,
            String distribuidorId,
            String puntoDeVentaId,
            String minoristaId,
            LocalDate desde,
            LocalDate hasta,
            Pageable pageable) {

        LocalDateTime fechaDesde = desde != null ? desde.atStartOfDay() : null;
        LocalDateTime fechaHasta = hasta != null ? hasta.atTime(LocalTime.MAX) : null;

        Page<Pedido> pagina = pedidoRepository.buscarConFiltros(
                estado,
                limpiarFiltro(preventorId),
                limpiarFiltro(distribuidorId),
                limpiarFiltro(puntoDeVentaId),
                limpiarFiltro(minoristaId),
                fechaDesde,
                fechaHasta,
                pageable
        );

        List<PedidoResponse> contenido = new ArrayList<>();
        for (Pedido p : pagina.getContent()) {
            contenido.add(enriquecerResponse(p));
        }

        return new PedidoPage(
                contenido,
                pagina.getNumber(),
                pagina.getSize(),
                pagina.getTotalElements(),
                pagina.getTotalPages()
        );
    }

    /**
     * Consulta y pagina de forma segura los pedidos pertenecientes exclusivamente al cliente minorista provisto,
     * permitiendo filtrar opcionalmente por estado.
     *
     * <p><b>Cuándo se usa:</b> Es invocado por {@code PedidoService.listarMiCuenta} cuando un cliente autenticado con rol
     * {@code MINORISTA} ingresa a la sección "Mi Cuenta" para seguir el estado de sus pedidos (endpoint {@code GET /pedidos/mi-cuenta}).</p>
     *
     * @param estado Estado del pedido a filtrar (opcional).
     * @param minoristaId Identificador único del minorista dueño de los pedidos.
     * @param pageable Configuración de paginación solicitada por el cliente.
     * @return {@link PedidoMiCuentaPage} con los pedidos enriquecidos y metadatos de paginación.
     */
    public PedidoMiCuentaPage listarMiCuenta(EstadoPedido estado, String minoristaId, Pageable pageable) {
        Page<Pedido> pagina = pedidoRepository.buscarConFiltros(
                estado,
                null,
                null,
                null,
                limpiarFiltro(minoristaId),
                null,
                null,
                pageable
        );

        List<PedidoResponse> contenido = new ArrayList<>();
        for (Pedido p : pagina.getContent()) {
            contenido.add(enriquecerResponse(p));
        }

        return new PedidoMiCuentaPage(
                contenido,
                pagina.getNumber(),
                pagina.getSize(),
                pagina.getTotalElements(),
                pagina.getTotalPages()
        );
    }

    /**
     * Normaliza un parámetro de búsqueda textual, recortando espacios en blanco innecesarios
     * y convirtiendo cadenas vacías o compuestas únicamente por espacios en {@code null}.
     *
     * <p><b>Cuándo se usa:</b> Es un método auxiliar privado utilizado internamente en {@link #listarPaginado}
     * y {@link #listarMiCuenta} antes de enviar los argumentos a la consulta JPQL en {@link PedidoRepository},
     * evitando que filtros en blanco interfieran en las condiciones SQL.</p>
     *
     * @param valor Cadena de texto recibida desde la capa de entrada.
     * @return Cadena normalizada sin espacios en los extremos, o {@code null} si estaba vacía o en blanco.
     */
    private String limpiarFiltro(String valor) {
        return (valor != null && !valor.isBlank()) ? valor.trim() : null;
    }
}
