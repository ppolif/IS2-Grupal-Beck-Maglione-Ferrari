package com.DulceMayor.backend.config;

import com.DulceMayor.backend.dtos.ErrorResponse;
import com.DulceMayor.backend.entities.Usuario;
import com.DulceMayor.backend.services.UsuarioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filtro de seguridad sin estado (stateless) que valida el token JWT en cada petición,
 * consulta el estado actual del usuario en base de datos y aplica la regla de bloqueo
 * por clave temporal (403 CAMBIO_CLAVE_REQUERIDO).
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";
    public static final String CODIGO_CAMBIO_CLAVE_REQUERIDO = "CAMBIO_CLAVE_REQUERIDO";

    private final JwtProvider jwtProvider;
    private final UsuarioService usuarioService;
    private final ObjectMapper objectMapper;

    public JwtAuthenticationFilter(JwtProvider jwtProvider, UsuarioService usuarioService) {
        this.jwtProvider = jwtProvider;
        this.usuarioService = usuarioService;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(BEARER_PREFIX.length()).trim();
        if (token.isEmpty()) {
            escribirError(
                    response,
                    HttpStatus.UNAUTHORIZED,
                    "NO_AUTENTICADO",
                    "Token de autenticación vacío",
                    request.getRequestURI()
            );
            return;
        }

        final Claims claims;
        try {
            claims = jwtProvider.validarYObtenerClaims(token);
        } catch (JwtException | IllegalArgumentException ex) {
            escribirError(
                    response,
                    HttpStatus.UNAUTHORIZED,
                    "NO_AUTENTICADO",
                    "Token inválido o vencido",
                    request.getRequestURI()
            );
            return;
        }

        String usuarioId = claims.getSubject();
        if (usuarioId == null || usuarioId.isBlank()) {
            escribirError(
                    response,
                    HttpStatus.UNAUTHORIZED,
                    "NO_AUTENTICADO",
                    "Token sin identificador de usuario",
                    request.getRequestURI()
            );
            return;
        }

        Usuario usuarioOpt = usuarioService.buscarUsuario(usuarioId);
        if (usuarioOpt == null) {
            escribirError(
                    response,
                    HttpStatus.UNAUTHORIZED,
                    "NO_AUTENTICADO",
                    "El usuario asociado al token no existe o se encuentra inactivo",
                    request.getRequestURI()
            );
            return;
        }

        Usuario usuario = usuarioOpt;

        if (usuario.isClaveTemporal() && !esRutaPermitidaConClaveTemporal(request)) {
            escribirError(
                    response,
                    HttpStatus.FORBIDDEN,
                    CODIGO_CAMBIO_CLAVE_REQUERIDO,
                    "Debe modificar su clave temporal antes de utilizar el sistema",
                    request.getRequestURI()
            );
            return;
        }

        UsuarioPrincipal principal = new UsuarioPrincipal(usuario);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        principal.getAuthorities()
                );
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }

    private boolean esRutaPermitidaConClaveTemporal(HttpServletRequest request) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isEmpty() && uri.startsWith(contextPath)) {
            uri = uri.substring(contextPath.length());
        }

        boolean esCambiarClave = "POST".equalsIgnoreCase(method)
                && ("/auth/cambiar-clave".equals(uri) || "/api/v1/auth/cambiar-clave".equals(uri));
        boolean esPerfil = "GET".equalsIgnoreCase(method)
                && ("/auth/perfil".equals(uri) || "/api/v1/auth/perfil".equals(uri));

        return esCambiarClave || esPerfil;
    }

    public void escribirError(
            HttpServletResponse response,
            HttpStatus status,
            String code,
            String message,
            String path) throws IOException {
        ErrorResponse body = ErrorResponse.of(status.value(), code, message, path);
        response.setStatus(status.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}

