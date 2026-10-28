package com.DulceMayor.backend.controllers;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.DulceMayor.backend.config.JwtProvider;
import com.DulceMayor.backend.dtos.StockAjusteRequest;
import com.DulceMayor.backend.dtos.StockReposicionRequest;
import com.DulceMayor.backend.entities.Categoria;
import com.DulceMayor.backend.entities.Producto;
import com.DulceMayor.backend.entities.ProveedorAuth;
import com.DulceMayor.backend.entities.RolUsuario;
import com.DulceMayor.backend.entities.Stock;
import com.DulceMayor.backend.entities.SubCategoria;
import com.DulceMayor.backend.entities.Usuario;
import com.DulceMayor.backend.repositories.CategoriaRepository;
import com.DulceMayor.backend.repositories.ProductoRepository;
import com.DulceMayor.backend.repositories.StockRepository;
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
class StockControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private com.DulceMayor.backend.repositories.VigenciaPrecioRepository vigenciaPrecioRepository;

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
    private String tokenPreventista;
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
                .nombreUsuario("jefe_stock")
                .clave(passwordEncoder.encode("clave12345"))
                .rol(RolUsuario.JEFE)
                .claveTemporal(false)
                .proveedorAuth(ProveedorAuth.LOCAL)
                .eliminado(false)
                .build());
        tokenJefe = jwtProvider.generarToken(jefe);

        Usuario prev = usuarioRepository.save(Usuario.builder()
                .nombreUsuario("prev_stock")
                .clave(passwordEncoder.encode("clave12345"))
                .rol(RolUsuario.PREVENTISTA)
                .claveTemporal(false)
                .proveedorAuth(ProveedorAuth.LOCAL)
                .eliminado(false)
                .build());
        tokenPreventista = jwtProvider.generarToken(prev);

        Categoria cat = categoriaRepository.save(Categoria.builder().nombre("Chocolates").eliminado(false).build());
        SubCategoria sub = subCategoriaRepository.save(SubCategoria.builder().nombre("Barritas").categoria(cat).eliminado(false).build());
        producto = productoRepository.save(Producto.builder()
                .codigo("BAR-01")
                .nombre("Barra Cereal")
                .presentacion("Display x20")
                .unidadesPorBulto(20)
                .subCategoria(sub)
                .eliminado(false)
                .build());

        stockRepository.save(Stock.builder()
                .cantidadActual(40)
                .observacion("Stock inicial")
                .eliminado(false)
                .build());
    }

    @Test
    @DisplayName("GET /productos/{id}/stock accesible para PREVENTISTA (200 OK)")
    void buscarStockPorProductoPreventista() throws Exception {
        mockMvc.perform(get("/productos/{id}/stock", producto.getId())
                        .header("Authorization", "Bearer " + tokenPreventista))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cantidadActual").value(40));
    }

    @Test
    @DisplayName("PUT /productos/{id}/stock ajusta stock físico")
    void modificarStockAjuste() throws Exception {
        StockAjusteRequest req = new StockAjusteRequest(15, "Rotura");

        mockMvc.perform(put("/productos/{id}/stock", producto.getId())
                        .header("Authorization", "Bearer " + tokenJefe)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cantidadActual").value(15))
                .andExpect(jsonPath("$.observacion").value("Rotura"));
    }

    @Test
    @DisplayName("POST /productos/{id}/stock/reposiciones suma stock")
    void reponerStock() throws Exception {
        StockReposicionRequest req = new StockReposicionRequest(10, "Reposición semanal");

        mockMvc.perform(post("/productos/{id}/stock/reposiciones", producto.getId())
                        .header("Authorization", "Bearer " + tokenJefe)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cantidadActual").value(50)); // 40 + 10
    }

    @Test
    @DisplayName("GET /stock devuelve página de stock")
    void listarStockPaginado() throws Exception {
        mockMvc.perform(get("/stock")
                        .header("Authorization", "Bearer " + tokenJefe))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].cantidadActual").value(40));
    }
}
