package com.DulceMayor.backend.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.DulceMayor.backend.dtos.persona.ActualizarPerfilMinoristaRequest;
import com.DulceMayor.backend.dtos.persona.RegistroMinoristaRequest;
import com.DulceMayor.backend.entities.persona.TipoDocumento;
import com.DulceMayor.backend.repositories.UsuarioRepository;
import com.DulceMayor.backend.repositories.persona.MinoristaRepository;
import com.DulceMayor.backend.repositories.persona.PersonaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Tests de integración con MockMvc para el ABM de usuarios minoristas (registro, perfil, actualización y baja).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MinoristaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MinoristaRepository minoristaRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @BeforeEach
    void setUp() {
        limpiarTablas();
    }

    @AfterEach
    void tearDown() {
        limpiarTablas();
    }

    private void limpiarTablas() {
        minoristaRepository.deleteAll();
        personaRepository.deleteAll();
        usuarioRepository.deleteAll();
    }

    @Test
    @DisplayName("Flujo completo ABM Minorista: auto-registro en portal -> consulta de perfil -> edición -> baja lógica")
    void flujoCompletoAbmMinorista() throws Exception {
        RegistroMinoristaRequest regReq = new RegistroMinoristaRequest(
                "Laura",
                "Fernandez",
                TipoDocumento.DNI,
                "37888999",
                "laura@portal.com",
                "ClaveLaura123",
                "Minimarket Laura"
        );

        MvcResult regRes = mockMvc.perform(post("/api/v1/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.debeCambiarClave").value(false))
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        String tokenLaura = objectMapper.readTree(regRes.getResponse().getContentAsString()).get("token").asText();

        mockMvc.perform(get("/api/v1/auth/perfil")
                        .header("Authorization", "Bearer " + tokenLaura))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Laura"))
                .andExpect(jsonPath("$.apellido").value("Fernandez"))
                .andExpect(jsonPath("$.personaId").exists())
                .andExpect(jsonPath("$.usuario.nombreUsuario").value("laura@portal.com"));

        mockMvc.perform(get("/api/v1/mi-cuenta/perfil")
                        .header("Authorization", "Bearer " + tokenLaura))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Laura"))
                .andExpect(jsonPath("$.numeroDocumento").value("37888999"));

        ActualizarPerfilMinoristaRequest actReq = new ActualizarPerfilMinoristaRequest(
                "Laura Beatriz",
                "Fernandez",
                null,
                TipoDocumento.DNI,
                "37888999",
                "laura.nueva@portal.com",
                "2615559988"
        );

        mockMvc.perform(put("/api/v1/mi-cuenta/perfil")
                        .header("Authorization", "Bearer " + tokenLaura)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(actReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Laura Beatriz"));

        mockMvc.perform(delete("/api/v1/mi-cuenta/perfil")
                        .header("Authorization", "Bearer " + tokenLaura))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/mi-cuenta/perfil")
                        .header("Authorization", "Bearer " + tokenLaura))
                .andExpect(status().isUnauthorized());
    }
}

