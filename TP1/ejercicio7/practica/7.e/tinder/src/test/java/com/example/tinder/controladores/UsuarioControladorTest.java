package com.example.tinder.controladores;

import com.example.tinder.errores.ErrorServicio;
import com.example.tinder.servicios.UsuarioServicio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UsuarioControladorTest {

    @Autowired
    private MockMvc mockMvc;

    // Falsificamos el servicio para no guardar datos reales en la BD durante esta prueba web
    @MockBean
    private UsuarioServicio usuarioServicio;

    // --- TEST 1: CARGAR EL FORMULARIO DE REGISTRO ---
    @Test
    void registrar_Get_RetornaVistaDeRegistro() throws Exception {
        // Asumiendo que tu ruta de registro es /registro
        mockMvc.perform(get("/registro"))
                .andExpect(status().isOk())
                .andExpect(view().name("registro.html")); // Revisa que coincida con el nombre de tu archivo HTML
    }

    // --- TEST 2: REGISTRO EXITOSO (CAMINO FELIZ) ---
    @Test
    void    registrar_PostValido_MuestraMensajeDeExito() throws Exception {
        // 1. Creamos un archivo falso en memoria
        MockMultipartFile archivoFoto = new MockMultipartFile(
                "archivo", "foto.jpg", "image/jpeg", "imagen_falsa".getBytes());

        // 2. Simulamos que el servicio no arroja ninguna excepción
        doNothing().when(usuarioServicio).registrar(
                any(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString());

        // 3. Ejecutamos la petición enviando todos los datos del formulario
        mockMvc.perform(multipart("/registrar") // Asumiendo que el action de tu form es /registrar
                        .file(archivoFoto)
                        .param("nombre", "Augusto")
                        .param("apellido", "Perez")
                        .param("email", "augusto@correo.com")
                        .param("clave", "123456")
                        .param("repetirClave", "123456")
                        .param("idZona", "ID-ZONA-FALSA"))
                .andExpect(status().isOk())
                .andExpect(view().name("exito.html"));
    }

    // --- TEST 3: REGISTRO CON ERROR (CAMINO TRISTE) ---
    @Test
    void registrar_FaltanDatos_RecargaFormularioConError() throws Exception {
        MockMultipartFile archivoFoto = new MockMultipartFile(
                "archivo", "foto.jpg", "image/jpeg", "imagen_falsa".getBytes());

        // 1. Agregamos un anyString() adicional al final para representar a idZona (7 parámetros en total)
        doThrow(new ErrorServicio("El nombre de usuario no puede ser nulo")).when(usuarioServicio)
                .registrar(any(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString());

        // 2. Ejecutamos la petición enviando el parámetro extra
        mockMvc.perform(multipart("/registrar")
                        .file(archivoFoto)
                        .param("nombre", "") // Nombre vacío a propósito
                        .param("apellido", "Perez")
                        .param("email", "augusto@correo.com")
                        .param("clave", "123456")
                        .param("repetirClave", "123456")
                        .param("idZona", "ID-ZONA-FALSA"))
                .andExpect(status().isOk())
                .andExpect(view().name("registro.html"));
    }
}