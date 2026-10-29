package com.DulceMayor.backend.controllers;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.DulceMayor.backend.config.JwtProvider;
import com.DulceMayor.backend.controllers.facturacion.FormaDePagoController;
import com.DulceMayor.backend.dtos.facturacion.FormaDePagoRequest;
import com.DulceMayor.backend.entities.ProveedorAuth;
import com.DulceMayor.backend.entities.RolUsuario;
import com.DulceMayor.backend.entities.Usuario;
import com.DulceMayor.backend.entities.facturacion.TipoPago;
import com.DulceMayor.backend.repositories.UsuarioRepository;
import com.DulceMayor.backend.repositories.facturacion.FacturaRepository;
import com.DulceMayor.backend.repositories.facturacion.FormaDePagoRepository;
import com.DulceMayor.backend.repositories.facturacion.PagoRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
 * Tests de integración con MockMvc y Spring Security para {@link FormaDePagoController}.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FormaDePagoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private PagoRepository pagoRepository;

    @Autowired
    private FacturaRepository facturaRepository;

    @Autowired
    private FormaDePagoRepository formaDePagoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtProvider jwtProvider;

    @BeforeEach
    void setUp() {
        pagoRepository.deleteAll();
        facturaRepository.deleteAll();
        formaDePagoRepository.deleteAll();
        usuarioRepository.deleteAll();
    }

    private String generarTokenParaRol(RolUsuario rol) {
        Usuario usuario = usuarioRepository.save(Usuario.builder()
                .nombreUsuario("user_fp_" + rol.name().toLowerCase())
                .clave(passwordEncoder.encode("ClaveDefinitiva123"))
                .rol(rol)
                .claveTemporal(false)
                .proveedorAuth(ProveedorAuth.LOCAL)
                .eliminado(false)
                .build());
        return jwtProvider.generarToken(usuario);
    }

    @Test
    @DisplayName("GET /formas-de-pago sin token responde 401 NO_AUTENTICADO y con token autenticado responde 200")
    void getFormasDePagoRequiereAutenticacion() throws Exception {
        mockMvc.perform(get("/formas-de-pago"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("NO_AUTENTICADO"));

        String tokenMinorista = generarTokenParaRol(RolUsuario.MINORISTA);
        mockMvc.perform(get("/formas-de-pago")
                        .header("Authorization", "Bearer " + tokenMinorista))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("POST, PUT, DELETE /formas-de-pago permiten JEFE/ADMINISTRATIVO y rechazan MINORISTA con 403")
    void abmFormaDePagoCompletoConControlDeRoles() throws Exception {
        String tokenJefe = generarTokenParaRol(RolUsuario.JEFE);
        String tokenMinorista = generarTokenParaRol(RolUsuario.MINORISTA);

        FormaDePagoRequest req = new FormaDePagoRequest(TipoPago.EFECTIVO, "  Efectivo en caja ");

        mockMvc.perform(post("/formas-de-pago")
                        .header("Authorization", "Bearer " + tokenMinorista)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESO_DENEGADO"));

        MvcResult creado = mockMvc.perform(post("/formas-de-pago")
                        .header("Authorization", "Bearer " + tokenJefe)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.tipoPago").value("EFECTIVO"))
                .andExpect(jsonPath("$.observacion").value("Efectivo en caja"))
                .andReturn();

        JsonNode json = objectMapper.readTree(creado.getResponse().getContentAsString());
        String id = json.get("id").asText();

        mockMvc.perform(put("/formas-de-pago/" + id)
                        .header("Authorization", "Bearer " + tokenJefe)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new FormaDePagoRequest(TipoPago.TRANSFERENCIA, "Transferencia CBU")
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipoPago").value("TRANSFERENCIA"))
                .andExpect(jsonPath("$.observacion").value("Transferencia CBU"));

        mockMvc.perform(delete("/formas-de-pago/" + id)
                        .header("Authorization", "Bearer " + tokenJefe))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/formas-de-pago/" + id)
                        .header("Authorization", "Bearer " + tokenJefe))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NO_ENCONTRADO"));
    }
}

