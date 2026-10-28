package com.DulceMayor.backend.controllers;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.DulceMayor.backend.config.JwtProvider;
import com.DulceMayor.backend.dtos.ProductoRequest;
import com.DulceMayor.backend.entities.Categoria;
import com.DulceMayor.backend.entities.Producto;
import com.DulceMayor.backend.entities.ProveedorAuth;
import com.DulceMayor.backend.entities.RolUsuario;
import com.DulceMayor.backend.entities.SubCategoria;
import com.DulceMayor.backend.entities.Usuario;
import com.DulceMayor.backend.repositories.CategoriaRepository;
import com.DulceMayor.backend.repositories.ProductoRepository;
import com.DulceMayor.backend.repositories.StockRepository;
import com.DulceMayor.backend.repositories.SubCategoriaRepository;
import com.DulceMayor.backend.repositories.UsuarioRepository;
import com.DulceMayor.backend.repositories.VigenciaPrecioRepository;
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
class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private com.DulceMayor.backend.repositories.PromocionRepository promocionRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private SubCategoriaRepository subCategoriaRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private VigenciaPrecioRepository vigenciaPrecioRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtProvider jwtProvider;

    private String tokenJefe;
    private String tokenPreventista;
    private SubCategoria subCategoria;

    @BeforeEach
    void setUp() {
        promocionRepository.deleteAll();
        vigenciaPrecioRepository.deleteAll();
        stockRepository.deleteAll();
        productoRepository.deleteAll();
        subCategoriaRepository.deleteAll();
        categoriaRepository.deleteAll();
        usuarioRepository.deleteAll();

        Usuario jefe = usuarioRepository.save(Usuario.builder()
                .nombreUsuario("jefe_prod")
                .clave(passwordEncoder.encode("clave12345"))
                .rol(RolUsuario.JEFE)
                .claveTemporal(false)
                .proveedorAuth(ProveedorAuth.LOCAL)
                .eliminado(false)
                .build());
        tokenJefe = jwtProvider.generarToken(jefe);

        Usuario prev = usuarioRepository.save(Usuario.builder()
                .nombreUsuario("prev_prod")
                .clave(passwordEncoder.encode("clave12345"))
                .rol(RolUsuario.PREVENTISTA)
                .claveTemporal(false)
                .proveedorAuth(ProveedorAuth.LOCAL)
                .eliminado(false)
                .build());
        tokenPreventista = jwtProvider.generarToken(prev);

        Categoria cat = categoriaRepository.save(Categoria.builder().nombre("Golosinas").eliminado(false).build());
        subCategoria = subCategoriaRepository.save(SubCategoria.builder().nombre("Alfajores").categoria(cat).eliminado(false).build());
    }

    @Test
    @DisplayName("POST /productos crea producto e inicializa vigencia de precio y stock")
    void crearProductoCompleto() throws Exception {
        ProductoRequest req = new ProductoRequest(
                "ALF-TRIPLE",
                "Alfajor Triple Chocolate",
                "Cobertura chocolate negro",
                "Caja x12",
                12,
                true,
                null,
                subCategoria.getId(),
                450.0,
                80
        );

        mockMvc.perform(post("/productos")
                        .header("Authorization", "Bearer " + tokenJefe)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo").value("ALF-TRIPLE"))
                .andExpect(jsonPath("$.nombre").value("Alfajor Triple Chocolate"))
                .andExpect(jsonPath("$.precioVigente").value(450.0))
                .andExpect(jsonPath("$.stockActual").value(80));
    }

    @Test
    @DisplayName("GET /productos/{id} accesible por PREVENTISTA")
    void buscarProductoPreventista() throws Exception {
        Producto p = productoRepository.save(Producto.builder()
                .codigo("ALF-01")
                .nombre("Alfajor Clásico")
                .presentacion("Caja x24")
                .unidadesPorBulto(24)
                .subCategoria(subCategoria)
                .eliminado(false)
                .build());

        mockMvc.perform(get("/productos/{id}", p.getId())
                        .header("Authorization", "Bearer " + tokenPreventista))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value("ALF-01"));
    }
}
