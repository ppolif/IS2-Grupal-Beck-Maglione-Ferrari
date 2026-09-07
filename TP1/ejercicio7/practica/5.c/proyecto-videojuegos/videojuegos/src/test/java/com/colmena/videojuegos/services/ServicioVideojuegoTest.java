package com.colmena.videojuegos.services;

import com.colmena.videojuegos.dtos.VideojuegoRequestDTO;
import com.colmena.videojuegos.dtos.VideojuegoResponseDTO;
import com.colmena.videojuegos.entities.Videojuego;
import com.colmena.videojuegos.repositories.RepositorioVideojuego;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ServicioVideojuegoTest {

    @InjectMocks
    private ServicioVideojuego servicioVideojuego;

    @Mock
    private RepositorioVideojuego repositorioVideojuego;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    //tests buscar por nombre
    @Test
    public void  testBuscarPorTituloDTO() throws Exception {

        List<Videojuego> juegos = new ArrayList<>();
        Videojuego juego = new Videojuego();
        juego.setId(1L);
        juego.setTitulo("Mario 64");

       juegos.add(juego);

        when(repositorioVideojuego.findByTitle("Mario 64")).thenReturn(juegos);

        List<VideojuegoResponseDTO> resultado = servicioVideojuego.buscarPorTituloDTO("Mario 64");

        verify(repositorioVideojuego, times(1)).findByTitle("Mario 64");
    }

    @Test
    public void testBuscarPorTituloDTOexcepcion() {
        // Simulamos una búsqueda vacía en la base de datos[cite: 2]
        when(repositorioVideojuego.findByTitle("Inexistente")).thenReturn(null);

        // Validamos que se lance la excepción correcta al no encontrar el juego
        Exception exception = assertThrows(Exception.class, () -> {
            servicioVideojuego.buscarPorTituloDTO("Inexistente");
        });

        assertNotNull(exception);
    }

    //test para guardar
    @Test
    public void testGuardarDesdeDTO() throws Exception {
        VideojuegoRequestDTO dto = new VideojuegoRequestDTO();
        dto.setId(1L);

        when(repositorioVideojuego.save(any(Videojuego.class))).thenReturn(new Videojuego());

        servicioVideojuego.guardarDesdeDTO(dto);

        verify(repositorioVideojuego, times(1)).save(any(Videojuego.class));
    }
}

