package com.colmena.videojuegos.controllers;

import com.colmena.videojuegos.entities.Base;
import com.colmena.videojuegos.services.ServicioBaseImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

public abstract class BaseControllerImpl<E extends Base, S extends ServicioBaseImpl<E, Long>> implements BaseController<E, Long> {

    @Autowired
    protected S servicio;

    /**
     * Template Method para listar todos los recursos vía REST.
     */
    @GetMapping("")
    @Override
    public ResponseEntity<?> getAll() {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(servicio.findAll());
        } catch (Exception e) {
            return buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    /**
     * Template Method para obtener un recurso individual por ID.
     */
    @GetMapping("/{id}")
    @Override
    public ResponseEntity<?> getOne(@PathVariable("id") Long id) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(servicio.findById(id));
        } catch (Exception e) {
            return buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    /**
     * Template Method para crear un nuevo recurso.
     */
    @PostMapping("")
    @Override
    public ResponseEntity<?> save(@RequestBody E entity) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(servicio.saveOne(entity));
        } catch (Exception e) {
            return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    /**
     * Template Method para actualizar un recurso existente.
     */
    @PutMapping("/{id}")
    @Override
    public ResponseEntity<?> update(@PathVariable("id") Long id, @RequestBody E entity) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(servicio.updateOne(entity, id));
        } catch (Exception e) {
            return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    /**
     * Template Method para eliminar / desactivar un recurso.
     */
    @DeleteMapping("/{id}")
    @Override
    public ResponseEntity<?> delete(@PathVariable("id") Long id) {
        try {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(servicio.deleteById(id));
        } catch (Exception e) {
            return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    /**
     * Paso común para formatear respuestas JSON de error estandarizadas.
     */
    protected ResponseEntity<?> buildErrorResponse(HttpStatus status, String message) {
        String msg = message != null ? message.replace("\"", "\\\"") : "Error procesando solicitud";
        return ResponseEntity.status(status).body("{\"error\":\"" + msg + "\"}");
    }
}
