package com.DulceMayor.backend.services.ruta;

import com.DulceMayor.backend.config.AccesoDenegadoException;
import com.DulceMayor.backend.config.ConflictoException;
import com.DulceMayor.backend.config.RecursoNoEncontradoException;
import com.DulceMayor.backend.config.UsuarioPrincipal;
import com.DulceMayor.backend.config.ValidacionException;
import com.DulceMayor.backend.dtos.pedido.PedidoResponse;
import com.DulceMayor.backend.dtos.ruta.AvanceRutaResponse;
import com.DulceMayor.backend.dtos.ruta.AvisoProximaEntregaEvent;
import com.DulceMayor.backend.dtos.ruta.EntregaRequest;
import com.DulceMayor.backend.dtos.ruta.ParadaResponse;
import com.DulceMayor.backend.dtos.ruta.RutaDistribuidorRequest;
import com.DulceMayor.backend.dtos.ruta.RutaResponse;
import com.DulceMayor.backend.entities.RolUsuario;
import com.DulceMayor.backend.entities.contacto.Contacto;
import com.DulceMayor.backend.entities.contacto.ContactoCorreoElectronico;
import com.DulceMayor.backend.entities.direccion.Direccion;
import com.DulceMayor.backend.entities.mayorista.Mayorista;
import com.DulceMayor.backend.entities.pedido.EstadoPedido;
import com.DulceMayor.backend.entities.pedido.Pedido;
import com.DulceMayor.backend.entities.persona.Distribuidor;
import com.DulceMayor.backend.entities.persona.Minorista;
import com.DulceMayor.backend.entities.persona.PuntoDeVenta;
import com.DulceMayor.backend.entities.ruta.RutaDistribuidor;
import com.DulceMayor.backend.entities.ruta.RutaEstado;
import com.DulceMayor.backend.repositories.ruta.RutaDistribuidorRepository;
import com.DulceMayor.backend.services.mayorista.MayoristaService;
import com.DulceMayor.backend.services.pedido.PedidoService;
import com.DulceMayor.backend.services.persona.EmpleadoService;
import com.DulceMayor.backend.services.ruta.strategy.NearestNeighborStrategy;
import com.DulceMayor.backend.services.ruta.strategy.RutaOptimizerStrategy;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de lógica de negocio para la gestión integral de rutas de distribución y entrega de pedidos.
 *
 * <p><b>Origen automático desde la Sede Central:</b></p>
 * Los distribuidores inician el recorrido siempre desde la <b>Sede Central</b> de Dulce Mayor.
 * El servicio busca automáticamente la entidad {@link Mayorista} configurada como Sede Central
 * y utiliza las coordenadas de su dirección física (compatibles con <b>OpenStreetMap</b>) como origen geodésico
 * para ordenar las entregas del día.
 *
 * <p><b>Patrón GoF aplicado: Strategy (Comportamiento)</b></p>
 * Utiliza {@link RutaOptimizerStrategy} (instancia concreta {@link NearestNeighborStrategy}) para determinar
 * la secuencia óptima de entrega según la distancia Haversine a los comercios de destino.
 *
 * <p><b>Patrón GoF aplicado: Observer (Comportamiento - Spring Events)</b></p>
 * Cuando se completa una entrega y se detecta una próxima parada en el itinerario, este servicio emite un evento
 * {@link AvisoProximaEntregaEvent} para que los observadores de notificación despachen el aviso al cliente
 * sin acoplar la transacción logística a la pasarela de mensajería/email.
 */
@Service
@RequiredArgsConstructor
public class RutaDistribuidorService {

    private final RutaDistribuidorRepository rutaDistribuidorRepository;
    private final EmpleadoService empleadoService;
    private final PedidoService pedidoService;
    private final MayoristaService mayoristaService;
    private final RutaOptimizerStrategy rutaOptimizerStrategy;
    private final RutaMapperHelper rutaMapperHelper;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Lista las rutas de distribuidores con filtros opcionales.
     * Si el usuario autenticado tiene rol {@code DISTRIBUIDOR}, solo puede ver sus propias rutas.
     *
     * @param responsableId Filtro opcional por ID de distribuidor.
     * @param fecha         Filtro opcional por fecha.
     * @param estado        Filtro opcional por estado.
     * @return Lista de {@link RutaResponse}.
     */
    @Transactional(readOnly = true)
    public List<RutaResponse> listarRutas(String responsableId, LocalDate fecha, String estado) {
        UsuarioPrincipal usuarioActual = obtenerPrincipalActual();
        String responsableFiltro = responsableId;

        if (usuarioActual != null && usuarioActual.getRol() == RolUsuario.DISTRIBUIDOR) {
            Distribuidor distribuidor = empleadoService.obtenerDistribuidorPorUsuarioId(usuarioActual.getId());
            responsableFiltro = distribuidor.getId();
        }

        RutaEstado rutaEstado = parsearEstado(estado);

        return rutaDistribuidorRepository.buscarConFiltros(responsableFiltro, fecha, rutaEstado).stream()
                .map(rutaMapperHelper::aResponse)
                .toList();
    }

