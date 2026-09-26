package com.example.zero.services;

import com.example.zero.entidades.Imagen;
import com.example.zero.enums.TipoImagen;
import com.example.zero.repositories.ImagenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImagenServiceTest {

    @Mock
    private ImagenRepository imagenRepository;

    private ImagenService imagenService;

    @BeforeEach
    void setUp() {
        imagenService = new ImagenService(imagenRepository);
    }

    @Test
    void guardarImagen_conArchivoValido_persisteImagen() {
        MockMultipartFile file = new MockMultipartFile(
                "foto",
                "perfil.jpg",
                "image/jpeg",
                new byte[]{1, 2, 3, 4}
        );

        when(imagenRepository.save(any(Imagen.class))).thenAnswer(i -> {
            Imagen img = i.getArgument(0);
            img.setId("img-123");
            return img;
        });

        Imagen guardada = imagenService.guardarImagen(file, TipoImagen.PERSONA);

        assertNotNull(guardada);
        assertEquals("img-123", guardada.getId());
        assertEquals("perfil.jpg", guardada.getNombre());
        assertEquals("image/jpeg", guardada.getMime());
        assertEquals(TipoImagen.PERSONA, guardada.getTipoImagen());
        assertFalse(guardada.isEliminado());
        verify(imagenRepository).save(any(Imagen.class));
    }

    @Test
    void guardarImagen_conArchivoNulo_lanzaExcepcion() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                imagenService.guardarImagen(null, TipoImagen.PERSONA));
        assertTrue(ex.getMessage().contains("El archivo de imagen no puede estar vacío"));
    }

    @Test
    void guardarImagen_conArchivoVacio_lanzaExcepcion() {
        MockMultipartFile emptyFile = new MockMultipartFile("foto", "empty.jpg", "image/jpeg", new byte[0]);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                imagenService.guardarImagen(emptyFile, TipoImagen.PRODUCTO));
        assertTrue(ex.getMessage().contains("El archivo de imagen no puede estar vacío"));
    }

    @Test
    void guardarMonigoteDefault_guardaTipoPersona() {
        when(imagenRepository.save(any(Imagen.class))).thenAnswer(i -> {
            Imagen img = i.getArgument(0);
            img.setId("avatar-default");
            return img;
        });

        Imagen monigote = imagenService.guardarMonigoteDefault();

        assertNotNull(monigote);
        assertEquals("avatar-default", monigote.getId());
        assertEquals(TipoImagen.PERSONA, monigote.getTipoImagen());
        assertNotNull(monigote.getContenido());
        assertTrue(monigote.getContenido().length > 0);
        verify(imagenRepository).save(any(Imagen.class));
    }

    @Test
    void buscarPorId_existente_retornaImagen() {
        Imagen img = Imagen.builder().id("img-1").nombre("test.png").eliminado(false).build();
        when(imagenRepository.findActive("img-1")).thenReturn(Optional.of(img));

        Imagen resultado = imagenService.buscarPorId("img-1");

        assertNotNull(resultado);
        assertEquals("img-1", resultado.getId());
    }

    @Test
    void buscarPorId_inexistente_lanzaExcepcion() {
        when(imagenRepository.findActive("no-existe")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                imagenService.buscarPorId("no-existe"));
        assertTrue(ex.getMessage().contains("No se encontró la imagen"));
    }

    @Test
    void eliminarImagen_existente_marcaEliminado() {
        Imagen img = Imagen.builder().id("img-1").eliminado(false).build();
        when(imagenRepository.findActive("img-1")).thenReturn(Optional.of(img));

        imagenService.eliminarImagen("img-1");

        assertTrue(img.isEliminado());
        verify(imagenRepository).save(img);
    }
}
