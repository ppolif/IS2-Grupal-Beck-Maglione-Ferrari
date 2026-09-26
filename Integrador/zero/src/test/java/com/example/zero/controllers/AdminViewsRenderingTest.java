package com.example.zero.controllers;

import com.example.zero.entidades.persona.Usuario;
import com.example.zero.enums.RolUsuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class AdminViewsRenderingTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    private MockHttpSession createAdminSession() {
        MockHttpSession session = new MockHttpSession();
        Usuario admin = Usuario.builder()
                .id("admin-test-id")
                .nombreUsuario("admin@zero.com")
                .rol(RolUsuario.ADMINISTRATIVO)
                .eliminado(false)
                .build();
        session.setAttribute("usuariosession", admin);
        return session;
    }

    @Test
    void testRenderProductsView() throws Exception {
        mockMvc.perform(get("/admin/products").session(createAdminSession()))
                .andExpect(status().isOk());
    }

    @Test
    void testRenderRegistrarCompraView() throws Exception {
        mockMvc.perform(get("/admin/registrar-compra").session(createAdminSession()))
                .andExpect(status().isOk());
    }
}
