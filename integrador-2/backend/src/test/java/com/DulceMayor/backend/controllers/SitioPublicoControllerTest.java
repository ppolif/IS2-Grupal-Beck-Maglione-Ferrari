package com.DulceMayor.backend.controllers;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.DulceMayor.backend.dtos.producto.ProductoRequest;
import com.DulceMayor.backend.dtos.producto.ProductoResponse;
import com.DulceMayor.backend.dtos.publico.ChatbotPresupuestoRequest;
import com.DulceMayor.backend.entities.producto.Categoria;
import com.DulceMayor.backend.entities.producto.SubCategoria;
import com.DulceMayor.backend.repositories.producto.CategoriaRepository;
import com.DulceMayor.backend.repositories.producto.ProductoRepository;
import com.DulceMayor.backend.repositories.producto.StockRepository;
import com.DulceMayor.backend.repositories.producto.SubCategoriaRepository;
import com.DulceMayor.backend.repositories.producto.VigenciaPrecioRepository;
import com.DulceMayor.backend.services.producto.ProductoService;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Tests de integración con MockMvc para {@link com.DulceMayor.backend.controllers.publico.SitioPublicoController}
 * y los adaptadores GoF ({@code CotizacionDolarAdapter} y {@code SpringAiChatbotAdapter}).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SitioPublicoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private ProductoService productoService;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private SubCategoriaRepository subCategoriaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private VigenciaPrecioRepository vigenciaPrecioRepository;

    @Autowired
    private StockRepository stockRepository;

    private SubCategoria subCategoriaBase;

    @BeforeEach
    void setUp() {
        limpiarTablas();
        Categoria cat = categoriaRepository.save(Categoria.builder()
                .nombre("Golosinas")
                .eliminado(false)
                .build());
        subCategoriaBase = subCategoriaRepository.save(SubCategoria.builder()
                .nombre("Alfajores y Chocolates")
                .categoria(cat)
                .eliminado(false)
                .build());
    }

    @AfterEach
    void tearDown() {
        limpiarTablas();
    }

    private void limpiarTablas() {
        stockRepository.deleteAll();
        vigenciaPrecioRepository.deleteAll();
        productoRepository.deleteAll();
        subCategoriaRepository.deleteAll();
        categoriaRepository.deleteAll();
    }

    @Test
    @DisplayName("GET /publico/catalogo y /publico/catalogo/{id} exponen productos con stock sin revelar stockActual")
    void catalogoPublicoExcluyeStockActual() throws Exception {
        ProductoResponse creado = productoService.crear(new ProductoRequest(
                "ALF-001",
                "Alfajor Triple Dulce Mayor",
                "Caja de 24 unidades",
                "Caja x24",
                24,
                true,
                null,
                subCategoriaBase.getId(),
                15000.0,
                10
        ));

        mockMvc.perform(get("/publico/catalogo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(creado.id()))
                .andExpect(jsonPath("$.content[0].nombre").value("Alfajor Triple Dulce Mayor"))
                .andExpect(jsonPath("$.content[0].precio").value(15000.0))
                .andExpect(jsonPath("$.content[0].stockActual").doesNotExist());

        mockMvc.perform(get("/api/v1/publico/catalogo/" + creado.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(creado.id()))
                .andExpect(jsonPath("$.precio").value(15000.0))
                .andExpect(jsonPath("$.stockActual").doesNotExist());
    }

    @Test
    @DisplayName("GET /publico/cotizacion-dolar devuelve la cotización mediante el Adapter sin requerir token")
    void cotizacionDolarPublica() throws Exception {
        mockMvc.perform(get("/publico/cotizacion-dolar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.moneda").value("USD"))
                .andExpect(jsonPath("$.compra", greaterThan(0.0)))
                .andExpect(jsonPath("$.venta", greaterThan(0.0)))
                .andExpect(jsonPath("$.esValorRespaldo").isBoolean());
    }

    @Test
    @DisplayName("POST /publico/chatbot/presupuesto interpreta pedido natural y calcula precios/advertencias en el backend")
    void chatbotPresupuestoCalculaTotalesYAdvertenciasEnBackend() throws Exception {
        productoService.crear(new ProductoRequest(
                "ALF-TRIP",
                "Alfajor Triple",
                "Alfajor triple bañado en chocolate",
                "Caja x24",
                24,
                true,
                null,
                subCategoriaBase.getId(),
                12000.0,
                20
        ));

        ChatbotPresupuestoRequest request = new ChatbotPresupuestoRequest(
                "Hola, quiero dos cajas de alfajores triples y 3 gomitas frutales",
                List.of()
        );

        mockMvc.perform(post("/publico/chatbot/presupuesto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].productoNombre").value("Alfajor Triple"))
                .andExpect(jsonPath("$.items[0].cantidad").value(2))
                .andExpect(jsonPath("$.items[0].precioUnitario").value(12000.0))
                .andExpect(jsonPath("$.items[0].subtotal").value(24000.0))
                .andExpect(jsonPath("$.total").value(24000.0))
                .andExpect(jsonPath("$.advertencias", hasSize(1)));
    }
}

