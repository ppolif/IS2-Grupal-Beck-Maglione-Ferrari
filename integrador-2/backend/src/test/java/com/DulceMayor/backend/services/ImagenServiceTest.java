package com.DulceMayor.backend.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.DulceMayor.backend.config.RecursoNoEncontradoException;
import com.DulceMayor.backend.config.ValidacionException;
import com.DulceMayor.backend.dtos.ImagenContenido;
import com.DulceMayor.backend.dtos.ImagenResponse;
import com.DulceMayor.backend.entities.Imagen;
import com.DulceMayor.backend.entities.TipoImagen;
import com.DulceMayor.backend.repositories.ImagenRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

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
    @DisplayName("subirImagen: persiste con eliminado=false y devuelve ImagenResponse")
    void subirImagenExitoso() {
        MockMultipartFile file = new MockMultipartFile(
                "archivo",
                "foto.png",
                "image/png",
                new byte[]{1, 2, 3, 4}
        );

        when(imagenRepository.save(any(Imagen.class))).thenAnswer(invocation -> {
            Imagen img = invocation.getArgument(0);
            img.setId("img-1");
            return img;
        });

        ImagenResponse res = imagenService.subirImagen(file, TipoImagen.PRODUCTO);

        assertNotNull(res);
        assertEquals("img-1", res.id());
        assertEquals("foto.png", res.nombre());
        assertEquals("image/png", res.mime());
        assertEquals(TipoImagen.PRODUCTO, res.tipoImagen());
    }

    @Test
    @DisplayName("subirImagen: rechaza tipos MIME no admitidos con ValidacionException")
    void subirImagenMimeInvalido() {
        MockMultipartFile file = new MockMultipartFile(
                "archivo",
                "doc.pdf",
                "application/pdf",
                new byte[]{1, 2}
        );

        assertThrows(ValidacionException.class, () -> imagenService.subirImagen(file, TipoImagen.PRODUCTO));
    }

    @Test
    @DisplayName("obtenerContenido: devuelve bytes y MIME si existe")
    void obtenerContenidoExitoso() {
        Imagen img = Imagen.builder()
                .id("img-1")
                .nombre("logo.jpg")
                .mime("image/jpeg")
                .contenido(new byte[]{10, 20})
                .tipoImagen(TipoImagen.LOGO_EMPRESA)
                .eliminado(false)
                .build();

        when(imagenRepository.findByIdAndEliminadoFalse("img-1")).thenReturn(Optional.of(img));

        ImagenContenido res = imagenService.obtenerContenido("img-1");
        assertNotNull(res);
        assertEquals("image/jpeg", res.mime());
        assertEquals(2, res.contenido().length);
    }

    @Test
    @DisplayName("eliminar: aplica borrado lógico")
    void eliminarBorradoLogico() {
        Imagen img = Imagen.builder()
                .id("img-1")
                .eliminado(false)
                .build();

        when(imagenRepository.findByIdAndEliminadoFalse("img-1")).thenReturn(Optional.of(img));

        imagenService.eliminar("img-1");

        assertTrue(img.isEliminado());
        verify(imagenRepository).save(img);
    }
}

