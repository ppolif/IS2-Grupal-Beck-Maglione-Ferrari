package com.example.zero.config;

import com.example.zero.entidades.persona.Usuario;
import com.example.zero.enums.RolUsuario;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminAuthInterceptorTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    @InjectMocks
    private AdminAuthInterceptor interceptor;

    @Test
    void preHandle_sinSesion_redirigeLoginYRetornaFalse() throws Exception {
        when(request.getSession(false)).thenReturn(null);

        boolean result = interceptor.preHandle(request, response, new Object());

        assertFalse(result);
        verify(response, times(1)).sendRedirect("/admin/login");
    }

    @Test
    void preHandle_sesionSinUsuario_redirigeLoginYRetornaFalse() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("usuariosession")).thenReturn(null);

        boolean result = interceptor.preHandle(request, response, new Object());

        assertFalse(result);
        verify(response, times(1)).sendRedirect("/admin/login");
    }

    @Test
    void preHandle_usuarioCliente_redirigeInicioYRetornaFalse() throws Exception {
        Usuario cliente = Usuario.builder().nombreUsuario("cliente@test.com").rol(RolUsuario.CLIENTE).build();
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("usuariosession")).thenReturn(cliente);

        boolean result = interceptor.preHandle(request, response, new Object());

        assertFalse(result);
        verify(response, times(1)).sendRedirect("/?error=unauthorized");
    }

    @Test
    void preHandle_usuarioAdministrativo_retornaTrue() throws Exception {
        Usuario admin = Usuario.builder().nombreUsuario("admin@test.com").rol(RolUsuario.ADMINISTRATIVO).build();
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("usuariosession")).thenReturn(admin);

        boolean result = interceptor.preHandle(request, response, new Object());

        assertTrue(result);
        verify(response, never()).sendRedirect(anyString());
    }

    @Test
    void preHandle_usuarioJefe_retornaTrue() throws Exception {
        Usuario jefe = Usuario.builder().nombreUsuario("jefe@test.com").rol(RolUsuario.JEFE).build();
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("usuariosession")).thenReturn(jefe);

        boolean result = interceptor.preHandle(request, response, new Object());

        assertTrue(result);
        verify(response, never()).sendRedirect(anyString());
    }
}

