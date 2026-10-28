package com.example.persona.controllers;

import com.example.persona.dtos.BaseDto;
import com.example.persona.services.BaseServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

public abstract class BaseControllerImpl<D extends BaseDto, S extends BaseServiceImpl<?, D, Long>> implements BaseController<D, Long> {

    @Autowired
    protected S servicio;

    @FunctionalInterface
    public interface ControllerAction<R> {
        R execute() throws Exception;
    }

    // ============================================================
    // GENERIC TEMPLATE METHOD: executeTemplate
    // ============================================================
    protected <R> ResponseEntity<?> executeTemplate(
            HttpStatus successStatus,
            HttpStatus errorStatus,
            ControllerAction<R> action,
            String operationName) {
        try {
            preExecute(operationName);
            R result = action.execute();
            postExecute(operationName, result);
            return buildSuccessResponse(successStatus, result);
        } catch (Exception e) {
            return handleException(operationName, e, errorStatus);
        }
    }

    protected void preExecute(String operationName) throws Exception {
        // Hook
    }

    protected void postExecute(String operationName, Object result) throws Exception {
        // Hook
    }

    // ============================================================
    // TEMPLATE METHOD: getAll()
    // ============================================================
    @GetMapping("")
    public ResponseEntity<?> getAll() {
        return executeTemplate(
                HttpStatus.OK,
                HttpStatus.NOT_FOUND,
                () -> {
                    preGetAll();
                    var list = servicio.findAll();
                    postGetAll(list);
                    return list;
                },
                "getAll"
        );
    }

    protected void preGetAll() throws Exception {
        // Hook
    }

    protected void postGetAll(Object result) throws Exception {
        // Hook
    }

    // ============================================================
    // TEMPLATE METHOD: getAll(Pageable)
    // ============================================================
    @GetMapping("/paged")
    public ResponseEntity<?> getAll(Pageable pageable) {
        return executeTemplate(
                HttpStatus.OK,
                HttpStatus.NOT_FOUND,
                () -> {
                    preGetAllPaged(pageable);
                    var page = servicio.findAll(pageable);
                    postGetAllPaged(page);
                    return page;
                },
                "getAllPaged"
        );
    }

    protected void preGetAllPaged(Pageable pageable) throws Exception {
        // Hook
    }

    protected void postGetAllPaged(Object result) throws Exception {
        // Hook
    }

    // ============================================================
    // TEMPLATE METHOD: getOne(Long)
    // ============================================================
    @GetMapping("/{id}")
    public ResponseEntity<?> getOne(@PathVariable Long id) {
        return executeTemplate(
                HttpStatus.OK,
                HttpStatus.NOT_FOUND,
                () -> {
                    validateId(id);
                    preGetOne(id);
                    D dto = servicio.findById(id);
                    postGetOne(dto);
                    return dto;
                },
                "getOne"
        );
    }

    protected void validateId(Long id) throws Exception {
        if (id == null || id <= 0) {
            throw new Exception("El id proporcionado no es válido.");
        }
    }

    protected void preGetOne(Long id) throws Exception {
        // Hook
    }

    protected void postGetOne(D result) throws Exception {
        // Hook
    }

    // ============================================================
    // TEMPLATE METHOD: save(D)
    // ============================================================
    @PostMapping("")
    public ResponseEntity<?> save(@RequestBody D dto) {
        return executeTemplate(
                getCreateStatus(),
                HttpStatus.BAD_REQUEST,
                () -> {
                    validateDtoForSave(dto);
                    preSave(dto);
                    D result = servicio.save(dto);
                    postSave(result);
                    return result;
                },
                "save"
        );
    }

    protected void validateDtoForSave(D dto) throws Exception {
        if (dto == null) {
            throw new Exception("El cuerpo de la petición no puede ser nulo.");
        }
    }

    protected void preSave(D dto) throws Exception {
        // Hook
    }

    protected void postSave(D result) throws Exception {
        // Hook
    }

    protected HttpStatus getCreateStatus() {
        return HttpStatus.OK;
    }

    // ============================================================
    // TEMPLATE METHOD: update(Long, D)
    // ============================================================
    @PostMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody D dto) {
        return executeTemplate(
                HttpStatus.OK,
                HttpStatus.BAD_REQUEST,
                () -> {
                    validateId(id);
                    validateDtoForUpdate(id, dto);
                    preUpdate(id, dto);
                    D result = servicio.update(id, dto);
                    postUpdate(result);
                    return result;
                },
                "update"
        );
    }

    protected void validateDtoForUpdate(Long id, D dto) throws Exception {
        if (dto == null) {
            throw new Exception("El cuerpo de la petición no puede ser nulo.");
        }
    }

    protected void preUpdate(Long id, D dto) throws Exception {
        // Hook
    }

    protected void postUpdate(D result) throws Exception {
        // Hook
    }

    // ============================================================
    // TEMPLATE METHOD: delete(Long)
    // ============================================================
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        return executeTemplate(
                getDeleteStatus(),
                HttpStatus.BAD_REQUEST,
                () -> {
                    validateId(id);
                    preDelete(id);
                    boolean deleted = servicio.delete(id);
                    postDelete(id);
                    return deleted;
                },
                "delete"
        );
    }

    protected void preDelete(Long id) throws Exception {
        // Hook
    }

    protected void postDelete(Long id) throws Exception {
        // Hook
    }

    protected HttpStatus getDeleteStatus() {
        return HttpStatus.NO_CONTENT;
    }

    // ============================================================
    // RESPONSE & ERROR HANDLING HOOKS
    // ============================================================
    protected ResponseEntity<?> buildSuccessResponse(HttpStatus status, Object body) {
        return ResponseEntity.status(status).body(body);
    }

    protected ResponseEntity<?> handleException(String operation, Exception e, HttpStatus defaultStatus) {
        String message = e.getMessage() != null ? e.getMessage() : "Error. Por favor intente mas tarde.";
        message = message.replace("\"", "\\\"");
        return ResponseEntity.status(defaultStatus).body("{\"error\":\"" + message + "\"}");
    }
}
