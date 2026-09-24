package com.example.zero.services;

import com.example.zero.entidades.persona.Usuario;
import com.example.zero.enums.RolUsuario;
import com.example.zero.repositories.UsuarioRepository;
import com.example.zero.services.mail.EmailService;
import com.example.zero.services.persona.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceVerificacionTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private EmailService emailService;

    private UsuarioService usuarioService;

    @BeforeEach
    void setUp() {
        usuarioService = new UsuarioService(usuarioRepository, emailService);
    }

    @Test
    void generarYAsignarCodigo_asignaCodigoDe6DigitosYExpiracion() {
        Usuario usuario = Usuario.builder()
                .nombreUsuario("cliente@test.com")
                .activo(false)
                .build();

        when(usuarioRepository.findByNombreUsuarioAndEliminadoFalse("cliente@test.com"))
                .thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));

        String codigo = usuarioService.generarYAsignarCodigo("cliente@test.com");

        assertNotNull(codigo);
        assertEquals(6, codigo.length());
        assertTrue(codigo.matches("\\d{6}"));
        assertEquals(codigo, usuario.getCodigoConfirmacion());
        assertNotNull(usuario.getCodigoExpiracion());
        assertTrue(usuario.getCodigoExpiracion().isAfter(LocalDateTime.now()));
        verify(usuarioRepository, times(1)).save(usuario);
    }

    @Test
    void generarYAsignarCodigo_conUsuarioActivo_lanzaExcepcion() {
        Usuario usuario = Usuario.builder()
                .nombreUsuario("activo@test.com")
                .activo(true)
                .build();

        when(usuarioRepository.findByNombreUsuarioAndEliminadoFalse("activo@test.com"))
                .thenReturn(Optional.of(usuario));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> usuarioService.generarYAsignarCodigo("activo@test.com"));

        assertTrue(ex.getMessage().contains("La cuenta ya se encuentra activa"));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void verificarCodigo_codigoCorrecto_activaUsuarioYLimpiaCodigo() {
        Usuario usuario = Usuario.builder()
                .nombreUsuario("cliente@test.com")
                .activo(false)
                .codigoConfirmacion("654321")
                .codigoExpiracion(LocalDateTime.now().plusMinutes(10))
                .build();

        when(usuarioRepository.findByNombreUsuarioAndEliminadoFalse("cliente@test.com"))
                .thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));

        Usuario verificado = usuarioService.verificarCodigo("cliente@test.com", "654321");

        assertTrue(verificado.isActivo());
        assertNull(verificado.getCodigoConfirmacion());
        assertNull(verificado.getCodigoExpiracion());
        verify(usuarioRepository, times(1)).save(usuario);
    }

    @Test
    void verificarCodigo_codigoIncorrecto_lanzaExcepcion() {
        Usuario usuario = Usuario.builder()
                .nombreUsuario("cliente@test.com")
                .activo(false)
                .codigoConfirmacion("654321")
                .codigoExpiracion(LocalDateTime.now().plusMinutes(10))
                .build();

        when(usuarioRepository.findByNombreUsuarioAndEliminadoFalse("cliente@test.com"))
                .thenReturn(Optional.of(usuario));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> usuarioService.verificarCodigo("cliente@test.com", "111111"));

        assertTrue(ex.getMessage().contains("El código de confirmación ingresado es incorrecto"));
        assertFalse(usuario.isActivo());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void verificarCodigo_codigoExpirado_lanzaExcepcion() {
        Usuario usuario = Usuario.builder()
                .nombreUsuario("cliente@test.com")
                .activo(false)
                .codigoConfirmacion("654321")
                .codigoExpiracion(LocalDateTime.now().minusMinutes(1)) // expirado
                .build();

        when(usuarioRepository.findByNombreUsuarioAndEliminadoFalse("cliente@test.com"))
                .thenReturn(Optional.of(usuario));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> usuarioService.verificarCodigo("cliente@test.com", "654321"));

        assertTrue(ex.getMessage().contains("El código de confirmación ha expirado"));
        assertFalse(usuario.isActivo());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void autenticar_usuarioInactivo_lanzaExcepcion() {
        Usuario usuario = Usuario.builder()
                .nombreUsuario("inactivo@test.com")
                .clave("pass1234")
                .activo(false)
                .build();

        when(usuarioRepository.findByNombreUsuarioAndEliminadoFalse("inactivo@test.com"))
                .thenReturn(Optional.of(usuario));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> usuarioService.autenticar("inactivo@test.com", "pass1234"));

        assertTrue(ex.getMessage().contains("Tu cuenta no está activa"));
    }

    @Test
    void autenticar_usuarioActivo_autenticaCorrectamente() {
        Usuario usuario = Usuario.builder()
                .nombreUsuario("activo@test.com")
                .clave("pass1234")
                .activo(true)
                .build();

        when(usuarioRepository.findByNombreUsuarioAndEliminadoFalse("activo@test.com"))
                .thenReturn(Optional.of(usuario));

        Usuario autenticado = usuarioService.autenticar("activo@test.com", "pass1234");

        assertNotNull(autenticado);
        assertEquals("activo@test.com", autenticado.getNombreUsuario());
    }

    @Test
    void reenviarCodigoConfirmacion_generaYEnviaCorreo() {
        Usuario usuario = Usuario.builder()
                .nombreUsuario("reenvio@test.com")
                .activo(false)
                .build();

        when(usuarioRepository.findByNombreUsuarioAndEliminadoFalse("reenvio@test.com"))
                .thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));

        usuarioService.reenviarCodigoConfirmacion("reenvio@test.com");

        verify(emailService, times(1)).enviarCodigoConfirmacion(eq("reenvio@test.com"), anyString());
    }
}

