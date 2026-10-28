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
import com.DulceMayor.backend.dtos.SubCategoriaRequest;
import com.DulceMayor.backend.entities.Categoria;
import com.DulceMayor.backend.entities.ProveedorAuth;
import com.DulceMayor.backend.entities.RolUsuario;
import com.DulceMayor.backend.entities.SubCategoria;
import com.DulceMayor.backend.entities.Usuario;
import com.DulceMayor.backend.repositories.CategoriaRepository;
import com.DulceMayor.backend.repositories.SubCategoriaRepository;
import com.DulceMayor.backend.repositories.UsuarioRepository;
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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SubCategoriaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private com.DulceMayor.backend.repositories.VigenciaPrecioRepository vigenciaPrecioRepository;

    @Autowired
    private com.DulceMayor.backend.repositories.StockRepository stockRepository;

    @Autowired
    private com.DulceMayor.backend.repositories.ProductoRepository productoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private SubCategoriaRepository subCategoriaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtProvider jwtProvider;

    private String tokenJefe;
    private String tokenPreventista;
    private Categoria categoriaBase;

    @BeforeEach
    void setUp() {
        vigenciaPrecioRepository.deleteAll();
        stockRepository.deleteAll();
        productoRepository.deleteAll();
        subCategoriaRepository.deleteAll();
        categoriaRepository.deleteAll();
        usuarioRepository.deleteAll();

        Usuario jefe = usuarioRepository.save(Usuario.builder()
                .nombreUsuario("jefe_test")
                .clave(passwordEncoder.encode("clave12345"))
                .rol(RolUsuario.JEFE)
                .claveTemporal(false)
                .proveedorAuth(ProveedorAuth.LOCAL)
                .eliminado(false)
                .build());
        tokenJefe = jwtProvider.generarToken(jefe);

        Usuario preventista = usuarioRepository.save(Usuario.builder()
                .nombreUsuario("preventista_test")
                .clave(passwordEncoder.encode("clave12345"))
                .rol(RolUsuario.PREVENTISTA)
                .claveTemporal(false)
                .proveedorAuth(ProveedorAuth.LOCAL)
                .eliminado(false)
                .build());
        tokenPreventista = jwtProvider.generarToken(preventista);

        categoriaBase = categoriaRepository.save(Categoria.builder()
                .nombre("Golosinas")
                .eliminado(false)
                .build());
    }

    @Test
    @DisplayName("GET /subcategorias es público (sin token) y responde 200 OK")
    void listarSubCategoriaPublico() throws Exception {
        subCategoriaRepository.save(SubCategoria.builder().nombre("Caramelos").categoria(categoriaBase).eliminado(false).build());

        mockMvc.perform(get("/subcategorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nombre").value("Caramelos"));
    }

    @Test
    @DisplayName("POST /subcategorias con rol JEFE responde 201 Created y header Location")
    void crearSubCategoriaConJefe() throws Exception {
        SubCategoriaRequest req = new SubCategoriaRequest("Chupetines", categoriaBase.getId());

        mockMvc.perform(post("/subcategorias")
                        .header("Authorization", "Bearer " + tokenJefe)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.nombre").value("Chupetines"))
                .andExpect(jsonPath("$.categoriaId").value(categoriaBase.getId()));
    }

    @Test
    @DisplayName("POST /subcategorias con rol no autorizado (PREVENTISTA) responde 403 Forbidden")
    void crearSubCategoriaRolNoAutorizado() throws Exception {
        SubCategoriaRequest req = new SubCategoriaRequest("Alfajores", categoriaBase.getId());

        mockMvc.perform(post("/subcategorias")
                        .header("Authorization", "Bearer " + tokenPreventista)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESO_DENEGADO"));
    }

    @Test
    @DisplayName("POST /subcategorias con nombre duplicado en la misma categoría responde 409 Conflict")
    void crearSubCategoriaDuplicada() throws Exception {
        subCategoriaRepository.save(SubCategoria.builder().nombre("Chupetines").categoria(categoriaBase).eliminado(false).build());

        SubCategoriaRequest req = new SubCategoriaRequest("Chupetines", categoriaBase.getId());

        mockMvc.perform(post("/subcategorias")
                        .header("Authorization", "Bearer " + tokenJefe)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICADO"));
    }

    @Test
    @DisplayName("DELETE /subcategorias/{id} con rol JEFE responde 204 No Content")
    void eliminarSubCategoria() throws Exception {
        SubCategoria sub = subCategoriaRepository.save(SubCategoria.builder().nombre("Gomitas").categoria(categoriaBase).eliminado(false).build());

        mockMvc.perform(delete("/subcategorias/{id}", sub.getId())
                        .header("Authorization", "Bearer " + tokenJefe))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/subcategorias/{id}", sub.getId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NO_ENCONTRADO"));
    }
}
