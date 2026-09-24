package com.example.zero.controllers;

import com.example.zero.entidades.persona.Empleado;
import com.example.zero.entidades.persona.Usuario;
import com.example.zero.enums.RolUsuario;
import com.example.zero.repositories.EmpleadoRepository;
import com.example.zero.repositories.UsuarioRepository;
import com.example.zero.services.persona.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminProfileControllerTest {

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private EmpleadoRepository empleadoRepository;

    @InjectMocks
    private AdminProfileController adminProfileController;

    private MockHttpSession session;
    private Usuario usuarioAdmin;
    private Empleado empleadoTest;

    @BeforeEach
    void setUp() {
        session = new MockHttpSession();

        empleadoTest = Empleado.builder()
                .numeroDocumento("20112233")
                .nombre("Carlos")
                .apellido("Lopez")
                .build();

        usuarioAdmin = Usuario.builder()
                .id("admin-50")
                .nombreUsuario("admin@zero.com")
                .rol(RolUsuario.ADMINISTRATIVO)
                .persona(empleadoTest)
                .foto("https://ejemplo.com/admin.jpg")
                .build();
    }

    @Test
    @DisplayName("verPerfilAdmin sin sesión redirige a /login")
    void verPerfilAdmin_sinSesion_redirigeALogin() {
        Model model = new ConcurrentModel();
        RedirectAttributes ra = new RedirectAttributesModelMap();

        String view = adminProfileController.verPerfilAdmin(session, model, ra);

        assertEquals("redirect:/login", view);
        assertTrue(ra.getFlashAttributes().containsKey("errorMessage"));
    }

    @Test
    @DisplayName("verPerfilAdmin con rol CLIENTE redirige a /profile")
    void verPerfilAdmin_rolCliente_redirigeAProfile() {
        Usuario cliente = Usuario.builder().id("c1").rol(RolUsuario.CLIENTE).build();
        session.setAttribute("usuariosession", cliente);

        Model model = new ConcurrentModel();
        RedirectAttributes ra = new RedirectAttributesModelMap();

        String view = adminProfileController.verPerfilAdmin(session, model, ra);

        assertEquals("redirect:/profile", view);
    }

    @Test
    @DisplayName("verPerfilAdmin con rol ADMINISTRATIVO retorna admin/profile")
    void verPerfilAdmin_rolAdmin_retornaAdminProfile() {
        session.setAttribute("usuariosession", usuarioAdmin);
        when(usuarioRepository.findById("admin-50")).thenReturn(Optional.of(usuarioAdmin));

        Model model = new ConcurrentModel();
        RedirectAttributes ra = new RedirectAttributesModelMap();

        String view = adminProfileController.verPerfilAdmin(session, model, ra);

        assertEquals("admin/profile", view);
        assertEquals(usuarioAdmin, model.getAttribute("usuario"));
        assertEquals(empleadoTest, model.getAttribute("empleado"));
    }

    @Test
    @DisplayName("actualizarPerfilAdmin exitoso actualiza perfil y recarga sesión")
    void actualizarPerfilAdmin_exitoso_actualizaYRedirige() {
        session.setAttribute("usuariosession", usuarioAdmin);
        Usuario adminActualizado = Usuario.builder()
                .id("admin-50")
                .nombreUsuario("carlos.admin@zero.com")
                .foto("https://ejemplo.com/nueva_foto.jpg")
                .rol(RolUsuario.ADMINISTRATIVO)
                .build();

        when(usuarioService.actualizarPerfil("admin-50", "carlos.admin@zero.com", "https://ejemplo.com/nueva_foto.jpg"))
                .thenReturn(adminActualizado);

        RedirectAttributes ra = new RedirectAttributesModelMap();

        String view = adminProfileController.actualizarPerfilAdmin(session, "carlos.admin@zero.com", "https://ejemplo.com/nueva_foto.jpg", ra);

        assertEquals("redirect:/admin/profile", view);
        assertTrue(ra.getFlashAttributes().containsKey("successMessage"));
        assertEquals(adminActualizado, session.getAttribute("usuariosession"));
    }
}
