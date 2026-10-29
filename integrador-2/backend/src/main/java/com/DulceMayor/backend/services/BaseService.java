package com.DulceMayor.backend.services;

import com.DulceMayor.backend.config.RecursoNoEncontradoException;
import com.DulceMayor.backend.dtos.BaseMapper;
import com.DulceMayor.backend.entities.EntidadBase;
import com.DulceMayor.backend.repositories.BaseRepository;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;

/**
 * Clase base abstracta para los servicios de negocio CRUD del sistema.
 *
 * <p><b>Patrón aplicado: Template Method (Método Plantilla) en la capa de Servicio.</b></p>
 * <p><b>Por qué se aplica acá:</b> Todas las entidades administrables del dominio comparten un
 * ciclo de vida transaccional invariante para las operaciones de alta, modificación, baja lógica,
 * búsqueda individual y listado:
 * {@code normalizar/preparar -> validar reglas de negocio -> mapear DTO/Entidad -> persistir -> pos-procesar -> proyectar a DTO}.
 * El patrón Template Method fija el esqueleto de este algoritmo en los métodos plantilla
 * ({@link #crear(Object)}, {@link #modificar(String, Object)}, {@link #eliminar(String)},
 * {@link #buscar(String)} y {@link #listar()}) evitando duplicar el flujo en los ~30 módulos
 * del sistema, mientras delega en las subclases concretas únicamente los pasos variables
 * (normalización de datos, validaciones de unicidad o integridad y ganchos pre/post persistencia).</p>
 *
 * <p><b>Patrón aplicado: DTO (Data Transfer Object) en la frontera del servicio.</b></p>
 * <p><b>Por qué se aplica acá:</b> Los métodos plantilla públicos orientados a la capa de
 * presentación reciben {@code Req} y devuelven {@code Res}, garantizando que los controladores
 * nunca manipulen entidades JPA. Para la colaboración interna entre módulos de negocio (por
 * ejemplo, {@code SubCategoriaService} consultando una {@code Categoria} activa), se provee
 * el método de acceso interno {@link #obtenerEntidad(String)}.</p>
 *
 * @param <E>   Tipo de la entidad persistente que hereda de {@link EntidadBase}.
 * @param <Req> Tipo del DTO de entrada (Request).
 * @param <Res> Tipo del DTO de salida (Response).
 */
public abstract class BaseService<E extends EntidadBase, Req, Res> {

    protected final BaseRepository<E> repository;
    protected final BaseMapper<E, Req, Res> mapper;
    protected final String nombreRecurso;

    protected BaseService(BaseRepository<E> repository, BaseMapper<E, Req, Res> mapper, String nombreRecurso) {
        this.repository = repository;
        this.mapper = mapper;
        this.nombreRecurso = nombreRecurso;
    }

    /**
     * Devuelve el repositorio específico de la entidad administrada por este servicio.
     */
    public BaseRepository<E> getRepository() {
        return repository;
    }

    /**
     * Devuelve el mapper MapStruct responsable de convertir entre la entidad y sus DTOs.
     */
    public BaseMapper<E, Req, Res> getMapper() {
        return mapper;
    }

    /**
     * Nombre legible del recurso para construir mensajes de error uniformes (ej. "Categoría").
     */
    public String getNombreRecurso() {
        return nombreRecurso;
    }

    /**
     * Método Plantilla (Template Method) para la creación de un nuevo registro.
     * Flujo fijo: preparar request -> validar alta -> mapear a entidad -> hook pre-persistencia
     * -> persistir -> hook post-creación -> mapear a DTO de respuesta.
     */
    @Transactional
    public Res crear(Req request) {
        Req requestPreparado = prepararRequest(request);
        validarCrear(requestPreparado);
        E entidad = aEntidad(requestPreparado);
        entidad.setEliminado(false);
        antesDePersistir(entidad, requestPreparado);
        E guardada = getRepository().save(entidad);
        despuesDeCrear(guardada, requestPreparado);
        return aResponse(guardada);
    }

    /**
     * Método Plantilla (Template Method) para la modificación de un registro existente.
     * Flujo fijo: obtener entidad activa (404 si no existe/eliminada) -> preparar request
     * -> validar modificación -> actualizar estado de la entidad -> hook pre-persistencia
     * -> persistir -> hook post-modificación -> mapear a DTO de respuesta.
     */
    @Transactional
    public Res modificar(String id, Req request) {
        E existente = obtenerEntidad(id);
        Req requestPreparado = prepararRequest(request);
        validarModificar(id, requestPreparado, existente);
        actualizarEntidad(requestPreparado, existente);
        antesDePersistir(existente, requestPreparado);
        E guardada = getRepository().save(existente);
        despuesDeModificar(guardada, requestPreparado);
        return aResponse(guardada);
    }

