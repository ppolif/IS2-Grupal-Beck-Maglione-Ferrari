package com.colmena.videojuegos.controllers;

import com.colmena.videojuegos.dtos.VideojuegoRequestDTO;
import com.colmena.videojuegos.services.ServicioCategoria;
import com.colmena.videojuegos.services.ServicioEstudio;
import com.colmena.videojuegos.services.ServicioVideojuego;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class ControladorVideojuegoTest {

    @InjectMocks
    private controladorVideojuego controlador;

    @Mock
    private ServicioVideojuego servicioVideojuego;

    @Mock
    private ServicioCategoria servicioCategoria;

    @Mock
    private ServicioEstudio servicioEstudio;

    @Mock
    private Model model;

    @Mock
    private BindingResult result;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // test de guardado exitoso
    @Test
    public void testGuardarVideojuego() throws Exception {
        VideojuegoRequestDTO dto = new VideojuegoRequestDTO();
        Long idPrueba = 1L;

        // asumimos que el formulario no tiene errores
        when(result.hasErrors()).thenReturn(false);

        String vistaDestino = controlador.guardarVideojuego(dto, result, model, idPrueba);

        verify(servicioVideojuego, times(1)).guardarDesdeDTO(dto);

        assertEquals("redirect:/crud", vistaDestino);
    }


    // test de error en el formulario
    @Test
    public void testGuardarVideojuegoException() throws Exception {
        VideojuegoRequestDTO dto = new VideojuegoRequestDTO();
        Long idPrueba = 1L;

        // asumimos que el formulario tiene errores
        when(result.hasErrors()).thenReturn(true);

        when(servicioCategoria.listarDTO()).thenReturn(new ArrayList<>());
        when(servicioEstudio.listarDTO()).thenReturn(new ArrayList<>());

        String vistaDestino = controlador.guardarVideojuego(dto, result, model, idPrueba);

        // podemos verificar que guardar no haya ejecutado
        verify(servicioVideojuego, never()).guardarDesdeDTO(any());

        assertEquals("views/formulario/videojuego", vistaDestino);
    }
    
}
