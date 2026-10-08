package com.example.tinder.errores;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice(basePackages = "com.example.tinder.controladores.rest")
public class GlobalRestExceptionHandler {

    @ExceptionHandler(ErrorServicio.class)
    public ResponseEntity<Map<String, Object>> handleServicioException(ErrorServicio ex, HttpServletRequest request) {
        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", new Date());
        error.put("status", HttpStatus.BAD_REQUEST.value());
        error.put("error", "Error de Servicio");
        error.put("message", ex.getMessage());
        error.put("path", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, Object> error = new HashMap<>();
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fe.getField(), fe.getDefaultMessage());
        }
        error.put("timestamp", new Date());
        error.put("status", HttpStatus.BAD_REQUEST.value());
        error.put("error", "Validación fallida");
        error.put("message", fieldErrors.toString());
        error.put("fieldErrors", fieldErrors);
        error.put("path", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", new Date());
        error.put("status", HttpStatus.BAD_REQUEST.value());
        error.put("error", "Argumento inválido");
        error.put("message", ex.getMessage());
        error.put("path", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralException(Exception ex, HttpServletRequest request) {
        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", new Date());
        error.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        error.put("error", "Error Interno");
        error.put("message", ex.getMessage());
        error.put("path", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}

