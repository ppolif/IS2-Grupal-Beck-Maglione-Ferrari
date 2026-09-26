package com.example.zero.controllers;

import com.example.zero.entidades.Imagen;
import com.example.zero.services.ImagenService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImagenControllerTest {

    @Mock
    private ImagenService imagenService;

    @InjectMocks
    private ImagenController imagenController;

    @Test
    void obtenerImagen_existente_retornaBytesYContentType() {
        byte[] contenido = new byte[]{10, 20, 30};
        Imagen img = Imagen.builder()
                .id("img-1")
                .nombre("foto.png")
                .mime("image/png")
                .contenido(contenido)
                .eliminado(false)
                .build();

        when(imagenService.buscarPorId("img-1")).thenReturn(img);

        ResponseEntity<byte[]> response = imagenController.obtenerImagen("img-1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.IMAGE_PNG, response.getHeaders().getContentType());
        assertArrayEquals(contenido, response.getBody());
    }

    @Test
    void obtenerImagen_inexistente_retornaNotFound() {
        when(imagenService.buscarPorId("no-existe")).thenThrow(new IllegalArgumentException("No se encontró la imagen"));

        ResponseEntity<byte[]> response = imagenController.obtenerImagen("no-existe");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void obtenerImagen_contenidoNulo_retornaNotFound() {
        Imagen img = Imagen.builder().id("img-1").contenido(null).build();
        when(imagenService.buscarPorId("img-1")).thenReturn(img);

        ResponseEntity<byte[]> response = imagenController.obtenerImagen("img-1");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
