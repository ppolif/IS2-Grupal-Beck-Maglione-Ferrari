package com.DulceMayor.backend.services.ruta;

import com.DulceMayor.backend.config.AccesoDenegadoException;
import com.DulceMayor.backend.config.ConflictoException;
import com.DulceMayor.backend.config.RecursoNoEncontradoException;
import com.DulceMayor.backend.config.UsuarioPrincipal;
import com.DulceMayor.backend.config.ValidacionException;
import com.DulceMayor.backend.dtos.facturacion.RegistrarCobroEfectivoRequest;
import com.DulceMayor.backend.dtos.pedido.PedidoResponse;
import com.DulceMayor.backend.dtos.ruta.AvanceRutaResponse;
import com.DulceMayor.backend.dtos.ruta.AvisoProximaEntregaEvent;
import com.DulceMayor.backend.dtos.ruta.EntregaRequest;
import com.DulceMayor.backend.dtos.ruta.ParadaResponse;
import com.DulceMayor.backend.dtos.ruta.RutaResponse;
import com.DulceMayor.backend.entities.RolUsuario;
import com.DulceMayor.backend.entities.direccion.Direccion;
import com.DulceMayor.backend.entities.facturacion.TipoPago;
import com.DulceMayor.backend.entities.mayorista.Mayorista;
import com.DulceMayor.backend.entities.pedido.EstadoPedido;
import com.DulceMayor.backend.entities.pedido.Pedido;
import com.DulceMayor.backend.entities.persona.Distribuidor;
import com.DulceMayor.backend.entities.persona.PuntoDeVenta;
import com.DulceMayor.backend.entities.ruta.RutaDistribuidor;
import com.DulceMayor.backend.entities.ruta.RutaEstado;
import com.DulceMayor.backend.repositories.ruta.RutaDistribuidorRepository;
import com.DulceMayor.backend.services.direccion.DireccionService;
import com.DulceMayor.backend.services.facturacion.ProcesamientoPagoService;
import com.DulceMayor.backend.services.mail.EmailService;
import com.DulceMayor.backend.services.mayorista.MayoristaService;
import com.DulceMayor.backend.services.pedido.PedidoService;
import com.DulceMayor.backend.services.persona.EmpleadoService;
import com.DulceMayor.backend.services.ruta.strategy.OsrmRoutingStrategy;
import com.DulceMayor.backend.services.ruta.strategy.RutaOptimizerStrategy;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de lógica de negocio para la gestión integral de rutas de distribución y entrega de pedidos.
 *
 * <p><b>Delegación total a OpenStreetMap y OSRM:</b></p>
 * <ul>
 *   <li>Obtiene las coordenadas de la <b>Sede Central</b> de Dulce Mayor.</li>
 *   <li>Utiliza el servicio {@code /table/v1/driving/} de OSRM (vía {@link RutaOptimizerStrategy}) para obtener
 *       la matriz de distancias viales entre la Sede Central y todos los comercios con pedidos facturados.</li>
 *   <li>Utiliza el servicio {@code /trip/v1/driving/} de OSRM ({@link OsrmRoutingStrategy}) para resolver el
 *       Problema del Viajante (TSP) y determinar la secuencia óptima de entrega de cada distribuidor.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class RutaDistribuidorService {

    private final RutaDistribuidorRepository rutaDistribuidorRepository;
    private final EmpleadoService empleadoService;
    private final PedidoService pedidoService;
    private final MayoristaService mayoristaService;
    private final DireccionService direccionService;
    private final RutaOptimizerStrategy rutaOptimizerStrategy;
    private final RutaMapperHelper rutaMapperHelper;
    private final EmailService emailService;
    private final ProcesamientoPagoService procesamientoPagoService;
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
     * Si no existe una ruta pero hay pedidos facturados pendientes, auto-genera las rutas del día.
     * Utilizado por la app móvil para renderizar las paradas y el recorrido del día en el mapa.
     *
     * @param fecha Fecha consultada.
     * @return {@link RutaResponse} con pedidos y paradas ordenadas.
     */
    @Transactional
    public RutaResponse buscarMiRutaDelDia(LocalDate fecha) {
        LocalDate fechaConsulta = fecha != null ? fecha : LocalDate.now();
        UsuarioPrincipal usuarioActual = obtenerPrincipalActual();
        if (usuarioActual == null) {
            throw new AccesoDenegadoException("Usuario no autenticado");
        }
        Distribuidor distribuidor = empleadoService.obtenerDistribuidorPorUsuarioId(usuarioActual.getId());

        Optional<RutaDistribuidor> rutaOpt = rutaDistribuidorRepository.findByResponsableIdAndFechaAndEliminadoFalse(distribuidor.getId(), fechaConsulta);
        if (rutaOpt.isPresent()) {
            RutaDistribuidor rutaActual = incorporarPedidosPendientesSiExisten(rutaOpt.get(), distribuidor);
            return rutaMapperHelper.aResponse(rutaActual);
        }

        List<Pedido> pedidosFacturados = pedidoService.listarFacturadosPendientes();
        if (!pedidosFacturados.isEmpty()) {
            List<RutaResponse> creadas = crearRutasAutomaticas(fechaConsulta);
            for (RutaResponse r : creadas) {
                if (r.responsable() != null && distribuidor.getId().equals(r.responsable().id())) {
                    return r;
                }
            }
        }

        throw new RecursoNoEncontradoException("No hay pedidos facturados pendientes ni ruta asignada para la fecha " + fechaConsulta);
    }

    /**
     * Genera la ruta de distribución para el día de hoy y retorna la ruta correspondiente al distribuidor autenticado.
     */
    @Transactional
    public RutaResponse generarRutaParaDistribuidorActual(LocalDate fecha) {
        LocalDate fechaConsulta = fecha != null ? fecha : LocalDate.now();
        UsuarioPrincipal usuarioActual = obtenerPrincipalActual();
        if (usuarioActual == null) {
            throw new AccesoDenegadoException("Usuario no autenticado");
        }
        Distribuidor distribuidor = empleadoService.obtenerDistribuidorPorUsuarioId(usuarioActual.getId());

        Optional<RutaDistribuidor> existente = rutaDistribuidorRepository.findByResponsableIdAndFechaAndEliminadoFalse(distribuidor.getId(), fechaConsulta);
        if (existente.isPresent()) {
            RutaDistribuidor rutaActual = incorporarPedidosPendientesSiExisten(existente.get(), distribuidor);
            return rutaMapperHelper.aResponse(rutaActual);
        }

        List<RutaResponse> creadas = crearRutasAutomaticas(fechaConsulta);
        return creadas.stream()
                .filter(r -> r.responsable() != null && distribuidor.getId().equals(r.responsable().id()))
                .findFirst()
                .orElseThrow(() -> new RecursoNoEncontradoException("No se asignaron pedidos para el distribuidor actual"));
    }

    /**
     * Incorpora a la ruta del día cualquier pedido nuevo pendiente de reparto (incluyendo pedidos en EFECTIVO
     * cargados durante la jornada) que aún no tenga distribuidor asignado.
     */
    private RutaDistribuidor incorporarPedidosPendientesSiExisten(RutaDistribuidor ruta, Distribuidor distribuidor) {
        List<Pedido> nuevosPendientes = pedidoService.listarFacturadosPendientes();
        if (nuevosPendientes == null || nuevosPendientes.isEmpty()) {
            return ruta;
        }

        boolean huboCambios = false;
        for (Pedido nuevo : nuevosPendientes) {
            boolean yaEnRuta = ruta.getPedidos().stream()
                    .anyMatch(p -> p.getId().equals(nuevo.getId()));
            if (!yaEnRuta) {
                pedidoService.asignarAReparto(
                        nuevo.getId(),
                        distribuidor.getId(),
                        "Incorporado a ruta activa del distribuidor " + distribuidor.getNombre() + " " + distribuidor.getApellido()
                );
                nuevo.setEstado(EstadoPedido.EN_REPARTO);
                nuevo.setDistribuidor(distribuidor);
                ruta.getPedidos().add(nuevo);
                huboCambios = true;
            }
        }

        if (huboCambios) {
            ruta.setEstado(RutaEstado.PENDIENTE);
            if (ruta.getParadaActual() == null) {
                ruta.getPedidos().stream()
                        .filter(p -> p.getEstado() == EstadoPedido.EN_REPARTO)
                        .findFirst()
                        .ifPresent(p -> ruta.setParadaActual(p.getPuntoDeVenta()));
            }
            return rutaDistribuidorRepository.save(ruta);
        }

        return ruta;
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
     * Genera de forma 100% automática las rutas diarias de distribución y entrega a partir de pedidos facturados,
     * delegando el cálculo de matrices de distancias viales a {@code /table/v1/driving/} de OSRM y la optimización
     * de cada recorrido al solver TSP {@code /trip/v1/driving/} de OSRM.
     */
    @Transactional
    public List<RutaResponse> crearRutasAutomaticas(LocalDate fecha) {
        LocalDate fechaOperativa = fecha != null ? fecha : LocalDate.now();

        List<Distribuidor> todosDistribuidores = empleadoService.listarTodosLosDistribuidoresActivos();
        if (todosDistribuidores.isEmpty()) {
            throw new ValidacionException("No hay distribuidores activos registrados en el sistema");
        }

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

        // Asegurar que todos los puntos de venta de los pedidos tengan coordenadas geocodificadas en OpenStreetMap
        List<double[]> coordsPedidos = new ArrayList<>(pedidosFacturados.size());
        for (Pedido p : pedidosFacturados) {
            PuntoDeVenta pdv = p.getPuntoDeVenta();
            if (pdv != null && pdv.getDireccion() != null) {
                direccionService.asegurarCoordenadas(pdv.getDireccion());
                Direccion dir = pdv.getDireccion();
                coordsPedidos.add(new double[]{
                        dir.getLatitud() != null ? dir.getLatitud() : origenCentral[0],
                        dir.getLongitud() != null ? dir.getLongitud() : origenCentral[1]
                });
            } else {
                coordsPedidos.add(new double[]{origenCentral[0], origenCentral[1]});
            }
        }

        // Consultar a OSRM (/table/v1/driving/) todas las distancias viales desde la Sede Central en una sola llamada
        double[] distanciasDesdeCentral = rutaOptimizerStrategy.calcularDistanciasDesdeOrigen(
                origenCentral[0],
                origenCentral[1],
                coordsPedidos
        );

        Map<String, Integer> indicePedidoMap = new HashMap<>();
        for (int i = 0; i < pedidosFacturados.size(); i++) {
            indicePedidoMap.put(pedidosFacturados.get(i).getId(), i);
        }

        // Ordenar pedidos por proximidad vial de su punto de venta a la Sede Central (TimSort O(n log n))
        List<Pedido> pedidosOrdenadosPorCercaniaCentral = new ArrayList<>(pedidosFacturados);
        pedidosOrdenadosPorCercaniaCentral.sort(Comparator.comparingDouble(p -> {
            Integer idx = indicePedidoMap.get(p.getId());
            return idx != null && idx < distanciasDesdeCentral.length ? distanciasDesdeCentral[idx] : Double.MAX_VALUE;
        }));

        int cantidadDistribuidores = Math.min(distribuidoresDisponibles.size(), pedidosFacturados.size());
        List<Distribuidor> distribuidoresAUsar = new ArrayList<>(distribuidoresDisponibles.subList(0, cantidadDistribuidores));
        Collections.shuffle(distribuidoresAUsar);

        Map<Distribuidor, List<Pedido>> asignaciones = new LinkedHashMap<>();
        for (int i = 0; i < cantidadDistribuidores; i++) {
            Distribuidor dist = distribuidoresAUsar.get(i);
            Pedido pedidoSemilla = pedidosOrdenadosPorCercaniaCentral.get(i);
            List<Pedido> lista = new ArrayList<>();
            lista.add(pedidoSemilla);
            asignaciones.put(dist, lista);
        }

        int cuotaPorDistribuidor = (int) Math.ceil((double) pedidosFacturados.size() / cantidadDistribuidores);

        // Si quedan pedidos por asignar, obtener la matriz completa de distancias viales en OSRM (/table/v1/driving/)
        double[][] matrizDistanciasEntrePedidos = pedidosFacturados.size() > cantidadDistribuidores
                ? rutaOptimizerStrategy.calcularMatrizDistancias(coordsPedidos)
                : new double[0][0];

        for (int i = cantidadDistribuidores; i < pedidosOrdenadosPorCercaniaCentral.size(); i++) {
            Pedido pedidoPendiente = pedidosOrdenadosPorCercaniaCentral.get(i);
            Integer idxPendiente = indicePedidoMap.get(pedidoPendiente.getId());

            Distribuidor mejorDistribuidor = null;
            double menorDistanciaAUltimoPdv = Double.MAX_VALUE;

            for (Distribuidor dist : distribuidoresAUsar) {
                List<Pedido> asignados = asignaciones.get(dist);
                if (asignados.size() >= cuotaPorDistribuidor) {
                    continue;
                }

                Pedido ultimoPedido = asignados.get(asignados.size() - 1);
                Integer idxUltimo = indicePedidoMap.get(ultimoPedido.getId());

                double distMetros = Double.MAX_VALUE;
                if (idxUltimo != null && idxPendiente != null
                        && idxUltimo < matrizDistanciasEntrePedidos.length
                        && idxPendiente < matrizDistanciasEntrePedidos[idxUltimo].length) {
                    distMetros = matrizDistanciasEntrePedidos[idxUltimo][idxPendiente];
                }

                if (distMetros < menorDistanciaAUltimoPdv) {
                    menorDistanciaAUltimoPdv = distMetros;
                    mejorDistribuidor = dist;
                }
            }

            if (mejorDistribuidor == null) {
                mejorDistribuidor = distribuidoresAUsar.stream()
                        .min(Comparator.comparingInt(d -> asignaciones.get(d).size()))
                        .orElse(distribuidoresAUsar.get(0));
            }

            asignaciones.get(mejorDistribuidor).add(pedidoPendiente);
        }

        List<RutaResponse> resultado = new ArrayList<>();
        for (Map.Entry<Distribuidor, List<Pedido>> entry : asignaciones.entrySet()) {
            Distribuidor distribuidor = entry.getKey();
            List<Pedido> pedidosDeDistribuidor = entry.getValue();

            Map<String, List<Pedido>> mapaPuntosAPedidos = new LinkedHashMap<>();
            List<PuntoDeVenta> puntosAVisitar = new ArrayList<>();
            for (Pedido p : pedidosDeDistribuidor) {
                PuntoDeVenta pdv = p.getPuntoDeVenta();
                if (!mapaPuntosAPedidos.containsKey(pdv.getId())) {
                    mapaPuntosAPedidos.put(pdv.getId(), new ArrayList<>());
                    puntosAVisitar.add(pdv);
                }
                mapaPuntosAPedidos.get(pdv.getId()).add(p);
            }

            RutaResponse rutaResp = armarYPersistirRuta(distribuidor, fechaOperativa, puntosAVisitar, mapaPuntosAPedidos);
            resultado.add(rutaResp);
        }

        return resultado;
    }

    /**
     * Secuencia los puntos de venta delegando el cálculo del recorrido óptimo a {@code /trip/v1/driving/} de OSRM,
     * transiciona los pedidos a EN_REPARTO y persiste la entidad {@link RutaDistribuidor}.
     */
    private RutaResponse armarYPersistirRuta(
            Distribuidor distribuidor,
            LocalDate fecha,
            List<PuntoDeVenta> puntosAVisitar,
            Map<String, List<Pedido>> mapaPuntosAPedidos) {

        double[] origen = obtenerCoordenadasSedeCentral();

        List<PuntoDeVenta> puntosOrdenados = rutaOptimizerStrategy.optimizarRecorrido(
                origen[0],
                origen[1],
                puntosAVisitar
        );

        List<Pedido> pedidosOrdenados = new ArrayList<>();
        for (PuntoDeVenta pdv : puntosOrdenados) {
            List<Pedido> pedidosDelPdv = mapaPuntosAPedidos.get(pdv.getId());
            if (pedidosDelPdv != null) {
                pedidosOrdenados.addAll(pedidosDelPdv);
            }
        }

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

        Pedido primerPendiente = ruta.getPedidos().stream()
                .filter(p -> p.getEstado() == EstadoPedido.EN_REPARTO)
                .findFirst()
                .orElse(null);

        if (primerPendiente != null && !primerPendiente.getId().equals(pedidoEntregado.getId())) {
            String nombreComercioSig = primerPendiente.getPuntoDeVenta() != null
                    ? primerPendiente.getPuntoDeVenta().getNombreComercio()
                    : String.valueOf(primerPendiente.getNumeroPedido());
            throw new ValidacionException(
                    "Solo puede registrar la entrega de la siguiente parada pendiente en el orden de la ruta ("
                            + nombreComercioSig + ")"
            );
        }

        String obs = request.observacion() != null && !request.observacion().isBlank()
                ? request.observacion()
                : "Entrega completada por el distribuidor";

        boolean esEfectivo = pedidoEntregado.getFormaDePago() != null
                && pedidoEntregado.getFormaDePago().getTipoPago() == TipoPago.EFECTIVO;

        if (esEfectivo) {
            UsuarioPrincipal up = obtenerPrincipalActual();
            String usuarioDist = up != null ? up.getUsername() : "distribuidor";
            procesamientoPagoService.registrarCobroEfectivoEnEntrega(
                    new RegistrarCobroEfectivoRequest(pedidoEntregado.getId(), pedidoEntregado.getTotal(), obs),
                    usuarioDist
            );
            pedidoEntregado.setEstado(EstadoPedido.ENTREGADO);
        } else {
            // Marcar pedido entregado usando PedidoService para asegurar coherencia y trazabilidad
            pedidoService.marcarEntregado(pedidoEntregado.getId(), obs);
            pedidoEntregado.setEstado(EstadoPedido.ENTREGADO);
        }

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
                .allMatch(p -> p.getEstado() == EstadoPedido.ENTREGADO || p.getEstado() == EstadoPedido.FACTURADO);

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
            emailDestino = emailService.resolverEmailMinorista(pdv.getMinorista());
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
                if (!dir.isEliminado()) {
                    direccionService.asegurarCoordenadas(dir);
                    if (dir.getLatitud() != null && dir.getLongitud() != null
                            && (dir.getLatitud() != 0.0 || dir.getLongitud() != 0.0)) {
                        return new double[]{dir.getLatitud(), dir.getLongitud()};
                    }
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