    /**
     * Método Plantilla (Template Method) para el borrado lógico de un registro.
     * Flujo fijo: obtener entidad activa (404 si no existe/eliminada) -> validar eliminación
     * -> hook pre-eliminación -> marcar eliminado=true y persistir -> hook post-eliminación.
     */
    @Transactional
    public void eliminar(String id) {
        E existente = obtenerEntidad(id);
        validarEliminar(existente);
        antesDeEliminar(existente);
        existente.setEliminado(true);
        getRepository().save(existente);
        despuesDeEliminar(existente);
    }

    /**
     * Método Plantilla (Template Method) para buscar un registro activo por ID y devolver su DTO.
     */
    @Transactional(readOnly = true)
    public Res buscar(String id) {
        E entidad = obtenerEntidad(id);
        return aResponse(entidad);
    }

    /**
     * Método Plantilla (Template Method) para listar todos los registros activos proyectados a DTO.
     */
    @Transactional(readOnly = true)
    public List<Res> listar() {
        List<E> entidades = buscarEntidadesActivas();
        return getMapper().aResponseList(entidades);
    }

    /**
     * Acceso interno de dominio para que otros servicios obtengan la entidad activa sin pasar por DTO
     * (según regla 2 de la sección 10.1 de GENERAL.md).
     * Lanza 404 (NO_ENCONTRADO) si no existe o si fue dada de baja lógicamente.
     */
    @Transactional(readOnly = true)
    public E obtenerEntidad(String id) {
        return getRepository().findByIdAndEliminadoFalse(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        getNombreRecurso() + " no encontrada con id: " + id));
    }

    // =========================================================================
    // Pasos personalizables (Hooks / Operaciones primitivas del Template Method)
    // =========================================================================

    /**
     * Permite normalizar o limpiar los datos de entrada (por ejemplo, quitar espacios sobrantes)
     * antes de ejecutar las validaciones y el mapeo.
     */
    protected Req prepararRequest(Req request) {
        return request;
    }

    /**
     * Validaciones de reglas de negocio específicas para el alta (ej. unicidad).
     */
    protected void validarCrear(Req request) {
        // Hook vacío por defecto para sobreescribir en subclases
    }

    /**
     * Validaciones de reglas de negocio específicas para la modificación (ej. unicidad excluyendo el propio ID).
     */
    protected void validarModificar(String id, Req request, E existente) {
        // Hook vacío por defecto para sobreescribir en subclases
    }

    /**
     * Validaciones de reglas de negocio previas a la baja lógica.
     */
    protected void validarEliminar(E existente) {
        // Hook vacío por defecto para sobreescribir en subclases
    }

    /**
     * Convierte el DTO de entrada en una nueva instancia de la entidad.
     */
    protected E aEntidad(Req request) {
        return getMapper().aEntidad(request);
    }

    /**
     * Actualiza los campos de una entidad existente a partir del DTO de entrada.
     */
    protected void actualizarEntidad(Req request, E existente) {
        getMapper().actualizarEntidad(request, existente);
    }

    /**
     * Convierte una entidad persistida en su DTO de respuesta.
     */
    protected Res aResponse(E entidad) {
        return getMapper().aResponse(entidad);
    }

    /**
     * Recupera las entidades no eliminadas para el listado general.
     * Las subclases pueden sobreescribirlo para aplicar un ordenamiento específico.
     */
    protected List<E> buscarEntidadesActivas() {
        return getRepository().findAllByEliminadoFalse();
    }

    /**
     * Hook invocado inmediatamente antes de guardar la entidad (en alta o modificación),
     * útil para resolver referencias a otras entidades mediante sus respectivos servicios.
     */
    protected void antesDePersistir(E entidad, Req request) {
        // Hook vacío por defecto
    }

    /**
     * Hook de pos-procesamiento ejecutado tras persistir una nueva entidad.
     */
    protected void despuesDeCrear(E entidad, Req request) {
        // Hook vacío por defecto
    }

    /**
     * Hook de pos-procesamiento ejecutado tras persistir la modificación de una entidad.
     */
    protected void despuesDeModificar(E entidad, Req request) {
        // Hook vacío por defecto
    }

    /**
     * Hook ejecutado antes de aplicar la marca de borrado lógico.
     */
    protected void antesDeEliminar(E entidad) {
        // Hook vacío por defecto
    }

    /**
     * Hook de pos-procesamiento ejecutado después del borrado lógico.
     */
    protected void despuesDeEliminar(E entidad) {
        // Hook vacío por defecto
    }
}

