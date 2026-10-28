package com.DulceMayor.backend.controllers;

import com.DulceMayor.backend.services.BaseService;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * Clase base abstracta para los controladores REST de recursos CRUD.
 *
 * <p><b>Patrón GoF aplicado: Template Method (Comportamiento)</b></p>
 * <ul>
 *   <li><b>Participante GoF:</b> AbstractClass (Clase Base Abstracta).</li>
 *   <li><b>Métodos Plantilla (Template Methods):</b>
 *     <ul>
 *       <li>{@link #ejecutarCrear(Object)}: define el flujo de creación HTTP (invoca servicio, construye URI y retorna {@code 201 Created}).</li>
 *       <li>{@link #ejecutarModificar(String, Object)}: define el flujo de actualización HTTP (invoca servicio y retorna {@code 200 OK}).</li>
 *       <li>{@link #ejecutarEliminar(String)}: define el flujo de baja lógica HTTP (invoca servicio y retorna {@code 204 No Content}).</li>
 *       <li>{@link #ejecutarBuscar(String)}: define la búsqueda por ID y retorna {@code 200 OK}.</li>
 *       <li>{@link #ejecutarListar()}: define la consulta de colección completa y retorna {@code 200 OK}.</li>
 *     </ul>
 *   </li>
 *   <li><b>Operaciones primitivas abstractas (Primitive Operations):</b>
 *     <ul>
 *       <li>{@link #getService()}: hook abstracto donde cada controlador concreto provee su servicio correspondiente.</li>
 *       <li>{@link #extraerId(Object)}: hook abstracto para extraer el identificador del DTO de respuesta y generar la cabecera {@code Location}.</li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * <p><b>Patrón GoF relacionado: DTO / Adapter</b></p>
 * Los controladores nunca exponen entidades JPA; operan exclusivamente con contratos DTO ({@code Req} y {@code Res}).
 *
 * @param <Req> Tipo del DTO de entrada (Request).
 * @param <Res> Tipo del DTO de salida (Response).
 */
public abstract class BaseController<Req, Res> {

    /**
     * Devuelve el servicio de negocio asociado al recurso.
     */
    protected abstract BaseService<?, Req, Res> getService();

    /**
     * Extrae el identificador único del DTO de respuesta creado para construir la cabecera HTTP Location.
     */
    protected abstract String extraerId(Res response);

    /**
     * Método plantilla protegido para el listado de recursos activos ({@code 200 OK}).
     */
    protected ResponseEntity<List<Res>> ejecutarListar() {
        return ResponseEntity.ok(getService().listar());
    }

    /**
     * Método plantilla protegido para la búsqueda de un recurso por ID ({@code 200 OK}).
     */
    protected ResponseEntity<Res> ejecutarBuscar(String id) {
        return ResponseEntity.ok(getService().buscar(id));
    }

    /**
     * Método plantilla protegido para la creación de un recurso ({@code 201 Created} + header {@code Location}).
     */
    protected ResponseEntity<Res> ejecutarCrear(Req request) {
        Res creado = getService().crear(request);
        URI location = construirLocation(extraerId(creado));
        return ResponseEntity.created(location).body(creado);
    }

    /**
     * Método plantilla protegido para la modificación de un recurso existente ({@code 200 OK}).
     */
    protected ResponseEntity<Res> ejecutarModificar(String id, Req request) {
        return ResponseEntity.ok(getService().modificar(id, request));
    }

    /**
     * Método plantilla protegido para el borrado lógico de un recurso ({@code 204 No Content}).
     */
    protected ResponseEntity<Void> ejecutarEliminar(String id) {
        getService().eliminar(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Construye la URI absoluta del recurso recién creado a partir de la petición HTTP actual.
     */
    protected URI construirLocation(String id) {
        return ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();
    }
}

