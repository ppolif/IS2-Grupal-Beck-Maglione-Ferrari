package com.example.tinder.controladores;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Levanta el contexto completo de la aplicación (BD, Seguridad, MVC)
@SpringBootTest
// Configura la herramienta para simular peticiones HTTP
@AutoConfigureMockMvc
class PortalControladorTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void index_RutaPublica_RetornaVistaIndex() throws Exception {
        // Simulamos una petición GET a la raíz del sitio
        mockMvc.perform(get("/"))
                .andExpect(status().isOk()) // Esperamos un código HTTP 200 (OK)
                .andExpect(view().name("index.html")); // Esperamos que devuelva la vista index.html
    }

    @Test
    void inicio_UsuarioNoAutenticado_RedirigeAlLogin() throws Exception {
        // Un usuario anónimo intenta entrar a /inicio
        mockMvc.perform(get("/inicio"))
                .andExpect(status().is3xxRedirection()) // Esperamos que Spring Security bloquee y redirija (302)
                .andExpect(redirectedUrlPattern("**/login")); // Esperamos que lo mande a la página de login
    }

    @Test
    // Simulamos un usuario logueado con el rol correcto
    @WithMockUser(roles = "USUARIO_REGISTRADO")
    void inicio_UsuarioAutenticado_RetornaVistaInicio() throws Exception {
        // Ahora el usuario logueado intenta entrar a /inicio
        mockMvc.perform(get("/inicio"))
                .andExpect(status().isOk()) // Esperamos un 200 OK
                .andExpect(view().name("inicio.html")); // Esperamos que le muestre la vista inicio.html
    }
}