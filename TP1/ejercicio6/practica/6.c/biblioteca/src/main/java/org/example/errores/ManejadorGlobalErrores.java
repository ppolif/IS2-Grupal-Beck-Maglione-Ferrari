package org.example.errores;

import jakarta.servlet.http.HttpServletRequest;
import org.example.dtos.ErrorResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@ControllerAdvice
public class ManejadorGlobalErrores {

    // 1. Manejo de errores de validación de Bean Validation (@Valid) REST
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Object manejarValidaciones(MethodArgumentNotValidException ex, HttpServletRequest request) throws MethodArgumentNotValidException {

        // Si la petición viene de un formulario web normal, dejamos que el framework haga su magia
        // (En ControladorVistas capturamos el BindingResult antes de llegar acá)
        if (esPeticionHtml(request)) {
            throw ex; // Lo relanzamos para que Spring lo resuelva (o podríamos retornar una vista de error)
        }

        // Si es una llamada REST, agrupamos los campos fallidos en un Map
        Map<String, String> errores = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String campo = ((FieldError) error).getField();
            String mensaje = error.getDefaultMessage();
            errores.put(campo, mensaje);
        });

        ErrorResponseDTO errorRespuesta = ErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Error")
                .message(errores.toString()) // Serializamos los campos afectados de forma segura
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(errorRespuesta, HttpStatus.BAD_REQUEST);
    }

    // 2. Manejo de Acceso Denegado (403 Forbidden)
    @ExceptionHandler(AccessDeniedException.class)
    public Object manejarAccesoDenegado(AccessDeniedException ex, HttpServletRequest request, Model model) {

        if (esPeticionHtml(request)) {
            // Redirige al inicio indicando un error (como configuraste en inicio.html)
            return "redirect:/inicio?error=true";
        }

        // Formato para llamadas API
        ErrorResponseDTO errorRespuesta = ErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.FORBIDDEN.value())
                .error("Forbidden")
                .message("No tienes permisos suficientes para realizar esta acción.")
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(errorRespuesta, HttpStatus.FORBIDDEN);
    }

    // 3. Manejo de Errores de Negocio Custom (ej: "No hay stock", "Usuario ya existe")
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public Object manejarErroresDeNegocio(RuntimeException ex, HttpServletRequest request, Model model) {

        if (esPeticionHtml(request)) {
            model.addAttribute("errorGrave", ex.getMessage());
            return "errores/error-generico";
        }

        ErrorResponseDTO errorRespuesta = ErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value()) // 409 Conflict suele ser un buen código
                .error("Business Rule Violation")
                .message(ex.getMessage()) // Mensaje sanitizado desde la capa de servicio
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(errorRespuesta, HttpStatus.CONFLICT);
    }

    @ExceptionHandler({NoResourceFoundException.class, NoHandlerFoundException.class})
    public Object manejarRutaNoEncontrada(Exception ex, HttpServletRequest request) {

        if (esPeticionHtml(request)) {
            // Retorna la vista 404.html que creaste
            return "error/404";
        }

        // Formato JSON para llamadas a la API REST que fallen por ruta inexistente
        ErrorResponseDTO errorRespuesta = ErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Not Found")
                .message("La ruta o recurso solicitado no existe.")
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(errorRespuesta, HttpStatus.NOT_FOUND);
    }

    // 4. Catch-All para Errores Internos no Previstos (500 Internal Server Error)
    @ExceptionHandler(Exception.class)
    public Object manejarErrorInterno(Exception ex, HttpServletRequest request, Model model) {

        // Generamos un ID de traza para buscar el stacktrace real en la consola/logs
        String traceId = UUID.randomUUID().toString();
        // LOGGEAR EL ERROR REAL AQUÍ: log.error("Trace ID: {} - Error: ", traceId, ex);
        System.err.println("Trace ID: " + traceId + " - Error Crítico: " + ex.getMessage());

        String mensajeSeguro = "Ha ocurrido un error interno en el servidor. ID de referencia: " + traceId;

        if (esPeticionHtml(request)) {
            model.addAttribute("traceId", traceId);
            return "error/500";
        }

        ErrorResponseDTO errorRespuesta = ErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message(mensajeSeguro) // ¡Nunca exponemos el `ex.getMessage()` o StackTrace crudo aquí!
                .path(request.getRequestURI())
                .traceId(traceId)
                .build();

        return new ResponseEntity<>(errorRespuesta, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // --- Utilitarios ---

    // Evalúa si el cliente solicitó HTML o si es una llamada REST (JSON)
    private boolean esPeticionHtml(HttpServletRequest request) {
        String acceptHeader = request.getHeader("Accept");
        return acceptHeader != null && acceptHeader.contains("text/html");
    }
}