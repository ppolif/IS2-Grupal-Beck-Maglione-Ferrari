package com.DulceMayor.backend.controllers;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.DulceMayor.backend.config.JwtProvider;
import com.DulceMayor.backend.controllers.facturacion.FacturaController;
import com.DulceMayor.backend.controllers.facturacion.PagoController;
import com.DulceMayor.backend.dtos.facturacion.DetalleFacturaRequest;
import com.DulceMayor.backend.dtos.facturacion.FacturaRequest;
import com.DulceMayor.backend.dtos.facturacion.FacturaResponse;
import com.DulceMayor.backend.dtos.facturacion.FormaDePagoRequest;
import com.DulceMayor.backend.dtos.facturacion.FormaDePagoResponse;
import com.DulceMayor.backend.dtos.facturacion.PagoRequest;
import com.DulceMayor.backend.dtos.producto.CategoriaRequest;
import com.DulceMayor.backend.dtos.producto.CategoriaResponse;
import com.DulceMayor.backend.dtos.producto.ProductoRequest;
import com.DulceMayor.backend.dtos.producto.ProductoResponse;
import com.DulceMayor.backend.dtos.producto.SubCategoriaRequest;
import com.DulceMayor.backend.dtos.producto.SubCategoriaResponse;
import com.DulceMayor.backend.entities.ProveedorAuth;
import com.DulceMayor.backend.entities.RolUsuario;
import com.DulceMayor.backend.entities.Usuario;
import com.DulceMayor.backend.entities.facturacion.TipoPago;
import com.DulceMayor.backend.repositories.UsuarioRepository;
import com.DulceMayor.backend.repositories.facturacion.DetalleFacturaRepository;
import com.DulceMayor.backend.repositories.facturacion.FacturaRepository;
import com.DulceMayor.backend.repositories.facturacion.FormaDePagoRepository;
import com.DulceMayor.backend.repositories.facturacion.PagoRepository;
import com.DulceMayor.backend.repositories.producto.CategoriaRepository;
import com.DulceMayor.backend.repositories.producto.ProductoRepository;
import com.DulceMayor.backend.repositories.producto.StockRepository;
import com.DulceMayor.backend.repositories.producto.SubCategoriaRepository;
import com.DulceMayor.backend.repositories.producto.VigenciaPrecioRepository;
import com.DulceMayor.backend.services.facturacion.FacturaService;
import com.DulceMayor.backend.services.facturacion.FormaDePagoService;
import com.DulceMayor.backend.services.producto.CategoriaService;
import com.DulceMayor.backend.services.producto.ProductoService;
import com.DulceMayor.backend.services.producto.SubCategoriaService;
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
 * Tests de integración con MockMvc y Spring Security para {@link FacturaController} y {@link PagoController}.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FacturaControllerTest {

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
    private FormaDePagoRepository formaDePagoRepository;

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
    private FacturaService facturaService;

    @Autowired
    private FormaDePagoService formaDePagoService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtProvider jwtProvider;

    private FacturaResponse facturaInicial;
    private FormaDePagoResponse formaDePagoInicial;

    @BeforeEach
    void setUp() {
        pagoRepository.deleteAll();
        stockRepository.deleteAll();
        detalleFacturaRepository.deleteAll();
        facturaRepository.deleteAll();
        formaDePagoRepository.deleteAll();
        vigenciaPrecioRepository.deleteAll();
        productoRepository.deleteAll();
        subCategoriaRepository.deleteAll();
        categoriaRepository.deleteAll();
        usuarioRepository.deleteAll();

        CategoriaResponse cat = categoriaService.crear(new CategoriaRequest("Golosinas"));
        SubCategoriaResponse sub = subCategoriaService.crear(new SubCategoriaRequest("Alfajores", cat.id()));
        ProductoResponse prod = productoService.crear(new ProductoRequest(
                "ALF-100", "Alfajor Dulce", "Triple", "Caja x24", 24, false, null, sub.id(), 1000.0, 50
        ));

        formaDePagoInicial = formaDePagoService.crear(new FormaDePagoRequest(TipoPago.TRANSFERENCIA, "CBU Principal"));
        facturaInicial = facturaService.crear(new FacturaRequest(
                "ped-test-1",
                List.of(new DetalleFacturaRequest(prod.id(), 2, 1000.0)),
                0.0
        ));
    }

    @AfterEach
    void tearDown() {
        pagoRepository.deleteAll();
        stockRepository.deleteAll();
        detalleFacturaRepository.deleteAll();
        facturaRepository.deleteAll();
        formaDePagoRepository.deleteAll();
    }

    private String generarTokenParaRol(RolUsuario rol) {
        Usuario usuario = usuarioRepository.save(Usuario.builder()
                .nombreUsuario("user_fac_" + rol.name().toLowerCase())
                .clave(passwordEncoder.encode("ClaveDefinitiva123"))
                .rol(rol)
                .claveTemporal(false)
                .proveedorAuth(ProveedorAuth.LOCAL)
                .eliminado(false)
                .build());
        return jwtProvider.generarToken(usuario);
    }

    @Test
    @DisplayName("GET /facturas y GET /mi-cuenta/facturas devuelven listados paginados según rol")
    void listarFacturasYMiCuentaSegunRol() throws Exception {
        String tokenJefe = generarTokenParaRol(RolUsuario.JEFE);
        String tokenMinorista = generarTokenParaRol(RolUsuario.MINORISTA);

        mockMvc.perform(get("/facturas")
                        .header("Authorization", "Bearer " + tokenJefe)
                        .param("estado", "PENDIENTE_PAGO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(facturaInicial.id()))
                .andExpect(jsonPath("$.content[0].total").value(2000.0))
                .andExpect(jsonPath("$.content[0].saldo").value(2000.0));

        mockMvc.perform(get("/mi-cuenta/facturas")
                        .header("Authorization", "Bearer " + tokenMinorista))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(facturaInicial.id()));
    }

    @Test
    @DisplayName("GET /facturas/{id}/pdf y GET /facturas/{id}/qr-pago devuelven binarios con Content-Type esperado")
    void descargarPdfYQrPagoDeFacturaPendiente() throws Exception {
        String tokenMinorista = generarTokenParaRol(RolUsuario.MINORISTA);

        MvcResult pdfRes = mockMvc.perform(get("/facturas/" + facturaInicial.id() + "/pdf")
                        .header("Authorization", "Bearer " + tokenMinorista))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andReturn();
        assertTrue(pdfRes.getResponse().getContentAsByteArray().length > 50);

        MvcResult qrRes = mockMvc.perform(get("/facturas/" + facturaInicial.id() + "/qr-pago")
                        .header("Authorization", "Bearer " + tokenMinorista))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG))
                .andReturn();
        assertTrue(qrRes.getResponse().getContentAsByteArray().length > 50);
    }

    @Test
    @DisplayName("Ciclo de pagos: pagos parciales actualizan a PAGADA, bloquean anulación/QR y DELETE /pagos/{id} revierte a PENDIENTE_PAGO")
    void cicloCompletoPagosYAnulacion() throws Exception {
        String tokenJefe = generarTokenParaRol(RolUsuario.JEFE);

        // 1. Primer pago parcial ($800 de $2000)
        MvcResult pago1Res = mockMvc.perform(post("/facturas/" + facturaInicial.id() + "/pagos")
                        .header("Authorization", "Bearer " + tokenJefe)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new PagoRequest(formaDePagoInicial.id(), 800.0, "TRF-001", "Pago parcial 1")
                        )))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.monto").value(800.0))
                .andReturn();

        JsonNode pago1Json = objectMapper.readTree(pago1Res.getResponse().getContentAsString());
        String pago1Id = pago1Json.get("id").asText();

        // No se puede anular una factura que tiene pagos registrados -> 409
        mockMvc.perform(post("/facturas/" + facturaInicial.id() + "/anular")
                        .header("Authorization", "Bearer " + tokenJefe))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICTO"));

        // Pago que excede el saldo ($1200 restantes, se intenta pagar $1500) -> 409
        mockMvc.perform(post("/facturas/" + facturaInicial.id() + "/pagos")
                        .header("Authorization", "Bearer " + tokenJefe)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new PagoRequest(formaDePagoInicial.id(), 1500.0, "TRF-ERR", null)
                        )))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICTO"));

        // 2. Segundo pago por el saldo restante ($1200) -> Factura pasa a PAGADA
        MvcResult pago2Res = mockMvc.perform(post("/facturas/" + facturaInicial.id() + "/pagos")
                        .header("Authorization", "Bearer " + tokenJefe)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new PagoRequest(formaDePagoInicial.id(), 1200.0, "TRF-002", "Cancelación total")
                        )))
                .andExpect(status().isCreated())
                .andReturn();
        String pago2Id = objectMapper.readTree(pago2Res.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(get("/facturas/" + facturaInicial.id())
                        .header("Authorization", "Bearer " + tokenJefe))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("PAGADA"))
                .andExpect(jsonPath("$.totalPagado").value(2000.0))
                .andExpect(jsonPath("$.saldo").value(0.0))
                .andExpect(jsonPath("$.pagos", hasSize(2)));

        // QR de pago sobre factura PAGADA responde 409
        mockMvc.perform(get("/facturas/" + facturaInicial.id() + "/qr-pago")
                        .header("Authorization", "Bearer " + tokenJefe))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICTO"));

        // 3. Eliminar ambos pagos con rol JEFE -> revierte a PENDIENTE_PAGO y permite anular
        mockMvc.perform(delete("/pagos/" + pago2Id)
                        .header("Authorization", "Bearer " + tokenJefe))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/pagos/" + pago1Id)
                        .header("Authorization", "Bearer " + tokenJefe))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/facturas/" + facturaInicial.id() + "/anular")
                        .header("Authorization", "Bearer " + tokenJefe))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ANULADA"));
    }
}
