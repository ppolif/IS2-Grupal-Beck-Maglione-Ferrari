package com.example.tinder.service;

import com.example.tinder.dto.UsuarioEdicionDTO;
import com.example.tinder.dto.UsuarioRegistroDTO;
import com.example.tinder.entidades.Foto;
import com.example.tinder.entidades.Usuario;
import com.example.tinder.entidades.Zona;
import com.example.tinder.errores.ErrorServicio;
import com.example.tinder.repositorios.UsuarioRepositorio;
import com.example.tinder.repositorios.ZonaRepositorio;
import com.example.tinder.servicios.FotoServicio;
import com.example.tinder.servicios.UsuarioServicio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class UsuarioServicioTest {

    @InjectMocks
    private UsuarioServicio usuarioServicio;

    @Mock
    private UsuarioRepositorio usuarioRepositorio;

    @Mock
    private ZonaRepositorio zonaRepositorio;

    @Mock
    private FotoServicio fotoServicio;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testRegistrar() throws ErrorServicio {
        UsuarioRegistroDTO dto = new UsuarioRegistroDTO();
        dto.setNombre("leandro");
        dto.setApellido("spadaro");
        dto.setEmail("leandro@mail.com");
        dto.setClave("1234567");
        dto.setRepetirClave("1234567");
        dto.setIdZona("1");

        Zona zonaMock = new Zona();
        when(zonaRepositorio.getOne("1")).thenReturn(zonaMock);
        when(fotoServicio.guardar(any())).thenReturn(new Foto());

        usuarioServicio.registrar(dto);

        verify(zonaRepositorio, times(1)).getOne("1");
        verify(usuarioRepositorio, times(1)).save(any(Usuario.class));
    }

    @Test
    public void testModificar() throws ErrorServicio {
        UsuarioEdicionDTO dto = new UsuarioEdicionDTO();
        dto.setId("1");
        dto.setNombre("leandro2");
        dto.setApellido("spadaro");
        dto.setMail("leandro@mail.com");
        dto.setClave1("1234567");
        dto.setClave2("1234567");
        dto.setIdZona("1");

        Zona zonaMock = new Zona();
        Usuario original = new Usuario();
        original.setId("1");

        when(zonaRepositorio.getOne("1")).thenReturn(zonaMock);
        when(usuarioRepositorio.findById("1")).thenReturn(Optional.of(original));
        when(fotoServicio.actualizar(any(), any())).thenReturn(new Foto());

        usuarioServicio.modificar(dto);

        verify(usuarioRepositorio, times(1)).findById("1");
        verify(usuarioRepositorio, times(1)).save(original);
    }

    @Test
    public void testModificarInexistente() {
        UsuarioEdicionDTO dto = new UsuarioEdicionDTO();
        dto.setId("123");
        dto.setNombre("leandro");
        dto.setApellido("spadaro");
        dto.setMail("leandro@mail.com");
        dto.setClave1("1234567");
        dto.setClave2("1234567");
        dto.setIdZona("1");

        when(zonaRepositorio.getOne("1")).thenReturn(new Zona());
        when(usuarioRepositorio.findById("123")).thenReturn(Optional.empty());

        ErrorServicio exception = assertThrows(ErrorServicio.class, () -> {
            usuarioServicio.modificar(dto);
        });

        assertEquals("No se encontró el usuario", exception.getMessage());
    }


}
