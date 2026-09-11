package com.example.tinder.controladores;

import com.example.tinder.entidades.Usuario;
import com.example.tinder.enumeraciones.Sexo;
import com.example.tinder.enumeraciones.Tipo;
import com.example.tinder.errores.ErrorServicio;
import com.example.tinder.servicios.MascotaServicio;
import com.example.tinder.servicios.UsuarioServicio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class MascotaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MascotaServicio mascotaServicio;

    @MockBean
    private UsuarioServicio usuarioServicio;

    private MockHttpSession sessionFalsa;
    private Usuario usuarioLogueado;
    private MockMultipartFile archivoFoto;

    @BeforeEach
    void setup() {
        usuarioLogueado = new Usuario();
        usuarioLogueado.setId("ID-USUARIO-123");
        usuarioLogueado.setNombre("Dueño Test");

        sessionFalsa = new MockHttpSession();
        sessionFalsa.setAttribute("usuariosession", usuarioLogueado);

        archivoFoto = new MockMultipartFile(
                "archivo", "foto.jpg", "image/jpeg", "imagen".getBytes());
    }

    // --- TEST 1: LISTAR MASCOTAS ---
    @Test
    @WithMockUser(roles = "USUARIO_REGISTRADO")
    void misMascotas_UsuarioLogueado_RetornaVistaMascotas() throws Exception {

        // Simulamos que el servicio devuelve una lista vacía de mascotas
        when(mascotaServicio.buscarMascotasPorUsuario("ID-USUARIO-123")).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/mascota/mis-mascotas").session(sessionFalsa))
                .andExpect(status().isOk())
                .andExpect(view().name("mascotas")) // Espera que el return sea "mascotas"[cite: 3]
                .andExpect(model().attributeExists("mascotas"));
    }

    // --- TEST 2: CREAR MASCOTA EXITOSAMENTE ---
    @Test
    @WithMockUser(roles = "USUARIO_REGISTRADO")
    void actualizar_DatosValidosSinId_CreaMascotaYRedirige() throws Exception {

        // El id viene vacío, por lo que el controlador llama a agregarMascota[cite: 3]
        doNothing().when(mascotaServicio).agregarMascota(
                any(), anyString(), anyString(), any(Sexo.class), any(Tipo.class));

        // NOTA: Reemplaza "MACHO" y "PERRO" por los nombres exactos de los valores de tus Enum
        mockMvc.perform(multipart("/mascota/actualizar-perfil")
                        .file(archivoFoto)
                        .param("id", "") // ID vacío indica creación[cite: 3]
                        .param("nombre", "Firulais")
                        .param("sexo", "MACHO")
                        .param("tipo", "PERRO")
                        .session(sessionFalsa))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/inicio"));
    }

    // --- TEST 3: ERROR AL CREAR MASCOTA ---
    @Test
    @WithMockUser(roles = "USUARIO_REGISTRADO")
    void actualizar_ErrorEnServicio_RetornaFormularioConError() throws Exception {

        // Forzamos un fallo en agregarMascota
        doThrow(new ErrorServicio("El nombre no puede estar vacío")).when(mascotaServicio).agregarMascota(
                any(), anyString(), anyString(), any(Sexo.class), any(Tipo.class));

        mockMvc.perform(multipart("/mascota/actualizar-perfil")
                        .file(archivoFoto)
                        .param("id", "")
                        .param("nombre", "") // Enviamos nombre vacío
                        .param("sexo", "MACHO")
                        .param("tipo", "PERRO")
                        .session(sessionFalsa))
                .andExpect(status().isOk()) // Atrapa el error y se queda en la vista[cite: 3]
                .andExpect(view().name("mascota.html")) // Verifica que vuelva a mascotas.html[cite: 3]
                .andExpect(model().attributeExists("error")) // Inyecta el error[cite: 3]
                .andExpect(model().attributeExists("sexos")) // Inyecta la lista de sexos para el select[cite: 3]
                .andExpect(model().attributeExists("tipos")); // Inyecta la lista de tipos para el select[cite: 3]
    }
}