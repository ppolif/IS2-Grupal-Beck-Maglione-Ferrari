package com.DulceMayor.backend.services.ruta;

import com.DulceMayor.backend.config.AccesoDenegadoException;
import com.DulceMayor.backend.config.ConflictoException;
import com.DulceMayor.backend.config.RecursoNoEncontradoException;
import com.DulceMayor.backend.config.UsuarioPrincipal;
import com.DulceMayor.backend.config.ValidacionException;
import com.DulceMayor.backend.dtos.persona.PuntoDeVentaCarteraActualizadaEvent;
import com.DulceMayor.backend.dtos.ruta.AvanceRutaResponse;
import com.DulceMayor.backend.dtos.ruta.CompletarParadaPreventorRequest;
import com.DulceMayor.backend.dtos.ruta.ParadaResponse;
import com.DulceMayor.backend.dtos.ruta.RutaPreventorGenerarRequest;
import com.DulceMayor.backend.dtos.ruta.RutaResponse;
import com.DulceMayor.backend.entities.RolUsuario;
import com.DulceMayor.backend.entities.direccion.Direccion;
import com.DulceMayor.backend.entities.mayorista.Mayorista;
import com.DulceMayor.backend.entities.persona.Empleado;
import com.DulceMayor.backend.entities.persona.Preventor;
import com.DulceMayor.backend.entities.persona.PuntoDeVenta;
import com.DulceMayor.backend.entities.ruta.RutaEstado;
import com.DulceMayor.backend.entities.ruta.RutaPreventor;
import com.DulceMayor.backend.repositories.ruta.RutaPreventorRepository;
import com.DulceMayor.backend.services.direccion.DireccionService;
import com.DulceMayor.backend.services.mayorista.MayoristaService;
import com.DulceMayor.backend.services.persona.EmpleadoService;
import com.DulceMayor.backend.services.persona.PuntoDeVentaService;
import com.DulceMayor.backend.services.ruta.strategy.OsrmRoutingStrategy;
import com.DulceMayor.backend.services.ruta.strategy.RutaOptimizerStrategy;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de lógica de negocio para la gestión de rutas diarias de preventores.
 *
 * <p><b>Punto de partida automático y delegación a OSRM / OpenStreetMap:</b></p>
 * El servicio obtiene automáticamente la dirección física de la <b>sucursal</b> a la que pertenece el preventor
 * (mediante su asociación con {@link Mayorista}, o la Sede Central por defecto), asegura que todos los comercios
 * de su cartera tengan coordenadas geocodificadas en OpenStreetMap y delega en {@link RutaOptimizerStrategy}
 * ({@link OsrmRoutingStrategy}) la resolución completa de la ruta más óptima mediante el servicio
 * {@code /trip/v1/driving/} de OSRM.
 */
@Service
@RequiredArgsConstructor
public class RutaPreventorService {

    private final RutaPreventorRepository rutaPreventorRepository;
    private final EmpleadoService empleadoService;
    private final PuntoDeVentaService puntoDeVentaService;
    private final DireccionService direccionService;
    private final MayoristaService mayoristaService;
    private final RutaOptimizerStrategy rutaOptimizerStrategy;
    private final RutaMapperHelper rutaMapperHelper;

