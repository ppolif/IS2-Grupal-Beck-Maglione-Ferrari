package com.DulceMayor.backend.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.DulceMayor.backend.config.JwtProvider;
import com.DulceMayor.backend.entities.Imagen;
import com.DulceMayor.backend.entities.ProveedorAuth;
import com.DulceMayor.backend.entities.RolUsuario;
import com.DulceMayor.backend.entities.TipoImagen;
import com.DulceMayor.backend.entities.Usuario;
import com.DulceMayor.backend.repositories.ImagenRepository;
import com.DulceMayor.backend.repositories.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ImagenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ImagenRepository imagenRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtProvider jwtProvider;

    private String tokenJefe;

    @BeforeEach
    void setUp() {
        imagenRepository.deleteAll();
        usuarioRepository.deleteAll();

        Usuario jefe = usuarioRepository.save(Usuario.builder()
                .nombreUsuario("jefe_img")
                .clave(passwordEncoder.encode("clave12345"))
                .rol(RolUsuario.JEFE)
                .claveTemporal(false)
                .proveedorAuth(ProveedorAuth.LOCAL)
                .eliminado(false)
                .build());
        tokenJefe = jwtProvider.generarToken(jefe);
    }

    @Test
    @DisplayName("POST /imagenes sube archivo multipart y retorna 201 Created con cabecera Location")
    void subirImagenExitoso() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "archivo",
                "producto.png",
                "image/png",
                new byte[]{1, 2, 3, 4}
        );

        mockMvc.perform(multipart("/imagenes")
                        .file(file)
                        .param("tipoImagen", "PRODUCTO")
                        .header("Authorization", "Bearer " + tokenJefe))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.nombre").value("producto.png"))
                .andExpect(jsonPath("$.tipoImagen").value("PRODUCTO"));
    }

    @Test
    @DisplayName("GET /imagenes/{id}/contenido es público y descarga los bytes con su MIME")
    void descargarContenidoPublico() throws Exception {
        Imagen img = imagenRepository.save(Imagen.builder()
                .nombre("foto.jpg")
                .mime("image/jpeg")
                .contenido(new byte[]{10, 20, 30})
                .tipoImagen(TipoImagen.SUCURSAL)
                .eliminado(false)
                .build());

        mockMvc.perform(get("/imagenes/{id}/contenido", img.getId()))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "image/jpeg"))
                .andExpect(content().bytes(new byte[]{10, 20, 30}));
    }

    @Test
    @DisplayName("DELETE /imagenes/{id} con rol JEFE marca borrado lógico (204 No Content)")
    void eliminarImagen() throws Exception {
        Imagen img = imagenRepository.save(Imagen.builder()
                .nombre("temp.png")
                .mime("image/png")
                .contenido(new byte[]{1})
                .tipoImagen(TipoImagen.PRODUCTO)
                .eliminado(false)
                .build());

        mockMvc.perform(delete("/imagenes/{id}", img.getId())
                        .header("Authorization", "Bearer " + tokenJefe))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/imagenes/{id}/contenido", img.getId()))
                .andExpect(status().isNotFound());
    }
}
