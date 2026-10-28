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
import com.DulceMayor.backend.dtos.CategoriaRequest;
import com.DulceMayor.backend.entities.ProveedorAuth;
import com.DulceMayor.backend.entities.RolUsuario;
import com.DulceMayor.backend.entities.Usuario;
import com.DulceMayor.backend.repositories.CategoriaRepository;
import com.DulceMayor.backend.repositories.UsuarioRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Tests de integración con MockMvc y Spring Security para {@link CategoriaController}.
 * Cubre todos los escenarios exigidos por el Paso 2 incluyendo verificación completa de ErrorResponse.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CategoriaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private com.DulceMayor.backend.repositories.SubCategoriaRepository subCategoriaRepository;

    @Autowired
    private com.DulceMayor.backend.repositories.ProductoRepository productoRepository;

    @Autowired
    private com.DulceMayor.backend.repositories.StockRepository stockRepository;

    @Autowired
    private com.DulceMayor.backend.repositories.VigenciaPrecioRepository vigenciaPrecioRepository;

    @Autowired
    private com.DulceMayor.backend.repositories.PromocionRepository promocionRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtProvider jwtProvider;

    @BeforeEach
    void setUp() {
        vigenciaPrecioRepository.deleteAll();
        stockRepository.deleteAll();
        promocionRepository.deleteAll();
        productoRepository.deleteAll();
        subCategoriaRepository.deleteAll();
        categoriaRepository.deleteAll();
        usuarioRepository.deleteAll();
    }

    private String generarTokenParaRol(RolUsuario rol) {
        Usuario usuario = usuarioRepository.save(Usuario.builder()
                .nombreUsuario("user_" + rol.name().toLowerCase())
                .clave(passwordEncoder.encode("ClaveDefinitiva123"))
                .rol(rol)
                .claveTemporal(false)
                .proveedorAuth(ProveedorAuth.LOCAL)
                .eliminado(false)
                .build());
        return jwtProvider.generarToken(usuario);
    }

    @Test
    @DisplayName("GET /categorias sin token responde 200 OK (ruta pública)")
    void getCategoriasSinTokenResponde200() throws Exception {
        mockMvc.perform(get("/categorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("POST /categorias sin token responde 401 con JSON ErrorResponse (NO_AUTENTICADO)")
    void postCategoriaSinTokenResponde401() throws Exception {
        CategoriaRequest request = new CategoriaRequest("Alfajores");

        mockMvc.perform(post("/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.code").value("NO_AUTENTICADO"))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.path").value("/categorias"));
    }

    @Test
    @DisplayName("POST /categorias con rol PREVENTISTA o MINORISTA responde 403 con JSON ErrorResponse (ACCESO_DENEGADO)")
    void postCategoriaConRolesNoAutorizadosResponde403() throws Exception {
        String tokenPreventista = generarTokenParaRol(RolUsuario.PREVENTISTA);
        String tokenMinorista = generarTokenParaRol(RolUsuario.MINORISTA);
        CategoriaRequest request = new CategoriaRequest("Alfajores");

        mockMvc.perform(post("/categorias")
                        .header("Authorization", "Bearer " + tokenPreventista)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.code").value("ACCESO_DENEGADO"))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.path").value("/categorias"));

        mockMvc.perform(post("/categorias")
                        .header("Authorization", "Bearer " + tokenMinorista)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.code").value("ACCESO_DENEGADO"))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.path").value("/categorias"));
    }

    @Test
    @DisplayName("POST /categorias con rol JEFE y ADMINISTRATIVO responde 201 Created con Location y CategoriaResponse")
    void postCategoriaConJefeYAdministrativoResponde201() throws Exception {
        String tokenJefe = generarTokenParaRol(RolUsuario.JEFE);
        String tokenAdministrativo = generarTokenParaRol(RolUsuario.ADMINISTRATIVO);

        mockMvc.perform(post("/categorias")
                        .header("Authorization", "Bearer " + tokenJefe)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CategoriaRequest("  Chocolates  "))))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.nombre").value("Chocolates"));

        mockMvc.perform(post("/categorias")
                        .header("Authorization", "Bearer " + tokenAdministrativo)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CategoriaRequest("Gomitas"))))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.nombre").value("Gomitas"));
    }

    @Test
    @DisplayName("POST /categorias con nombre vacío responde 400 con JSON ErrorResponse (VALIDACION y detalle de campo)")
    void postCategoriaConNombreVacioResponde400Validacion() throws Exception {
        String tokenJefe = generarTokenParaRol(RolUsuario.JEFE);
        CategoriaRequest requestInvalido = new CategoriaRequest("   ");

        mockMvc.perform(post("/categorias")
                        .header("Authorization", "Bearer " + tokenJefe)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestInvalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("VALIDACION"))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.path").value("/categorias"))
                .andExpect(jsonPath("$.errors[0].campo").value("nombre"))
                .andExpect(jsonPath("$.errors[0].mensaje").isNotEmpty());
    }

    @Test
    @DisplayName("POST /categorias duplicado (sin distinguir mayúsculas) responde 409 con JSON ErrorResponse (DUPLICADO)")
    void postCategoriaDuplicadaResponde409Duplicado() throws Exception {
        String tokenJefe = generarTokenParaRol(RolUsuario.JEFE);

        mockMvc.perform(post("/categorias")
                        .header("Authorization", "Bearer " + tokenJefe)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CategoriaRequest("Turrones"))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/categorias")
                        .header("Authorization", "Bearer " + tokenJefe)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CategoriaRequest("  tUrRoNeS  "))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.code").value("DUPLICADO"))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.path").value("/categorias"));
    }

    @Test
    @DisplayName("PUT /categorias/{id} sobre inexistente responde 404 con JSON ErrorResponse (NO_ENCONTRADO)")
    void putCategoriaInexistenteResponde404() throws Exception {
        String tokenJefe = generarTokenParaRol(RolUsuario.JEFE);

        mockMvc.perform(put("/categorias/id-inexistente")
                        .header("Authorization", "Bearer " + tokenJefe)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CategoriaRequest(" caramelos "))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("NO_ENCONTRADO"))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.path").value("/categorias/id-inexistente"));
    }

    @Test
    @DisplayName("PUT /categorias/{id} no choca consigo misma al conservar su nombre con distinto case")
    void putCategoriaNoChocaConsigoMisma() throws Exception {
        String tokenJefe = generarTokenParaRol(RolUsuario.JEFE);

        MvcResult creadoResult = mockMvc.perform(post("/categorias")
                        .header("Authorization", "Bearer " + tokenJefe)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CategoriaRequest("bombones"))))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode creadoJson = objectMapper.readTree(creadoResult.getResponse().getContentAsString());
        String id = creadoJson.get("id").asText();

        mockMvc.perform(put("/categorias/" + id)
                        .header("Authorization", "Bearer " + tokenJefe)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CategoriaRequest("Bombones"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.nombre").value("Bombones"));
    }

    @Test
    @DisplayName("DELETE /categorias/{id} responde 204 y luego la categoría no aparece en el listado ni en búsqueda por ID")
    void deleteCategoriaResponde204YDesapareceDelListado() throws Exception {
        String tokenJefe = generarTokenParaRol(RolUsuario.JEFE);

        MvcResult creadoResult = mockMvc.perform(post("/categorias")
                        .header("Authorization", "Bearer " + tokenJefe)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CategoriaRequest("Chupetines"))))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode creadoJson = objectMapper.readTree(creadoResult.getResponse().getContentAsString());
        String id = creadoJson.get("id").asText();

        mockMvc.perform(get("/categorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(id));

        mockMvc.perform(delete("/categorias/" + id)
                        .header("Authorization", "Bearer " + tokenJefe))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/categorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        mockMvc.perform(get("/categorias/" + id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("NO_ENCONTRADO"));
    }
}

