package com.DulceMayor.backend.controllers;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.DulceMayor.backend.config.JwtProvider;
import com.DulceMayor.backend.dtos.producto.VigenciaPrecioRequest;
import com.DulceMayor.backend.entities.producto.Categoria;
import com.DulceMayor.backend.entities.producto.Producto;
import com.DulceMayor.backend.entities.ProveedorAuth;
import com.DulceMayor.backend.entities.RolUsuario;
import com.DulceMayor.backend.entities.producto.SubCategoria;
import com.DulceMayor.backend.entities.Usuario;
import com.DulceMayor.backend.entities.producto.VigenciaPrecio;
import com.DulceMayor.backend.repositories.producto.CategoriaRepository;
import com.DulceMayor.backend.repositories.producto.ProductoRepository;
import com.DulceMayor.backend.repositories.producto.SubCategoriaRepository;
import com.DulceMayor.backend.repositories.UsuarioRepository;
import com.DulceMayor.backend.repositories.producto.VigenciaPrecioRepository;
import com.DulceMayor.backend.repositories.producto.StockRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
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
class VigenciaPrecioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Autowired
    private VigenciaPrecioRepository vigenciaPrecioRepository;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private SubCategoriaRepository subCategoriaRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtProvider jwtProvider;

    private String tokenJefe;
    private Producto producto;

    @BeforeEach
    void setUp() {
        vigenciaPrecioRepository.deleteAll();
        stockRepository.deleteAll();
        productoRepository.deleteAll();
        subCategoriaRepository.deleteAll();
        categoriaRepository.deleteAll();
        usuarioRepository.deleteAll();

        Usuario jefe = usuarioRepository.save(Usuario.builder()
                .nombreUsuario("jefe_vig")
                .clave(passwordEncoder.encode("clave12345"))
                .rol(RolUsuario.JEFE)
                .claveTemporal(false)
                .proveedorAuth(ProveedorAuth.LOCAL)
                .eliminado(false)
                .build());
        tokenJefe = jwtProvider.generarToken(jefe);

        Categoria cat = categoriaRepository.save(Categoria.builder().nombre("Golosinas").eliminado(false).build());
        SubCategoria sub = subCategoriaRepository.save(SubCategoria.builder().nombre("Alfajores").categoria(cat).eliminado(false).build());
        producto = productoRepository.save(Producto.builder()
                .codigo("ALF-01")
                .nombre("Alfajor Simple")
                .presentacion("Caja x24")
                .unidadesPorBulto(24)
                .enOferta(false)
                .subCategoria(sub)
                .eliminado(false)
                .build());
    }

    @Test
    @DisplayName("POST /productos/{id}/vigencias-precio crea nueva vigencia (201 Created)")
    void crearVigencia() throws Exception {
        VigenciaPrecioRequest req = new VigenciaPrecioRequest(LocalDate.of(2026, 10, 1), null, 350.0);

        mockMvc.perform(post("/productos/{id}/vigencias-precio", producto.getId())
                        .header("Authorization", "Bearer " + tokenJefe)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productoId").value(producto.getId()))
                .andExpect(jsonPath("$.precio").value(350.0));
    }

    @Test
    @DisplayName("GET /productos/{id}/vigencias-precio lista el historial de precios")
    void listarVigencias() throws Exception {
        vigenciaPrecioRepository.save(VigenciaPrecio.builder()
                .producto(producto)
                .fechaDesde(LocalDate.of(2026, 1, 1))
                .fechaHasta(LocalDate.of(2026, 6, 30))
                .precio(200.0)
                .eliminado(false)
                .build());

        mockMvc.perform(get("/productos/{id}/vigencias-precio", producto.getId())
                        .header("Authorization", "Bearer " + tokenJefe))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].precio").value(200.0));
    }

    @Test
    @DisplayName("DELETE /vigencias-precio/{id} borrado lógico (204 No Content)")
    void eliminarVigencia() throws Exception {
        VigenciaPrecio vig = vigenciaPrecioRepository.save(VigenciaPrecio.builder()
                .producto(producto)
                .fechaDesde(LocalDate.of(2026, 1, 1))
                .fechaHasta(null)
                .precio(200.0)
                .eliminado(false)
                .build());

        mockMvc.perform(delete("/vigencias-precio/{id}", vig.getId())
                        .header("Authorization", "Bearer " + tokenJefe))
                .andExpect(status().isNoContent());
    }
}
