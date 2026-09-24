package com.example.zero.controllers;

import com.example.zero.entidades.persona.Cliente;
import com.example.zero.entidades.persona.Usuario;
import com.example.zero.enums.RolUsuario;
import com.example.zero.repositories.ClienteRepository;
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
class ProfileControllerTest {

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private ProfileController profileController;

    private MockHttpSession session;
    private Usuario usuarioTest;
    private Cliente clienteTest;

    @BeforeEach
    void setUp() {
        session = new MockHttpSession();

        clienteTest = Cliente.builder()
                .numeroDocumento("35123456")
                .nombre("Martin")
                .apellido("Gomez")
                .build();

        usuarioTest = Usuario.builder()
                .id("user-100")
                .nombreUsuario("martin@zero.com")
                .rol(RolUsuario.CLIENTE)
                .persona(clienteTest)
                .foto("https://ejemplo.com/avatar.jpg")
                .build();
    }

    @Test
    @DisplayName("verPerfil sin sesión redirige a /login")
    void verPerfil_sinSesion_redirigeALogin() {
        Model model = new ConcurrentModel();
        RedirectAttributes ra = new RedirectAttributesModelMap();

        String view = profileController.verPerfil(session, model, ra);

        assertEquals("redirect:/login", view);
        assertTrue(ra.getFlashAttributes().containsKey("errorMessage"));
    }

    @Test
    @DisplayName("verPerfil con sesión de cliente retorna vista shop/profile con datos")
    void verPerfil_conSesion_retornaVistaShopProfile() {
        session.setAttribute("usuariosession", usuarioTest);
        when(usuarioRepository.findById("user-100")).thenReturn(Optional.of(usuarioTest));

        Model model = new ConcurrentModel();
        RedirectAttributes ra = new RedirectAttributesModelMap();

        String view = profileController.verPerfil(session, model, ra);

        assertEquals("shop/profile", view);
        assertEquals(usuarioTest, model.getAttribute("usuario"));
        assertEquals(clienteTest, model.getAttribute("cliente"));
    }

    @Test
    @DisplayName("actualizarPerfil exitoso actualiza usuario en servicio y sesión")
    void actualizarPerfil_exitoso_actualizaYRedirige() {
        session.setAttribute("usuariosession", usuarioTest);
        Usuario usuarioActualizado = Usuario.builder()
                .id("user-100")
                .nombreUsuario("nuevo@zero.com")
                .foto("https://ejemplo.com/nueva.jpg")
                .rol(RolUsuario.CLIENTE)
                .build();

        when(usuarioService.actualizarPerfil("user-100", "nuevo@zero.com", "https://ejemplo.com/nueva.jpg"))
                .thenReturn(usuarioActualizado);

        RedirectAttributes ra = new RedirectAttributesModelMap();

        String view = profileController.actualizarPerfil(session, "nuevo@zero.com", "https://ejemplo.com/nueva.jpg", ra);

        assertEquals("redirect:/profile", view);
        assertTrue(ra.getFlashAttributes().containsKey("successMessage"));
        assertEquals(usuarioActualizado, session.getAttribute("usuariosession"));
    }

    @Test
    @DisplayName("actualizarPerfil sin sesión redirige a /login")
    void actualizarPerfil_sinSesion_redirigeALogin() {
        RedirectAttributes ra = new RedirectAttributesModelMap();

        String view = profileController.actualizarPerfil(session, "nuevo@zero.com", null, ra);

        assertEquals("redirect:/login", view);
        assertTrue(ra.getFlashAttributes().containsKey("errorMessage"));
    }
}
