package com.example.zero.config;

import com.example.zero.entidades.persona.Usuario;
import com.example.zero.enums.RolUsuario;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor para verificar la autenticación y permisos de acceso a las rutas del panel administrativo (/admin/**).
 */
@Component
public class AdminAuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        Usuario usuario = (session != null) ? (Usuario) session.getAttribute("usuariosession") : null;

        if (usuario == null) {
            response.sendRedirect("/admin/login");
            return false;
        }

        if (usuario.getRol() != RolUsuario.ADMINISTRATIVO && usuario.getRol() != RolUsuario.JEFE) {
            response.sendRedirect("/?error=unauthorized");
            return false;
        }

        return true;
    }
}

