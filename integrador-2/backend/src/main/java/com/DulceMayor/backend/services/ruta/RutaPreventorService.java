package com.DulceMayor.backend.services.ruta;

import com.DulceMayor.backend.config.AccesoDenegadoException;
import com.DulceMayor.backend.config.ConflictoException;
import com.DulceMayor.backend.config.RecursoNoEncontradoException;
import com.DulceMayor.backend.config.UsuarioPrincipal;
import com.DulceMayor.backend.config.ValidacionException;
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
import com.DulceMayor.backend.services.persona.EmpleadoService;
import com.DulceMayor.backend.services.persona.PuntoDeVentaService;
import com.DulceMayor.backend.services.ruta.strategy.NearestNeighborStrategy;
import com.DulceMayor.backend.services.ruta.strategy.RutaOptimizerStrategy;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de lógica de negocio para la gestión de rutas diarias de preventores.
 *
 * <p><b>Punto de partida automático y OpenStreetMap:</b></p>
 * En concordancia con los requerimientos, el preventor no necesita ingresar coordenadas manuales de partida.
 * El servicio obtiene automáticamente la dirección física de la <b>sucursal</b> a la que pertenece el preventor
 * (mediante su asociación con {@link Mayorista}), extrayendo su latitud y longitud. Estas coordenadas compatibles
 * con <b>OpenStreetMap</b> sirven como punto de origen para alimentar el algoritmo de enrutamiento.
 *
 * <p><b>Patrón GoF aplicado: Strategy (Comportamiento)</b></p>
 * Inyecta {@link RutaOptimizerStrategy} (instancia concreta {@link NearestNeighborStrategy}) para delegar
 * el ordenamiento geodésico de los puntos de venta asignados a la cartera del preventor.
 */
@Service
@RequiredArgsConstructor
public class RutaPreventorService {

