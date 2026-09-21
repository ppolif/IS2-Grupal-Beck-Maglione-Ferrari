package com.example.zero.services;

import com.example.zero.entidades.persona.Usuario;
import com.example.zero.enums.RolUsuario;
import com.example.zero.repositories.UsuarioRepository;
import com.example.zero.services.persona.UsuarioService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    void autenticar_conCredencialesCorrectas_retornaUsuario() {
        Usuario usuario = Usuario.builder()
                .id("u-1")
                .nombreUsuario("admin@zero.com")
                .clave("admin123")
                .rol(RolUsuario.ADMINISTRATIVO)
                .eliminado(false)
                .build();

        when(usuarioRepository.findByNombreUsuarioAndEliminadoFalse("admin@zero.com"))
                .thenReturn(Optional.of(usuario));

        Usuario resultado = usuarioService.autenticar("admin@zero.com", "admin123");

        assertNotNull(resultado);
        assertEquals("admin@zero.com", resultado.getNombreUsuario());
        assertEquals(RolUsuario.ADMINISTRATIVO, resultado.getRol());
    }

    @Test
    void autenticar_conUsuarioInexistente_lanzaIllegalArgumentException() {
        when(usuarioRepository.findByNombreUsuarioAndEliminadoFalse("fantasma@zero.com"))
                .thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> usuarioService.autenticar("fantasma@zero.com", "clave123"));

        assertTrue(ex.getMessage().contains("Usuario no encontrado"));
    }

    @Test
    void autenticar_conClaveIncorrecta_lanzaIllegalArgumentException() {
        Usuario usuario = Usuario.builder()
                .id("u-1")
                .nombreUsuario("admin@zero.com")
                .clave("correcta")
                .rol(RolUsuario.ADMINISTRATIVO)
                .eliminado(false)
                .build();

        when(usuarioRepository.findByNombreUsuarioAndEliminadoFalse("admin@zero.com"))
                .thenReturn(Optional.of(usuario));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> usuarioService.autenticar("admin@zero.com", "incorrecta"));

        assertTrue(ex.getMessage().contains("Contraseña incorrecta"));
    }

    @Test
    void autenticar_conParametrosVacios_lanzaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> usuarioService.autenticar("", "1234"));
        assertThrows(IllegalArgumentException.class, () -> usuarioService.autenticar("admin@zero.com", ""));
    }

    @Test
    void crearUsuario_conDatosValidos_guardaYRetornaUsuario() {
        when(usuarioRepository.findByNombreUsuarioAndEliminadoFalse("nuevo@zero.com"))
                .thenReturn(Optional.empty());
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> {
            Usuario u = inv.getArgument(0);
            u.setId("u-123");
            return u;
        });

        Usuario resultado = usuarioService.crearUsuario("nuevo@zero.com", "segura123", RolUsuario.CLIENTE, null);

        assertNotNull(resultado);
        assertEquals("u-123", resultado.getId());
        assertEquals("nuevo@zero.com", resultado.getNombreUsuario());
        assertEquals("segura123", resultado.getClave());
        assertFalse(resultado.isEliminado());
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    void crearUsuario_conNombreUsuarioDuplicado_lanzaIllegalArgumentException() {
        Usuario existente = Usuario.builder().id("u-1").nombreUsuario("existente@zero.com").build();
        when(usuarioRepository.findByNombreUsuarioAndEliminadoFalse("existente@zero.com"))
                .thenReturn(Optional.of(existente));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> usuarioService.crearUsuario("existente@zero.com", "clave123", RolUsuario.CLIENTE, null));

        assertTrue(ex.getMessage().contains("Ya existe un usuario activo"));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void crearUsuario_conClaveCorta_lanzaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> usuarioService.crearUsuario("test@zero.com", "123", RolUsuario.CLIENTE, null));
    }

    @Test
    void crearUsuario_conRolNulo_lanzaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> usuarioService.crearUsuario("test@zero.com", "clave123", null, null));
    }

    @Test
    void modificarUsuario_conDatosValidos_actualizaYRetorna() {
        Usuario usuario = Usuario.builder().id("u-1").clave("vieja").rol(RolUsuario.CLIENTE).eliminado(false).build();
        when(usuarioRepository.findActive("u-1")).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        Usuario actualizado = usuarioService.modificarUsuario("u-1", "nueva123", RolUsuario.JEFE);

        assertEquals("nueva123", actualizado.getClave());
        assertEquals(RolUsuario.JEFE, actualizado.getRol());
        verify(usuarioRepository, times(1)).save(usuario);
    }

    @Test
    void eliminarUsuario_existente_marcaEliminado() {
        Usuario usuario = Usuario.builder().id("u-1").eliminado(false).build();
        when(usuarioRepository.findActive("u-1")).thenReturn(Optional.of(usuario));

        usuarioService.eliminarUsuario("u-1");

        assertTrue(usuario.isEliminado());
        verify(usuarioRepository, times(1)).save(usuario);
    }

    @Test
    void buscarPorId_existente_retornaUsuario() {
        Usuario usuario = Usuario.builder().id("u-1").nombreUsuario("juan").build();
        when(usuarioRepository.findActive("u-1")).thenReturn(Optional.of(usuario));

        Usuario encontrado = usuarioService.buscarPorId("u-1");

        assertEquals("juan", encontrado.getNombreUsuario());
    }

    @Test
    void buscarPorId_inexistente_lanzaIllegalArgumentException() {
        when(usuarioRepository.findActive("u-inexistente")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> usuarioService.buscarPorId("u-inexistente"));
    }

    @Test
    void buscarPorNombreUsuario_existente_retornaUsuario() {
        Usuario usuario = Usuario.builder().id("u-1").nombreUsuario("ana@zero.com").build();
        when(usuarioRepository.findByNombreUsuarioAndEliminadoFalse("ana@zero.com"))
                .thenReturn(Optional.of(usuario));

        Usuario encontrado = usuarioService.buscarPorNombreUsuario("ana@zero.com");

        assertEquals("ana@zero.com", encontrado.getNombreUsuario());
    }

    @Test
    void listarActivos_retornaLista() {
        when(usuarioRepository.findByEliminadoFalse()).thenReturn(List.of(
                Usuario.builder().id("u1").build(),
                Usuario.builder().id("u2").build()
        ));

        List<Usuario> activos = usuarioService.listarActivos();

        assertEquals(2, activos.size());
    }
}

