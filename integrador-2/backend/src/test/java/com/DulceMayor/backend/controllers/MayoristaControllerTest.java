package com.DulceMayor.backend.controllers;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.DulceMayor.backend.config.JwtProvider;
import com.DulceMayor.backend.dtos.mayorista.ConfiguracionCorreoMayoristaRequest;
import com.DulceMayor.backend.dtos.mayorista.MayoristaRequest;
import com.DulceMayor.backend.entities.ProveedorAuth;
import com.DulceMayor.backend.entities.RolUsuario;
import com.DulceMayor.backend.entities.Usuario;
import com.DulceMayor.backend.entities.mayorista.TipoEmpresa;
import com.DulceMayor.backend.repositories.UsuarioRepository;
import com.DulceMayor.backend.repositories.mayorista.ConfiguracionCorreoMayoristaRepository;
import com.DulceMayor.backend.repositories.mayorista.MayoristaRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Tests de integración con MockMvc para {@link com.DulceMayor.backend.controllers.mayorista.MayoristaController}.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MayoristaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private ConfiguracionCorreoMayoristaRepository configuracionCorreoMayoristaRepository;

    @Autowired
    private MayoristaRepository mayoristaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtProvider jwtProvider;

    @BeforeEach
    void setUp() {
        limpiarTablas();
    }

    @AfterEach
    void tearDown() {
        limpiarTablas();
    }

    private void limpiarTablas() {
        configuracionCorreoMayoristaRepository.deleteAll();
        mayoristaRepository.deleteAll();
        usuarioRepository.deleteAll();
    }

    private Usuario crearUsuario(String username, RolUsuario rol) {
        return usuarioRepository.save(Usuario.builder()
                .nombreUsuario(username)
                .clave(passwordEncoder.encode("ClaveDefinitiva123"))
                .rol(rol)
                .claveTemporal(false)
                .proveedorAuth(ProveedorAuth.LOCAL)
                .eliminado(false)
                .build());
    }

    @Test
    @DisplayName("CRUD de Mayorista, sucursales públicas y configuración SMTP")
    void crudMayoristaYSmtp() throws Exception {
        Usuario jefe = crearUsuario("jefe_mayorista", RolUsuario.JEFE);
        String tokenJefe = jwtProvider.generarToken(jefe);

        // 1. Jefe crea una sucursal mayorista
        MayoristaRequest req = new MayoristaRequest(
                "Dulce Mayor Sucursal Godoy Cruz",
                "30-71234567-9",
                TipoEmpresa.SUCURSAL,
                List.of(),
                List.of(),
                List.of()
        );

        MvcResult createRes = mockMvc.perform(post("/mayoristas")
                        .header("Authorization", "Bearer " + tokenJefe)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.razonSocial").value("Dulce Mayor Sucursal Godoy Cruz"))
                .andExpect(jsonPath("$.tipoEmpresa").value("SUCURSAL"))
                .andReturn();

        JsonNode creadoJson = objectMapper.readTree(createRes.getResponse().getContentAsString());
        String mayoristaId = creadoJson.get("id").asText();

        // 2. Consulta pública de sucursales (sin Authorization header)
        mockMvc.perform(get("/publico/sucursales"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(mayoristaId));

        // 3. Jefe configura el correo SMTP del mayorista
        ConfiguracionCorreoMayoristaRequest smtpReq = new ConfiguracionCorreoMayoristaRequest(
                "ventas@dulcemayor.com",
                "SuperPasswordSmtp123",
                "587",
                "smtp.office365.com",
                true
        );

        mockMvc.perform(put("/mayoristas/" + mayoristaId + "/configuracion-correo")
                        .header("Authorization", "Bearer " + tokenJefe)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(smtpReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correo").value("ventas@dulcemayor.com"))
                .andExpect(jsonPath("$.puerto").value("587"))
                .andExpect(jsonPath("$.clave").doesNotExist());

        // 4. Jefe consulta la configuración SMTP guardada
        mockMvc.perform(get("/mayoristas/" + mayoristaId + "/configuracion-correo")
                        .header("Authorization", "Bearer " + tokenJefe))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correo").value("ventas@dulcemayor.com"))
                .andExpect(jsonPath("$.clave").doesNotExist());
    }
}