    /**
     * Lista las rutas de preventores según los filtros provistos.
     * Si el usuario autenticado tiene rol {@code PREVENTISTA}, solo puede acceder a sus propias rutas.
     *
     * @param responsableId Filtro opcional por ID de preventor (solo aplicable a jefes y administrativos).
     * @param fecha         Filtro opcional por fecha.
     * @param estado        Filtro opcional por estado de ruta.
     * @return Lista de {@link RutaResponse}.
     */
    @Transactional
    public List<RutaResponse> listarRutas(String responsableId, LocalDate fecha, String estado) {
        UsuarioPrincipal usuarioActual = obtenerPrincipalActual();
        String responsableFiltro = responsableId;

        if (usuarioActual != null && usuarioActual.getRol() == RolUsuario.PREVENTISTA) {
            Preventor preventor = empleadoService.obtenerPreventorPorUsuarioId(usuarioActual.getId());
            responsableFiltro = preventor.getId();
        }

        RutaEstado rutaEstado = parsearEstado(estado);
        List<RutaPreventor> rutas = rutaPreventorRepository.buscarConFiltros(responsableFiltro, fecha, rutaEstado);

        LocalDate hoy = LocalDate.now();
        for (RutaPreventor ruta : rutas) {
            if (ruta.getResponsable() instanceof Preventor prev
                    && (ruta.getEstado() == RutaEstado.PENDIENTE || !ruta.getFecha().isBefore(hoy))) {
                sincronizarYReoptimizarSiCambioCartera(ruta, prev);
            }
        }

        return rutas.stream()
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
     * Busca la ruta asignada al preventor autenticado para una fecha dada (por defecto hoy).
     * Si no existía aún pero el preventor posee comercios en su cartera, la genera automáticamente con OSRM.
     * Si ya existía y se detectan nuevos comercios en la cartera o direcciones sin coordenadas,
     * los geocodifica y reoptimiza el recorrido con OSRM partiendo de su sucursal.
     */
    @Transactional
    public RutaResponse buscarMiRutaDelDia(LocalDate fecha) {
        LocalDate fechaConsulta = fecha != null ? fecha : LocalDate.now();
        UsuarioPrincipal usuarioActual = obtenerPrincipalActual();
        if (usuarioActual == null) {
            throw new AccesoDenegadoException("Usuario no autenticado");
        }
        Preventor preventor = empleadoService.obtenerPreventorPorUsuarioId(usuarioActual.getId());

        Optional<RutaPreventor> rutaOpt = rutaPreventorRepository
                .findByResponsableIdAndFechaAndEliminadoFalse(preventor.getId(), fechaConsulta);

        if (rutaOpt.isEmpty()) {
            List<PuntoDeVenta> cartera = puntoDeVentaService.listarPorPreventor(preventor.getId());
            if (cartera.isEmpty()) {
                throw new RecursoNoEncontradoException("No se encontró una ruta de preventa para la fecha " + fechaConsulta);
            }
            return generarRuta(new RutaPreventorGenerarRequest(preventor.getId(), fechaConsulta));
        }

        RutaPreventor ruta = rutaOpt.get();
        sincronizarYReoptimizarSiCambioCartera(ruta, preventor);

        return rutaMapperHelper.aResponse(ruta);
    }

    /**
     * Busca una ruta de preventor por su identificador único.
     * Valida permisos: si el usuario es {@code PREVENTISTA}, solo puede consultar su propia ruta.
     *
     * @param id Identificador de la ruta.
     * @return {@link RutaResponse}.
     */
    @Transactional(readOnly = true)
    public RutaResponse buscarPorId(String id) {
        RutaPreventor ruta = rutaPreventorRepository.findByIdAndEliminadoFalse(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Ruta de preventor no encontrada: " + id));

        UsuarioPrincipal usuarioActual = obtenerPrincipalActual();
        if (usuarioActual != null && usuarioActual.getRol() == RolUsuario.PREVENTISTA) {
            Preventor preventor = empleadoService.obtenerPreventorPorUsuarioId(usuarioActual.getId());
            if (!ruta.getResponsable().getId().equals(preventor.getId())) {
                throw new AccesoDenegadoException("No tiene permisos para consultar una ruta ajena");
            }
        }

        return rutaMapperHelper.aResponse(ruta);
    }

    /**
     * Genera o recalcula la ruta diaria de un preventor delegando el cálculo del recorrido óptimo
     * al servicio {@code /trip/v1/driving/} de OSRM partiendo desde la sucursal del preventor.
     */
    @Transactional
    public RutaResponse generarRuta(RutaPreventorGenerarRequest request) {
        LocalDate fechaOperativa = (request != null && request.fecha() != null)
                ? request.fecha()
                : LocalDate.now();

        UsuarioPrincipal usuarioActual = obtenerPrincipalActual();
        String preventorId = request != null ? request.preventorId() : null;

        if (usuarioActual != null && usuarioActual.getRol() == RolUsuario.PREVENTISTA) {
            Preventor preventor = empleadoService.obtenerPreventorPorUsuarioId(usuarioActual.getId());
            preventorId = preventor.getId();
        } else if (preventorId == null || preventorId.isBlank()) {
            throw new ValidacionException("Debe especificar el ID del preventor");
        }

        Preventor preventorEntity = empleadoService.obtenerPreventor(preventorId);

        List<PuntoDeVenta> cartera = puntoDeVentaService.listarPorPreventor(preventorId);
        if (cartera.isEmpty()) {
            throw new ValidacionException("El preventor no tiene puntos de venta asignados en su cartera");
        }

        for (PuntoDeVenta pdv : cartera) {
            direccionService.asegurarCoordenadas(pdv.getDireccion());
        }

        double[] origen = obtenerCoordenadasSucursal(preventorEntity);

        List<PuntoDeVenta> paradasOrdenadas = rutaOptimizerStrategy.optimizarRecorrido(
                origen[0],
                origen[1],
                cartera
        );

        Optional<RutaPreventor> existenteOpt = rutaPreventorRepository
                .findByResponsableIdAndFechaAndEliminadoFalse(preventorId, fechaOperativa);

        if (existenteOpt.isPresent()) {
            RutaPreventor existente = existenteOpt.get();
            reemplazarParadasYActualizarEstado(existente, paradasOrdenadas);
            RutaPreventor actualizada = rutaPreventorRepository.saveAndFlush(existente);
            return rutaMapperHelper.aResponse(actualizada);
        }

        PuntoDeVenta primerPunto = paradasOrdenadas.isEmpty() ? null : paradasOrdenadas.get(0);

        RutaPreventor ruta = RutaPreventor.builder()
                .fecha(fechaOperativa)
                .estado(RutaEstado.PENDIENTE)
                .responsable(preventorEntity)
                .paradaActual(primerPunto)
                .puntosDeVenta(new ArrayList<>(paradasOrdenadas))
                .visitadosIds(new ArrayList<>())
                .build();

        RutaPreventor guardada = rutaPreventorRepository.saveAndFlush(ruta);
        return rutaMapperHelper.aResponse(guardada);
    }

    /**
     * Escucha el alta o modificación de un punto de venta en la cartera de un preventor
     * y reoptimiza automáticamente con OSRM las rutas vigentes del preventor.
     */
    @EventListener
    @Transactional
    public void onPuntoDeVentaCarteraActualizada(PuntoDeVentaCarteraActualizadaEvent event) {
        if (event == null || event.preventorId() == null || event.preventorId().isBlank()) {
            return;
        }
        List<RutaPreventor> rutasPreventor = rutaPreventorRepository.buscarConFiltros(event.preventorId(), null, null);
        if (rutasPreventor.isEmpty()) {
            return;
        }
        Preventor preventor = empleadoService.obtenerPreventor(event.preventorId());
        LocalDate hoy = LocalDate.now();

        for (RutaPreventor ruta : rutasPreventor) {
            if (ruta.getEstado() == RutaEstado.PENDIENTE || !ruta.getFecha().isBefore(hoy)) {
                reoptimizarRutaConCarteraActual(ruta, preventor);
            }
        }
    }

    private void sincronizarYReoptimizarSiCambioCartera(RutaPreventor ruta, Preventor preventor) {
        List<PuntoDeVenta> carteraActual = puntoDeVentaService.listarPorPreventor(preventor.getId());
        if (carteraActual.isEmpty()) {
            return;
        }

        boolean requiereReoptimizar = false;
        for (PuntoDeVenta pdv : carteraActual) {
            if (direccionService.asegurarCoordenadas(pdv.getDireccion())) {
                requiereReoptimizar = true;
            }
        }

        Set<String> idsEnRuta = new HashSet<>();
        if (ruta.getPuntosDeVenta() != null) {
            for (PuntoDeVenta p : ruta.getPuntosDeVenta()) {
                idsEnRuta.add(p.getId());
            }
        }

        if (idsEnRuta.size() != carteraActual.size()) {
            requiereReoptimizar = true;
        } else {
            for (PuntoDeVenta p : carteraActual) {
                if (!idsEnRuta.contains(p.getId())) {
                    requiereReoptimizar = true;
                    break;
                }
            }
        }

        if (requiereReoptimizar) {
            reoptimizarRutaConCarteraActual(ruta, preventor);
        }
    }

    private void reoptimizarRutaConCarteraActual(RutaPreventor ruta, Preventor preventor) {
        List<PuntoDeVenta> cartera = puntoDeVentaService.listarPorPreventor(preventor.getId());
        if (cartera.isEmpty()) {
            return;
        }
        for (PuntoDeVenta pdv : cartera) {
            direccionService.asegurarCoordenadas(pdv.getDireccion());
        }
        double[] origen = obtenerCoordenadasSucursal(preventor);
        List<PuntoDeVenta> paradasOrdenadas = rutaOptimizerStrategy.optimizarRecorrido(
                origen[0],
                origen[1],
                cartera
        );
        reemplazarParadasYActualizarEstado(ruta, paradasOrdenadas);
        rutaPreventorRepository.saveAndFlush(ruta);
    }

    private void reemplazarParadasYActualizarEstado(RutaPreventor ruta, List<PuntoDeVenta> paradasOrdenadas) {
        if (ruta.getPuntosDeVenta() == null) {
            ruta.setPuntosDeVenta(new ArrayList<>(paradasOrdenadas));
        } else {
            ruta.getPuntosDeVenta().clear();
            ruta.getPuntosDeVenta().addAll(paradasOrdenadas);
        }

        List<String> visitados = ruta.getVisitadosIds() != null ? ruta.getVisitadosIds() : List.of();
        PuntoDeVenta siguiente = null;
        for (PuntoDeVenta pdv : paradasOrdenadas) {
            if (!visitados.contains(pdv.getId())) {
                siguiente = pdv;
                break;
            }
        }
        ruta.setParadaActual(siguiente);
        if (siguiente != null) {
            ruta.setEstado(RutaEstado.PENDIENTE);
        }
    }

    @Transactional
    public AvanceRutaResponse completarParada(String id, CompletarParadaPreventorRequest request) {
        if (request == null || request.puntoDeVentaId() == null || request.puntoDeVentaId().isBlank()) {
            throw new ValidacionException("El ID del punto de venta es requerido");
        }

        RutaPreventor ruta = rutaPreventorRepository.findByIdAndEliminadoFalse(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Ruta no encontrada: " + id));

        if (ruta.getEstado() != RutaEstado.PENDIENTE) {
            throw new ConflictoException("La ruta no se encuentra en estado PENDIENTE");
        }

        PuntoDeVenta primeraPendiente = null;
        for (PuntoDeVenta pdv : ruta.getPuntosDeVenta()) {
            if (!ruta.getVisitadosIds().contains(pdv.getId())) {
                primeraPendiente = pdv;
                break;
            }
        }

        if (primeraPendiente == null) {
            throw new ConflictoException("Todas las paradas de la ruta ya fueron visitadas");
        }

        if (!primeraPendiente.getId().equals(request.puntoDeVentaId())) {
            throw new ValidacionException(
                    "Solo puede marcar como visitada la siguiente parada pendiente en el orden de la ruta ("
                            + primeraPendiente.getNombreComercio() + ")"
            );
        }

        if (!ruta.getVisitadosIds().contains(request.puntoDeVentaId())) {
            ruta.getVisitadosIds().add(request.puntoDeVentaId());
        }

        PuntoDeVenta siguientePunto = null;
        for (PuntoDeVenta pdv : ruta.getPuntosDeVenta()) {
            if (!ruta.getVisitadosIds().contains(pdv.getId())) {
                siguientePunto = pdv;
                break;
            }
        }

        boolean finalizada = false;
        if (siguientePunto != null) {
            ruta.setParadaActual(siguientePunto);
        } else {
            ruta.setParadaActual(null);
            ruta.setEstado(RutaEstado.REALIZADA);
            finalizada = true;
        }

        RutaPreventor actualizada = rutaPreventorRepository.save(ruta);
        RutaResponse rutaRes = rutaMapperHelper.aResponse(actualizada);

        ParadaResponse siguienteParadaRes = null;
        if (siguientePunto != null && rutaRes.paradas() != null) {
            final String sigId = siguientePunto.getId();
            siguienteParadaRes = rutaRes.paradas().stream()
                    .filter(p -> p.puntoDeVenta() != null && sigId.equals(p.puntoDeVenta().id()))
                    .findFirst()
                    .orElse(null);
        }

        return new AvanceRutaResponse(rutaRes, siguienteParadaRes, finalizada);
    }

    @Transactional
    public RutaResponse cerrarRuta(String id) {
        RutaPreventor ruta = rutaPreventorRepository.findByIdAndEliminadoFalse(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Ruta no encontrada: " + id));

        if (ruta.getEstado() != RutaEstado.PENDIENTE) {
            throw new ConflictoException("La ruta ya se encuentra cerrada con estado " + ruta.getEstado());
        }

        boolean completa = ruta.getPuntosDeVenta().stream()
                .allMatch(pdv -> ruta.getVisitadosIds().contains(pdv.getId()));

        ruta.setEstado(completa ? RutaEstado.REALIZADA : RutaEstado.INCOMPLETA);
        ruta.setParadaActual(null);

        RutaPreventor cerrada = rutaPreventorRepository.save(ruta);
        return rutaMapperHelper.aResponse(cerrada);
    }

    /**
     * Obtiene automáticamente las coordenadas GPS (latitud y longitud) de la sucursal
     * a la que pertenece el preventor (o de la Sede Central si no tuviera sucursal asignada),
     * garantizando compatibilidad con OpenStreetMap y OSRM.
     */
    private double[] obtenerCoordenadasSucursal(Empleado empleado) {
        Mayorista sucursal = empleado != null && empleado.getMayorista() != null
                ? empleado.getMayorista()
                : mayoristaService.obtenerSedeCentral();

        if (sucursal != null && sucursal.getDirecciones() != null) {
            for (Direccion dir : sucursal.getDirecciones()) {
                if (!dir.isEliminado()) {
                    direccionService.asegurarCoordenadas(dir);
                    if (dir.getLatitud() != null && dir.getLongitud() != null
                            && (dir.getLatitud() != 0.0 || dir.getLongitud() != 0.0)) {
                        return new double[]{dir.getLatitud(), dir.getLongitud()};
                    }
                }
            }
        }
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
