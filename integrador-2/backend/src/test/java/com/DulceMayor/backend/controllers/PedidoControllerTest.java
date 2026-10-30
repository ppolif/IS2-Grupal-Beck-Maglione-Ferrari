package com.DulceMayor.backend.controllers;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.DulceMayor.backend.config.JwtProvider;
import com.DulceMayor.backend.controllers.pedido.PedidoController;
import com.DulceMayor.backend.dtos.pedido.AnularPedidoRequest;
import com.DulceMayor.backend.dtos.pedido.DetallePedidoRequest;
import com.DulceMayor.backend.dtos.pedido.PedidoRequest;
import com.DulceMayor.backend.dtos.pedido.PedidoUpdateRequest;
import com.DulceMayor.backend.dtos.producto.CategoriaRequest;
import com.DulceMayor.backend.dtos.producto.CategoriaResponse;
import com.DulceMayor.backend.dtos.producto.ProductoRequest;
import com.DulceMayor.backend.dtos.producto.ProductoResponse;
import com.DulceMayor.backend.dtos.producto.PromocionRequest;
import com.DulceMayor.backend.dtos.producto.StockReposicionRequest;
import com.DulceMayor.backend.dtos.producto.SubCategoriaRequest;
import com.DulceMayor.backend.dtos.producto.SubCategoriaResponse;
import com.DulceMayor.backend.entities.ProveedorAuth;
import com.DulceMayor.backend.entities.RolUsuario;
import com.DulceMayor.backend.entities.Usuario;
import com.DulceMayor.backend.repositories.UsuarioRepository;
import com.DulceMayor.backend.repositories.facturacion.DetalleFacturaRepository;
import com.DulceMayor.backend.repositories.facturacion.FacturaRepository;
import com.DulceMayor.backend.repositories.facturacion.PagoRepository;
import com.DulceMayor.backend.repositories.pedido.DetallePedidoRepository;
import com.DulceMayor.backend.repositories.pedido.HistorialEstadoPedidoRepository;
import com.DulceMayor.backend.repositories.pedido.PedidoRepository;
import com.DulceMayor.backend.repositories.producto.CategoriaRepository;
import com.DulceMayor.backend.repositories.producto.ProductoRepository;
import com.DulceMayor.backend.repositories.producto.PromocionRepository;
import com.DulceMayor.backend.repositories.producto.StockRepository;
import com.DulceMayor.backend.repositories.producto.SubCategoriaRepository;
import com.DulceMayor.backend.repositories.producto.VigenciaPrecioRepository;
import com.DulceMayor.backend.services.producto.CategoriaService;
import com.DulceMayor.backend.services.producto.ProductoService;
import com.DulceMayor.backend.services.producto.PromocionService;
import com.DulceMayor.backend.services.producto.StockService;
import com.DulceMayor.backend.services.producto.SubCategoriaService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
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
 * Tests de integración con MockMvc y Spring Security para {@link PedidoController}.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PedidoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private PagoRepository pagoRepository;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private DetalleFacturaRepository detalleFacturaRepository;

    @Autowired
    private FacturaRepository facturaRepository;

    @Autowired
    private HistorialEstadoPedidoRepository historialEstadoPedidoRepository;

    @Autowired
    private DetallePedidoRepository detallePedidoRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private PromocionRepository promocionRepository;

    @Autowired
    private VigenciaPrecioRepository vigenciaPrecioRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private SubCategoriaRepository subCategoriaRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CategoriaService categoriaService;

    @Autowired
    private SubCategoriaService subCategoriaService;

    @Autowired
    private ProductoService productoService;

    @Autowired
    private PromocionService promocionService;

    @Autowired
    private StockService stockService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtProvider jwtProvider;

    private ProductoResponse productoInicial;

    @BeforeEach
    void setUp() {
        limpiarTablas();

        CategoriaResponse cat = categoriaService.crear(new CategoriaRequest("Chocolates"));
        SubCategoriaResponse sub = subCategoriaService.crear(new SubCategoriaRequest("Tabletas", cat.id()));
        productoInicial = productoService.crear(new ProductoRequest(
                "CHOC-01", "Tableta Leche", "Clásica", "Display x12", 12, false, null, sub.id(), 1000.0, 5
        ));
        promocionService.crear(new PromocionRequest(
                "PROMO10", "10% descuento llevando 2 o más", 10.0, 2, productoInicial.id(),
                LocalDate.now().minusDays(1), LocalDate.now().plusDays(10)
        ));
    }

    @AfterEach
    void tearDown() {
        limpiarTablas();
    }

    private void limpiarTablas() {
        pagoRepository.deleteAll();
        stockRepository.deleteAll();
        detalleFacturaRepository.deleteAll();
        facturaRepository.deleteAll();
        historialEstadoPedidoRepository.deleteAll();
        detallePedidoRepository.deleteAll();
        pedidoRepository.deleteAll();
        promocionRepository.deleteAll();
        vigenciaPrecioRepository.deleteAll();
        productoRepository.deleteAll();
        subCategoriaRepository.deleteAll();
        categoriaRepository.deleteAll();
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
    @DisplayName("Ciclo completo: crear en CARGADO -> modificar -> procesar SIN_STOCK -> reponer -> procesar FACTURADO con facturaId e historial")
    void cicloCompletoPedidoProcesamientoEHistorial() throws Exception {
        Usuario preventista = crearUsuario("prev_pedidos", RolUsuario.PREVENTISTA);
        Usuario admin = crearUsuario("admin_pedidos", RolUsuario.ADMINISTRATIVO);
        Usuario minorista = crearUsuario("min_pedidos", RolUsuario.MINORISTA);

        String tokenPreventista = jwtProvider.generarToken(preventista);
        String tokenAdmin = jwtProvider.generarToken(admin);
        String tokenMinorista = jwtProvider.generarToken(minorista);

        // 1. Preventista crea pedido de 2 unidades con PROMO10 (stock disponible = 5)
        MvcResult createRes = mockMvc.perform(post("/pedidos")
                        .header("Authorization", "Bearer " + tokenPreventista)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PedidoRequest(
                                "pdv-centro",
                                null,
                                "PROMO10",
                                List.of(new DetallePedidoRequest(productoInicial.id(), 2))
                        ))))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.estado").value("CARGADO"))
                .andExpect(jsonPath("$.preventorId").value(preventista.getId()))
                .andExpect(jsonPath("$.subtotal").value(2000.0))
                .andExpect(jsonPath("$.descuento").value(200.0))
                .andExpect(jsonPath("$.total").value(1800.0))
                .andReturn();

        JsonNode creadoJson = objectMapper.readTree(createRes.getResponse().getContentAsString());
        String pedidoId = creadoJson.get("id").asText();

        // 2. Preventista modifica el pedido en CARGADO subiendo a 8 unidades (supera el stock actual de 5)
        mockMvc.perform(put("/pedidos/" + pedidoId)
                        .header("Authorization", "Bearer " + tokenPreventista)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PedidoUpdateRequest(
                                "PROMO10",
                                List.of(new DetallePedidoRequest(productoInicial.id(), 8))
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subtotal").value(8000.0))
                .andExpect(jsonPath("$.descuento").value(800.0))
                .andExpect(jsonPath("$.total").value(7200.0));

        // 3. Administrativo intenta procesar -> falta stock (pide 8, hay 5) -> pasa a SIN_STOCK sin factura
        mockMvc.perform(post("/pedidos/" + pedidoId + "/procesar")
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pedido.estado").value("SIN_STOCK"))
                .andExpect(jsonPath("$.factura").isEmpty())
                .andExpect(jsonPath("$.itemsSinStock", hasSize(1)))
                .andExpect(jsonPath("$.itemsSinStock[0].solicitado").value(8))
                .andExpect(jsonPath("$.itemsSinStock[0].disponible").value(5));

        // 4. Ya no está en CARGADO -> modificar devuelve 409 Conflict
        mockMvc.perform(put("/pedidos/" + pedidoId)
                        .header("Authorization", "Bearer " + tokenPreventista)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PedidoUpdateRequest(
                                null,
                                List.of(new DetallePedidoRequest(productoInicial.id(), 1))
                        ))))
                .andExpect(status().isConflict());

        // 5. Se repone stock (+10 -> quedan 15 disponibles) y se vuelve a procesar -> pasa a FACTURADO y emite Factura
        stockService.reponerStock(productoInicial.id(), new StockReposicionRequest(10, "Ingreso proveedor"));

        MvcResult procOk = mockMvc.perform(post("/pedidos/" + pedidoId + "/procesar")
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pedido.estado").value("FACTURADO"))
                .andExpect(jsonPath("$.factura.estado").value("PENDIENTE_PAGO"))
                .andExpect(jsonPath("$.factura.total").value(7200.0))
                .andExpect(jsonPath("$.itemsSinStock", hasSize(0)))
                .andReturn();

        String facturaId = objectMapper.readTree(procOk.getResponse().getContentAsString())
                .get("factura").get("id").asText();

        // 6. Consultar el pedido individual y en mi-cuenta expone facturaId resuelto por PedidoLecturaService
        mockMvc.perform(get("/pedidos/" + pedidoId)
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.facturaId").value(facturaId));

        mockMvc.perform(get("/mi-cuenta/pedidos")
                        .header("Authorization", "Bearer " + tokenMinorista)
                        .param("estado", "FACTURADO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].facturaId").value(facturaId));

        // 7. Consultar trazabilidad devuelve los 3 cambios cronológicos: CARGADO -> SIN_STOCK -> FACTURADO
        mockMvc.perform(get("/pedidos/" + pedidoId + "/historial")
                        .header("Authorization", "Bearer " + tokenMinorista))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].estado").value("CARGADO"))
                .andExpect(jsonPath("$[1].estado").value("SIN_STOCK"))
                .andExpect(jsonPath("$[2].estado").value("FACTURADO"));

        // 8. Intentar anular un pedido ya FACTURADO responde 409 Conflict
        mockMvc.perform(post("/pedidos/" + pedidoId + "/anular")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AnularPedidoRequest("Intento tardío"))))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Visibilidad por rol: un PREVENTISTA solo ve sus propios pedidos y recibe 403 al consultar pedidos ajenos")
    void visibilidadDePreventistaAislada() throws Exception {
        Usuario prev1 = crearUsuario("prev_uno", RolUsuario.PREVENTISTA);
        Usuario prev2 = crearUsuario("prev_dos", RolUsuario.PREVENTISTA);
        String tokenPrev1 = jwtProvider.generarToken(prev1);
        String tokenPrev2 = jwtProvider.generarToken(prev2);

        MvcResult resPrev1 = mockMvc.perform(post("/pedidos")
                        .header("Authorization", "Bearer " + tokenPrev1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PedidoRequest(
                                "pdv-norte",
                                null,
                                null,
                                List.of(new DetallePedidoRequest(productoInicial.id(), 1))
                        ))))
                .andExpect(status().isCreated())
                .andReturn();

        String pedidoPrev1Id = objectMapper.readTree(resPrev1.getResponse().getContentAsString()).get("id").asText();

        // prev2 lista pedidos y obtiene 0 porque solo ve los suyos
        mockMvc.perform(get("/pedidos")
                        .header("Authorization", "Bearer " + tokenPrev2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));

        // prev2 intenta buscar el pedido de prev1 -> 403 Forbidden
        mockMvc.perform(get("/pedidos/" + pedidoPrev1Id)
                        .header("Authorization", "Bearer " + tokenPrev2))
                .andExpect(status().isForbidden());
    }
}