    private final RutaPreventorRepository rutaPreventorRepository;
    private final EmpleadoService empleadoService;
    private final PuntoDeVentaService puntoDeVentaService;
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
    @Transactional(readOnly = true)
    public List<RutaResponse> listarRutas(String responsableId, LocalDate fecha, String estado) {
        UsuarioPrincipal usuarioActual = obtenerPrincipalActual();
        String responsableFiltro = responsableId;

        if (usuarioActual != null && usuarioActual.getRol() == RolUsuario.PREVENTISTA) {
            Preventor preventor = empleadoService.obtenerPreventorPorUsuarioId(usuarioActual.getId());
            responsableFiltro = preventor.getId();
        }

        RutaEstado rutaEstado = parsearEstado(estado);

        return rutaPreventorRepository.buscarConFiltros(responsableFiltro, fecha, rutaEstado).stream()
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
     * Utilizado principalmente por la aplicación móvil al iniciar la jornada laboral.
     *
     * @param fecha Fecha consultada (opcional, defaults a la fecha actual).
     * @return {@link RutaResponse} con las paradas del día ordenadas geográficamente.
     */
    @Transactional(readOnly = true)
    public RutaResponse buscarMiRutaDelDia(LocalDate fecha) {
        LocalDate fechaConsulta = fecha != null ? fecha : LocalDate.now();
        UsuarioPrincipal usuarioActual = obtenerPrincipalActual();
        if (usuarioActual == null) {
            throw new AccesoDenegadoException("Usuario no autenticado");
        }
        Preventor preventor = empleadoService.obtenerPreventorPorUsuarioId(usuarioActual.getId());

        RutaPreventor ruta = rutaPreventorRepository.findByResponsableIdAndFechaAndEliminadoFalse(preventor.getId(), fechaConsulta)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró una ruta de preventa para la fecha " + fechaConsulta));

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
     * Genera la ruta diaria de un preventor.
     *
     * <p><b>Reglas de Negocio aplicadas:</b></p>
     * <ul>
     *   <li>Verifica que no exista otra ruta para ese mismo empleado en la misma fecha (409 Conflict).</li>
     *   <li>Recupera la cartera de {@link PuntoDeVenta} asignados a este preventor.</li>
     *   <li>Obtiene la ubicación de salida automáticamente desde la sucursal del preventor (datos espaciales de OpenStreetMap).</li>
     *   <li>Aplica el algoritmo del Vecino Más Cercano (Haversine) mediante {@link RutaOptimizerStrategy}.</li>
     *   <li>Inicializa el estado en {@link RutaEstado#PENDIENTE} y establece {@code paradaActual} en el primer punto.</li>
     * </ul>
     *
     * @param request Solicitud con preventorId y fecha.
     * @return {@link RutaResponse} generada.
     */
    @Transactional
    public RutaResponse generarRuta(RutaPreventorGenerarRequest request) {
        if (request == null || request.fecha() == null) {
            throw new ValidacionException("La fecha es obligatoria para generar la ruta");
        }

        UsuarioPrincipal usuarioActual = obtenerPrincipalActual();
        String preventorId = request.preventorId();

        if (usuarioActual != null && usuarioActual.getRol() == RolUsuario.PREVENTISTA) {
            Preventor preventor = empleadoService.obtenerPreventorPorUsuarioId(usuarioActual.getId());
            preventorId = preventor.getId();

            /// esto de aca, igual que lo de indicar la fecha en vez de hacer que siempre sea automaticamente la fecha actual
            /// es por si algun admin esta haciendo la ruta de un preventor
        } else if (preventorId == null || preventorId.isBlank()) {
            throw new ValidacionException("Debe especificar el ID del preventor");
        }

        Preventor preventorEntity = empleadoService.obtenerPreventor(preventorId);

        if (rutaPreventorRepository.existsByResponsableIdAndFechaAndEliminadoFalse(preventorId, request.fecha())) {
            throw new ConflictoException("Ya existe una ruta registrada para el preventor en la fecha " + request.fecha());
        }

        /// recordar que un punto de venta tiene un preventor asociado
        List<PuntoDeVenta> cartera = puntoDeVentaService.listarPorPreventor(preventorId);
        if (cartera.isEmpty()) {
            throw new ValidacionException("El preventor no tiene puntos de venta asignados en su cartera");
        }

        // Obtención automática del punto de origen desde la sucursal (Mayorista)
        double[] origen = obtenerCoordenadasSucursal(preventorEntity);

        // Optimización por vecino más cercano con coordenadas compatibles con OpenStreetMap
        List<PuntoDeVenta> paradasOrdenadas = rutaOptimizerStrategy.optimizarRecorrido(
                origen[0],
                origen[1],
                cartera
        );

        PuntoDeVenta primerPunto = paradasOrdenadas.isEmpty() ? null : paradasOrdenadas.get(0);

        /// patron builder papá
        RutaPreventor ruta = RutaPreventor.builder()
                .fecha(request.fecha())
                .estado(RutaEstado.PENDIENTE)
                .responsable(preventorEntity)
                .paradaActual(primerPunto)
                .puntosDeVenta(new ArrayList<>(paradasOrdenadas))
                .visitadosIds(new ArrayList<>())
                .build();

        RutaPreventor guardada = rutaPreventorRepository.save(ruta);
        return rutaMapperHelper.aResponse(guardada);
    }

    /**
     * Registra una parada completada por el preventor usando la estrategia de lista cronológica de visitados.
     *
     * <p><b>Lógica de avance:</b></p>
     * <ul>
     *   <li>Añade el ID del punto visitado a la lista {@code visitadosIds} si aún no figuraba en ella.</li>
     *   <li>Busca dentro de la lista ordenada de la ruta el siguiente {@link PuntoDeVenta} que todavía no esté en {@code visitadosIds}.</li>
     *   <li>Si existe un próximo punto, se actualiza {@code paradaActual} con dicho punto.</li>
     *   <li>Si no restan puntos pendientes por visitar, {@code paradaActual} se fija en null y la ruta pasa a {@link RutaEstado#REALIZADA}.</li>
     * </ul>
     *
     * @param id      Identificador de la ruta.
     * @param request Datos de la visita (punto de venta ID y pedido ID opcional).
     * @return {@link AvanceRutaResponse} con la ruta actualizada y la siguiente parada sugerida para el mapa.
     */
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

        if (!ruta.getVisitadosIds().contains(request.puntoDeVentaId())) {
            ruta.getVisitadosIds().add(request.puntoDeVentaId());
        }

        // Buscar el siguiente punto pendiente que no figure en la lista de visitados
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
        ///Cuando el preventor o distribuidor toca "Completar Parada", la app no solo recibe la ruta actualizada, sino que
        /// en el campo siguienteParada recibe directamente las coordenadas y datos del próximo local. Con eso, la app centra
        /// automáticamente el mapa de OpenStreetMap en el próximo destino y muestra la ruta hacia allí sin obligar al frontend
        /// a buscarlo a mano en la lista.
        /// GOOOOOOD

        return new AvanceRutaResponse(rutaRes, siguienteParadaRes, finalizada);
    }

    /**
     * Cierra anticipadamente la ruta del preventor (por ejemplo al finalizar la jornada laboral).
     * Si quedaron puntos sin visitar en la lista de paradas, la ruta pasa a {@link RutaEstado#INCOMPLETA};
     * caso contrario pasa a {@link RutaEstado#REALIZADA}.
     *
     * @param id Identificador de la ruta.
     * @return {@link RutaResponse} con el estado final de la ruta.
     */
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
     * a la que pertenece el preventor, garantizando compatibilidad con OpenStreetMap.
     */
    private double[] obtenerCoordenadasSucursal(Empleado empleado) {
        if (empleado.getMayorista() != null && empleado.getMayorista().getDirecciones() != null) {
            for (Direccion dir : empleado.getMayorista().getDirecciones()) {
                if (!dir.isEliminado() && dir.getLatitud() != null && dir.getLongitud() != null
                        && (dir.getLatitud() != 0.0 || dir.getLongitud() != 0.0)) {
                    return new double[]{dir.getLatitud(), dir.getLongitud()};
                }
            }
        }
        // Coordenadas por defecto de Mendoza Centro (Sede Dulce Mayor) si no tiene dirección cargada
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
