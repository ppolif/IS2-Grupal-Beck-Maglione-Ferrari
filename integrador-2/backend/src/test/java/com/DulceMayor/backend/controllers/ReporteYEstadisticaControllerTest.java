package com.DulceMayor.backend.controllers;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.DulceMayor.backend.config.JwtProvider;
import com.DulceMayor.backend.dtos.producto.ProductoRequest;
import com.DulceMayor.backend.entities.ProveedorAuth;
import com.DulceMayor.backend.entities.RolUsuario;
import com.DulceMayor.backend.entities.Usuario;
import com.DulceMayor.backend.entities.producto.Categoria;
import com.DulceMayor.backend.entities.producto.SubCategoria;
import com.DulceMayor.backend.repositories.UsuarioRepository;
import com.DulceMayor.backend.repositories.producto.CategoriaRepository;
import com.DulceMayor.backend.repositories.producto.ProductoRepository;
import com.DulceMayor.backend.repositories.producto.StockRepository;
import com.DulceMayor.backend.repositories.producto.SubCategoriaRepository;
import com.DulceMayor.backend.repositories.producto.VigenciaPrecioRepository;
import com.DulceMayor.backend.services.producto.ProductoService;
import com.DulceMayor.backend.services.reporte.exporter.ExcelReporteExporter;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Tests de integración con MockMvc para {@link com.DulceMayor.backend.controllers.estadistica.EstadisticaController},
 * {@link com.DulceMayor.backend.controllers.reporte.ReporteController} y {@code ReporteExporterFactory} (Patrón Factory Method).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReporteYEstadisticaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtProvider jwtProvider;

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

    @BeforeEach
    void setUp() {
        limpiarTablas();
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
    @DisplayName("Endpoints de /estadisticas responden 200 para JEFE y 403 para ADMINISTRATIVO")
    void estadisticasControlDeAccesoYRespuesta() throws Exception {
        Usuario jefe = crearUsuario("jefe_stats", RolUsuario.JEFE);
        Usuario admin = crearUsuario("admin_stats", RolUsuario.ADMINISTRATIVO);
        String tokenJefe = jwtProvider.generarToken(jefe);
        String tokenAdmin = jwtProvider.generarToken(admin);

        Categoria cat = categoriaRepository.save(Categoria.builder().nombre("Chocolates").eliminado(false).build());
        SubCategoria sub = subCategoriaRepository.save(
                SubCategoria.builder().nombre("Tabletas").categoria(cat).eliminado(false).build()
        );
        productoService.crear(new ProductoRequest(
                "CHOC-01", "Chocolate con Leche", "Tableta 100g", "Display x12", 12, false, null, sub.getId(), 8500.0, 35
        ));

        // 1. JEFE puede consultar estadísticas
        mockMvc.perform(get("/estadisticas/pedidos-por-preventor-por-dia")
                        .param("anio", "2026")
                        .param("mes", "11")
                        .header("Authorization", "Bearer " + tokenJefe))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.anio").value(2026))
                .andExpect(jsonPath("$.mes").value(11));

        mockMvc.perform(get("/estadisticas/stock-por-producto")
                        .header("Authorization", "Bearer " + tokenJefe))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].productoNombre").value("Chocolate con Leche"))
                .andExpect(jsonPath("$[0].cantidad").value(35));

        // 2. Mes inválido retorna 400 VALIDACION
        mockMvc.perform(get("/estadisticas/pedidos-por-preventor-por-dia")
                        .param("anio", "2026")
                        .param("mes", "13")
                        .header("Authorization", "Bearer " + tokenJefe))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDACION"));

        // 3. ADMINISTRATIVO recibe 403 en estadísticas exclusivas de JEFE
        mockMvc.perform(get("/estadisticas/stock-por-producto")
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Endpoints de /reportes exportan JSON, PDF y XLSX mediante ReporteExporterFactory (Factory Method)")
    void reportesExportacionJsonPdfYXlsx() throws Exception {
        Usuario jefe = crearUsuario("jefe_rep", RolUsuario.JEFE);
        Usuario admin = crearUsuario("admin_rep", RolUsuario.ADMINISTRATIVO);
        String tokenJefe = jwtProvider.generarToken(jefe);
        String tokenAdmin = jwtProvider.generarToken(admin);

        // 1. Reporte de facturación en JSON (permitido para ADMINISTRATIVO)
        mockMvc.perform(get("/reportes/facturacion")
                        .param("formato", "json")
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.titulo").value("Reporte de Montos Facturados"))
                .andExpect(jsonPath("$.columnas.length()", greaterThan(0)));

        // 2. Reporte de pedidos en PDF (verifica firma %PDF-1.4)
        MvcResult pdfRes = mockMvc.perform(get("/reportes/pedidos")
                        .param("formato", "pdf")
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"reporte-pedidos.pdf\""))
                .andReturn();

        String pdfHeader = new String(pdfRes.getResponse().getContentAsByteArray(), StandardCharsets.ISO_8859_1);
        assertTrue(pdfHeader.startsWith("%PDF-1.4"));

        // 3. Reporte de ventas histórico en XLSX para JEFE (verifica contenedor ZIP OpenXML "PK")
        MvcResult xlsxRes = mockMvc.perform(get("/reportes/ventas-historico")
                        .param("formato", "xlsx")
                        .header("Authorization", "Bearer " + tokenJefe))
                .andExpect(status().isOk())
                .andExpect(content().contentType(ExcelReporteExporter.CONTENT_TYPE_XLSX))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"reporte-ventas-historico.xlsx\""))
                .andReturn();

        byte[] xlsxBytes = xlsxRes.getResponse().getContentAsByteArray();
        assertTrue(xlsxBytes.length > 4 && xlsxBytes[0] == 'P' && xlsxBytes[1] == 'K');

        // 4. Formato xlsx no permitido en /reportes/facturacion devuelve 400
        mockMvc.perform(get("/reportes/facturacion")
                        .param("formato", "xlsx")
                        .header("Authorization", "Bearer " + tokenJefe))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDACION"));

        // 5. ADMINISTRATIVO no puede acceder a /reportes/ventas-historico (exclusivo JEFE -> 403)
        mockMvc.perform(get("/reportes/ventas-historico")
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isForbidden());
    }
}