    private RutaEstado parsearEstado(String estado) {
        if (estado == null || estado.isBlank()) {
            return null;
        }
        try {
            return RutaEstado.valueOf(estado.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidacionException("Estado de ruta no válido: " + estado);
        }
    }

    /**
     * Busca la ruta de reparto del distribuidor logueado para una fecha dada (por defecto hoy).
     * Utilizado por la app móvil para renderizar las paradas y el recorrido del día en el mapa.
     *
     * @param fecha Fecha consultada.
     * @return {@link RutaResponse} con pedidos y paradas ordenadas.
     */
    @Transactional(readOnly = true)
    public RutaResponse buscarMiRutaDelDia(LocalDate fecha) {
        LocalDate fechaConsulta = fecha != null ? fecha : LocalDate.now();
        UsuarioPrincipal usuarioActual = obtenerPrincipalActual();
        if (usuarioActual == null) {
            throw new AccesoDenegadoException("Usuario no autenticado");
        }
        Distribuidor distribuidor = empleadoService.obtenerDistribuidorPorUsuarioId(usuarioActual.getId());

        RutaDistribuidor ruta = rutaDistribuidorRepository.findByResponsableIdAndFechaAndEliminadoFalse(distribuidor.getId(), fechaConsulta)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró una ruta de distribución para la fecha " + fechaConsulta));

        return rutaMapperHelper.aResponse(ruta);
    }

    /**
     * Busca una ruta de distribuidor por su identificador único.
     *
     * @param id Identificador de la ruta.
     * @return {@link RutaResponse}.
     */
    @Transactional(readOnly = true)
    public RutaResponse buscarPorId(String id) {
        RutaDistribuidor ruta = rutaDistribuidorRepository.findByIdAndEliminadoFalse(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Ruta de distribuidor no encontrada: " + id));

        UsuarioPrincipal usuarioActual = obtenerPrincipalActual();
        if (usuarioActual != null && usuarioActual.getRol() == RolUsuario.DISTRIBUIDOR) {
            Distribuidor distribuidor = empleadoService.obtenerDistribuidorPorUsuarioId(usuarioActual.getId());
            if (!ruta.getResponsable().getId().equals(distribuidor.getId())) {
                throw new AccesoDenegadoException("No tiene permisos para consultar una ruta de entrega ajena");
            }
        }

        return rutaMapperHelper.aResponse(ruta);
    }

    /**
     * Genera de forma 100% automática las rutas diarias de distribución y entrega a partir de pedidos facturados.
     *
     * <p><b>Algoritmo de clustering, balance de carga y optimización:</b></p>
     * <ol>
     *   <li>Obtiene todos los distribuidores activos que no tengan ya una ruta asignada para esa fecha.</li>
     *   <li>Obtiene todos los pedidos en estado {@link EstadoPedido#FACTURADO}.</li>
     *   <li>Obtiene las coordenadas geodésicas de la Sede Central (OpenStreetMap).</li>
     *   <li>Ordena los pedidos de forma ascendente según su distancia a la Sede Central usando TimSort (\(O(n \log n)\)).</li>
     *   <li>Toma los primeros \(N\) pedidos más cercanos a la central como núcleos/semillas iniciales.</li>
     *   <li>Mezcla aleatoriamente (shuffle) los distribuidores para asignarles una semilla inicial al azar a cada uno.</li>
     *   <li>Calcula la cuota equitativa por distribuidor (\(\lceil \text{totalPedidos} / N \rceil\)).</li>
     *   <li>Para cada pedido restante, lo asigna al distribuidor con cupo disponible cuyo pedido semilla esté más próximo (Haversine).</li>
     *   <li>Para cada distribuidor con pedidos asignados, optimiza el itinerario mediante {@link RutaOptimizerStrategy}
     *       partiendo de la Sede Central, transiciona los pedidos a {@link EstadoPedido#EN_REPARTO} y persiste la ruta.</li>
     * </ol>
     *
     * @param fecha Fecha operativa de entrega (si es null, se toma la fecha actual).
     * @return Lista de {@link RutaResponse} generadas para los distribuidores.
     */
    @Transactional
    public List<RutaResponse> crearRutasAutomaticas(LocalDate fecha) {
        LocalDate fechaOperativa = fecha != null ? fecha : LocalDate.now();

        List<Distribuidor> todosDistribuidores = empleadoService.listarTodosLosDistribuidoresActivos();
        if (todosDistribuidores.isEmpty()) {
            throw new ValidacionException("No hay distribuidores activos registrados en el sistema");
        }

        // Distribuidores disponibles sin ruta previa para esa fecha
        List<Distribuidor> distribuidoresDisponibles = new ArrayList<>();
        for (Distribuidor d : todosDistribuidores) {
            if (!rutaDistribuidorRepository.existsByResponsableIdAndFechaAndEliminadoFalse(d.getId(), fechaOperativa)) {
                distribuidoresDisponibles.add(d);
            }
        }

        if (distribuidoresDisponibles.isEmpty()) {
            throw new ConflictoException("Todos los distribuidores activos ya tienen una ruta asignada para el día " + fechaOperativa);
        }

        List<Pedido> pedidosFacturados = pedidoService.listarFacturadosPendientes();
        if (pedidosFacturados.isEmpty()) {
            throw new ValidacionException("No hay pedidos en estado FACTURADO pendientes de entrega");
        }

        double[] origenCentral = obtenerCoordenadasSedeCentral();

        // Ordenar pedidos por proximidad de su punto de venta a la Sede Central (TimSort O(n log n))
        List<Pedido> pedidosOrdenadosPorCercaniaCentral = new ArrayList<>(pedidosFacturados);
        pedidosOrdenadosPorCercaniaCentral.sort((p1, p2) -> {
            double d1 = calcularDistanciaPdv(origenCentral[0], origenCentral[1], p1.getPuntoDeVenta());
            double d2 = calcularDistanciaPdv(origenCentral[0], origenCentral[1], p2.getPuntoDeVenta());
            return Double.compare(d1, d2);
        });

        int cantidadDistribuidores = Math.min(distribuidoresDisponibles.size(), pedidosFacturados.size());
        List<Distribuidor> distribuidoresAUsar = new ArrayList<>(distribuidoresDisponibles.subList(0, cantidadDistribuidores));

        // Mezclar aleatoriamente para asignar los pedidos semilla al azar entre los distribuidores seleccionados
        java.util.Collections.shuffle(distribuidoresAUsar);

        // Estructuras de asignación: distribuidor -> lista de pedidos
        Map<Distribuidor, List<Pedido>> asignaciones = new LinkedHashMap<>();

        for (int i = 0; i < cantidadDistribuidores; i++) {
            Distribuidor dist = distribuidoresAUsar.get(i);
            Pedido pedidoSemilla = pedidosOrdenadosPorCercaniaCentral.get(i);
            List<Pedido> lista = new ArrayList<>();
            lista.add(pedidoSemilla);
            asignaciones.put(dist, lista);
        }

        // Cupo objetivo por distribuidor (división con redondeo hacia arriba)
        int cuotaPorDistribuidor = (int) Math.ceil((double) pedidosFacturados.size() / cantidadDistribuidores);

        // Asignar el resto de los pedidos encadenando con el último punto de venta asignado a cada distribuidor
        for (int i = cantidadDistribuidores; i < pedidosOrdenadosPorCercaniaCentral.size(); i++) {
            Pedido pedidoPendiente = pedidosOrdenadosPorCercaniaCentral.get(i);
            PuntoDeVenta pdvPendiente = pedidoPendiente.getPuntoDeVenta();

            Distribuidor mejorDistribuidor = null;
            double menorDistanciaAUltimoPdv = Double.MAX_VALUE;

            for (Distribuidor dist : distribuidoresAUsar) {
                List<Pedido> asignados = asignaciones.get(dist);
                if (asignados.size() >= cuotaPorDistribuidor) {
                    continue; // Ya alcanzó su cupo máximo
                }

                // Se compara con el último punto de venta asignado al distribuidor para encadenar el recorrido
                PuntoDeVenta ultimoPdv = asignados.get(asignados.size() - 1).getPuntoDeVenta();
                double distMetros = calcularDistanciaEntrePdvs(ultimoPdv, pdvPendiente);

                if (distMetros < menorDistanciaAUltimoPdv) {
                    menorDistanciaAUltimoPdv = distMetros;
                    mejorDistribuidor = dist;
                }
            }

            // Fallback: si todos alcanzaron la cuota teórica (casos de redondeo), se le da al que tenga menos
            if (mejorDistribuidor == null) {
                mejorDistribuidor = distribuidoresAUsar.stream()
                        .min(java.util.Comparator.comparingInt(d -> asignaciones.get(d).size()))
                        .orElse(distribuidoresAUsar.get(0));
            }

            asignaciones.get(mejorDistribuidor).add(pedidoPendiente);
        }

        // Armar y persistir cada ruta optimizada
        List<RutaResponse> resultado = new ArrayList<>();
        for (Map.Entry<Distribuidor, List<Pedido>> entry : asignaciones.entrySet()) {
            Distribuidor distribuidor = entry.getKey();
            List<Pedido> pedidosDeDistribuidor = entry.getValue();

            Map<String, Pedido> mapaPuntos = new LinkedHashMap<>();
            List<PuntoDeVenta> puntosAVisitar = new ArrayList<>();
            for (Pedido p : pedidosDeDistribuidor) {
                PuntoDeVenta pdv = p.getPuntoDeVenta();
                mapaPuntos.put(pdv.getId(), p);
                puntosAVisitar.add(pdv);
            }

            RutaResponse rutaResp = armarYPersistirRuta(distribuidor, fechaOperativa, puntosAVisitar, mapaPuntos);
            resultado.add(rutaResp);
        }

        return resultado;
    }

    /**
     * Secuencia los puntos de venta utilizando {@link RutaOptimizerStrategy}, transiciona los pedidos a EN_REPARTO
     * y persiste la entidad {@link RutaDistribuidor}.
     */
    private RutaResponse armarYPersistirRuta(
            Distribuidor distribuidor,
            LocalDate fecha,
            List<PuntoDeVenta> puntosAVisitar,
            Map<String, Pedido> mapaPuntosAPedidos) {

        double[] origen = obtenerCoordenadasSedeCentral();

        // Optimización del orden de paradas según Vecino Más Cercano (Haversine)
        List<PuntoDeVenta> puntosOrdenados = rutaOptimizerStrategy.optimizarRecorrido(
                origen[0],
                origen[1],
                puntosAVisitar
        );

        // Reordenar la lista de pedidos conforme a la secuencia óptima de paradas calculada
        List<Pedido> pedidosOrdenados = new ArrayList<>();
        for (PuntoDeVenta pdv : puntosOrdenados) {
            Pedido p = mapaPuntosAPedidos.get(pdv.getId());
            if (p != null) {
                pedidosOrdenados.add(p);
            }
        }

        // Transición de pedidos a EN_REPARTO mediante PedidoService
        for (Pedido pedido : pedidosOrdenados) {
            pedidoService.asignarAReparto(
                    pedido.getId(),
                    distribuidor.getId(),
                    "Asignado a ruta de entrega del distribuidor " + distribuidor.getNombre() + " " + distribuidor.getApellido()
            );
        }

        PuntoDeVenta primerPunto = pedidosOrdenados.isEmpty() ? null : pedidosOrdenados.get(0).getPuntoDeVenta();

        RutaDistribuidor ruta = RutaDistribuidor.builder()
                .fecha(fecha)
                .estado(RutaEstado.PENDIENTE)
                .responsable(distribuidor)
                .paradaActual(primerPunto)
                .pedidos(pedidosOrdenados)
                .build();

        RutaDistribuidor guardada = rutaDistribuidorRepository.save(ruta);
        return rutaMapperHelper.aResponse(guardada);
    }

    private double calcularDistanciaPdv(double latOrigen, double lonOrigen, PuntoDeVenta pdv) {
        if (pdv == null || pdv.getDireccion() == null) {
            return Double.MAX_VALUE;
        }
        Direccion dir = pdv.getDireccion();
        if (dir.getLatitud() == 0.0 && dir.getLongitud() == 0.0) {
            return Double.MAX_VALUE;
        }
        return rutaOptimizerStrategy.calcularDistanciaHaversine(latOrigen, lonOrigen, dir.getLatitud(), dir.getLongitud());
    }

    private double calcularDistanciaEntrePdvs(PuntoDeVenta pdv1, PuntoDeVenta pdv2) {
        if (pdv1 == null || pdv1.getDireccion() == null || pdv2 == null || pdv2.getDireccion() == null) {
            return Double.MAX_VALUE;
        }
        Direccion d1 = pdv1.getDireccion();
        Direccion d2 = pdv2.getDireccion();
        if ((d1.getLatitud() == 0.0 && d1.getLongitud() == 0.0) || (d2.getLatitud() == 0.0 && d2.getLongitud() == 0.0)) {
            return Double.MAX_VALUE;
        }
        return rutaOptimizerStrategy.calcularDistanciaHaversine(d1.getLatitud(), d1.getLongitud(), d2.getLatitud(), d2.getLongitud());
    }

    /**
     * Registra la entrega efectiva de un pedido en su destino según {@code 02-reglas-negocio.md} (§6.3).
     *
     * <p><b>Flujo de ejecución:</b></p>
     * <ol>
     *   <li>Marca el {@link Pedido} como {@link EstadoPedido#ENTREGADO} delegando en {@link PedidoService#marcarEntregado}.</li>
     *   <li>Busca en el itinerario cuál es el <i>siguiente pedido</i> que aún se encuentra en reparto.</li>
     *   <li>Si existe un próximo pedido:
     *     <ul>
     *       <li>Actualiza {@code paradaActual} de la ruta con el punto de venta de dicho pedido.</li>
     *       <li>Dispara de forma asíncrona el evento {@link AvisoProximaEntregaEvent} para notificar al minorista.</li>
     *     </ul>
     *   </li>
     *   <li>Si ya se entregaron todos los pedidos:
     *     <ul>
     *       <li>Fija {@code paradaActual = null}.</li>
     *       <li>Transiciona la ruta automáticamente a {@link RutaEstado#REALIZADA}.</li>
     *     </ul>
     *   </li>
     * </ol>
     *
     * @param id      Identificador de la ruta.
     * @param request Datos de la entrega (pedidoId y observación opcional).
     * @return {@link AvanceRutaResponse} con el estado de la ruta y la próxima parada sugerida.
     */
    @Transactional
    public AvanceRutaResponse registrarEntrega(String id, EntregaRequest request) {
        if (request == null || request.pedidoId() == null || request.pedidoId().isBlank()) {
            throw new ValidacionException("El ID del pedido entregado es obligatorio");
        }

        RutaDistribuidor ruta = rutaDistribuidorRepository.findByIdAndEliminadoFalse(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Ruta de distribuidor no encontrada: " + id));

        if (ruta.getEstado() != RutaEstado.PENDIENTE) {
            throw new ConflictoException("La ruta no se encuentra en estado PENDIENTE");
        }

        /// esto valida que el pedido que el distribuidor dice haber entregado realmente pertenezca a la hoja de ruta que está ejecutando.
        Pedido pedidoEntregado = ruta.getPedidos().stream()
                .filter(p -> p.getId().equals(request.pedidoId()))
                .findFirst()
                .orElseThrow(() -> new RecursoNoEncontradoException("El pedido " + request.pedidoId() + " no forma parte de esta ruta"));

        // Marcar pedido entregado usando PedidoService para asegurar coherencia y trazabilidad
        String obs = request.observacion() != null && !request.observacion().isBlank()
                ? request.observacion()
                : "Entrega completada por el distribuidor";
        PedidoResponse entregaRes = pedidoService.marcarEntregado(pedidoEntregado.getId(), obs);

        // Refrescar el estado en la instancia en memoria
        pedidoEntregado.setEstado(EstadoPedido.ENTREGADO);

        // Determinar el próximo pedido pendiente en el itinerario
        Pedido siguientePedido = null;
        for (Pedido p : ruta.getPedidos()) {
            if (p.getEstado() == EstadoPedido.EN_REPARTO) {
                siguientePedido = p;
                break;
            }
        }

        boolean finalizada = false;
        if (siguientePedido != null) {
            ruta.setParadaActual(siguientePedido.getPuntoDeVenta());

            // Disparar evento de notificación por email para el próximo cliente
            dispararAvisoProximaEntrega(siguientePedido);
        } else {
            ruta.setParadaActual(null);
            ruta.setEstado(RutaEstado.REALIZADA);
            finalizada = true;
        }

        RutaDistribuidor actualizada = rutaDistribuidorRepository.save(ruta);
        RutaResponse rutaRes = rutaMapperHelper.aResponse(actualizada);

        ParadaResponse siguienteParadaRes = null;
        if (siguientePedido != null && rutaRes.paradas() != null) {
            final String sigPedidoId = siguientePedido.getId();
            siguienteParadaRes = rutaRes.paradas().stream()
                    .filter(p -> sigPedidoId.equals(p.pedidoId()))
                    .findFirst()
                    .orElse(null);
        }

        return new AvanceRutaResponse(rutaRes, siguienteParadaRes, finalizada);
    }

    /**
     * Cierra la ruta de entrega del día (acción manual desde la app o el dashboard).
     * Si quedaron pedidos pendientes, la ruta se marca como {@link RutaEstado#INCOMPLETA};
     * si todos fueron entregados, pasa a {@link RutaEstado#REALIZADA}.
     *
     * @param id Identificador de la ruta.
     * @return {@link RutaResponse}.
     */
    @Transactional
    public RutaResponse cerrarRuta(String id) {
        RutaDistribuidor ruta = rutaDistribuidorRepository.findByIdAndEliminadoFalse(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Ruta de distribuidor no encontrada: " + id));

        if (ruta.getEstado() != RutaEstado.PENDIENTE) {
            throw new ConflictoException("La ruta ya se encuentra cerrada con estado " + ruta.getEstado());
        }

        boolean todosEntregados = ruta.getPedidos().stream()
                .allMatch(p -> p.getEstado() == EstadoPedido.ENTREGADO);

        ruta.setEstado(todosEntregados ? RutaEstado.REALIZADA : RutaEstado.INCOMPLETA);
        ruta.setParadaActual(null);

        RutaDistribuidor cerrada = rutaDistribuidorRepository.save(ruta);
        return rutaMapperHelper.aResponse(cerrada);
    }

    /**
     * Emite el evento asíncrono para notificar al minorista que su entrega es la siguiente en el orden.
     */
    private void dispararAvisoProximaEntrega(Pedido proximoPedido) {
        PuntoDeVenta pdv = proximoPedido.getPuntoDeVenta();
        String emailDestino = null;

        if (pdv != null && pdv.getMinorista() != null) {
            Minorista minorista = pdv.getMinorista();

            // 1. Prioridad: el correo de la cuenta de usuario del minorista
            if (minorista.getUsuario() != null && minorista.getUsuario().getNombreUsuario() != null) {
                emailDestino = minorista.getUsuario().getNombreUsuario();
            }

            // 2. Respaldo: si aún no tiene usuario activo, busca en la lista de contactos
            if (emailDestino == null && minorista.getContactos() != null) {
                for (Contacto c : minorista.getContactos()) {
                    if (c instanceof ContactoCorreoElectronico correo && correo.getEmail() != null) {
                        emailDestino = correo.getEmail();
                        break;
                    }
                }
            }
        }

        eventPublisher.publishEvent(new AvisoProximaEntregaEvent(
                proximoPedido.getId(),
                proximoPedido.getNumeroPedido(),
                pdv != null ? pdv.getId() : null,
                pdv != null ? pdv.getNombreComercio() : null,
                emailDestino
        ));
    }

    /**
     * Obtiene automáticamente las coordenadas geodésicas de la Sede Central de Dulce Mayor (OpenStreetMap).
     */
    private double[] obtenerCoordenadasSedeCentral() {
        Mayorista central = mayoristaService.obtenerSedeCentral();
        if (central != null && central.getDirecciones() != null) {
            for (Direccion dir : central.getDirecciones()) {
                if (!dir.isEliminado() && dir.getLatitud() != null && dir.getLongitud() != null
                        && (dir.getLatitud() != 0.0 || dir.getLongitud() != 0.0)) {
                    return new double[]{dir.getLatitud(), dir.getLongitud()};
                }
            }
        }
        // Fallback a Mendoza Centro (predeterminado de Sede Central Dulce Mayor)
        return new double[]{-32.889458, -68.845839};
    }

    private UsuarioPrincipal obtenerPrincipalActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UsuarioPrincipal up) {
            return up;
        }
        return null;
    }
}
