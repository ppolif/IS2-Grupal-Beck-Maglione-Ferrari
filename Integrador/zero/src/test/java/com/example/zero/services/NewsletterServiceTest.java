package com.example.zero.services;

import com.example.zero.entidades.persona.Usuario;
import com.example.zero.entidades.producto.Producto;
import com.example.zero.enums.RolUsuario;
import com.example.zero.repositories.ProductoRepository;
import com.example.zero.repositories.UsuarioRepository;
import com.example.zero.services.mail.NewsletterService;
import com.example.zero.services.producto.ProductoService;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NewsletterServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private ProductoService productoService;

    @InjectMocks
    private NewsletterService newsletterService;

    private Producto productoEnOferta;
    private Usuario usuarioRegistrado;

    @BeforeEach
    void setUp() {
        productoEnOferta = Producto.builder()
                .id("prod-oferta-1")
                .nombre("Zapatilla Hyper Speed")
                .precioActual(120.0)
                .enOferta(true)
                .eliminado(false)
                .descripcion("Calzado en promoción de alto impacto")
                .build();

        usuarioRegistrado = Usuario.builder()
                .id("user-1")
                .nombreUsuario("cliente1@zero.com")
                .rol(RolUsuario.CLIENTE)
                .eliminado(false)
                .build();
    }

    @Test
    @DisplayName("enviarNewsletterOfertas no despacha correos si no existen productos en oferta")
    void enviarNewsletterOfertas_sinOfertas_noEnviaMails() {
        when(productoRepository.findByEnOfertaTrueAndEliminadoFalse()).thenReturn(Collections.emptyList());

        int enviados = newsletterService.enviarNewsletterOfertas();

        assertEquals(0, enviados);
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("enviarNewsletterOfertas despacha mails a todos los usuarios con email válido")
    void enviarNewsletterOfertas_conOfertasYUsuarios_despachaMails() {
        when(productoRepository.findByEnOfertaTrueAndEliminadoFalse()).thenReturn(List.of(productoEnOferta));
        when(usuarioRepository.findAll()).thenReturn(List.of(usuarioRegistrado));
        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));

        int enviados = newsletterService.enviarNewsletterOfertas();

        assertEquals(1, enviados);
        verify(productoService, times(1)).prepararParaVista(List.of(productoEnOferta));
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("construirHtmlNewsletter genera el HTML con el nombre y precio del producto")
    void construirHtmlNewsletter_generaContenidoValido() {
        String html = newsletterService.construirHtmlNewsletter(List.of(productoEnOferta));

        assertNotNull(html);
        assertTrue(html.contains("Zapatilla Hyper Speed"));
        assertTrue(html.contains("$120.00"));
        assertTrue(html.contains("ZERO"));
    }
}

