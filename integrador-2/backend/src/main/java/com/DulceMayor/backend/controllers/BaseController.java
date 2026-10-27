package com.DulceMayor.backend.controllers;

import com.DulceMayor.backend.services.BaseService;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * Clase base abstracta para los controladores REST de recursos CRUD.
 *
 * <p><b>Patrón aplicado: Template Method (Método Plantilla) en la capa de Controlador.</b></p>
 * <p><b>Por qué se aplica acá:</b> Estandariza la construcción de respuestas HTTP y códigos de
 * estado según el contrato OpenAPI ({@code 201 Created} con cabecera {@code Location},
 * {@code 200 OK} con cuerpo JSON y {@code 204 No Content} sin cuerpo) delegando la lógica de
 * negocio en {@link BaseService}. Los endpoints HTTP ({@code @GetMapping}, {@code @PostMapping},
 * {@code @PutMapping}, {@code @DeleteMapping}) y las reglas de autorización ({@code @PreAuthorize})
 * NO se declaran en esta clase base sino explícitamente en cada controlador concreto, para que la
 * matriz de permisos por rol ({@code x-roles}) sea auditable y específica en cada endpoint.</p>
 *
 * <p><b>Patrón aplicado: DTO (Data Transfer Object).</b></p>
 * <p><b>Por qué se aplica acá:</b> Ningún método de la capa controladora recibe ni devuelve
 * entidades de dominio; opera exclusivamente con los tipos {@code Req} y {@code Res}.</p>
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

