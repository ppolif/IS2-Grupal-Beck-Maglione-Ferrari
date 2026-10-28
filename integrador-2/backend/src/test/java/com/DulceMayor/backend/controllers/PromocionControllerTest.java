package com.DulceMayor.backend.controllers;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.DulceMayor.backend.config.JwtProvider;
import com.DulceMayor.backend.dtos.producto.PromocionRequest;
import com.DulceMayor.backend.entities.ProveedorAuth;
import com.DulceMayor.backend.entities.RolUsuario;
import com.DulceMayor.backend.entities.producto.Promocion;
import com.DulceMayor.backend.entities.Usuario;
import com.DulceMayor.backend.repositories.producto.PromocionRepository;
import com.DulceMayor.backend.repositories.UsuarioRepository;
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
class PromocionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Autowired
    private PromocionRepository promocionRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtProvider jwtProvider;

    private String tokenJefe;
    private String tokenPreventista;

    @BeforeEach
    void setUp() {
        promocionRepository.deleteAll();
        usuarioRepository.deleteAll();

        Usuario jefe = usuarioRepository.save(Usuario.builder()
                .nombreUsuario("jefe_promo")
                .clave(passwordEncoder.encode("clave12345"))
                .rol(RolUsuario.JEFE)
                .claveTemporal(false)
                .proveedorAuth(ProveedorAuth.LOCAL)
                .eliminado(false)
                .build());
        tokenJefe = jwtProvider.generarToken(jefe);

        Usuario prev = usuarioRepository.save(Usuario.builder()
                .nombreUsuario("prev_promo")
                .clave(passwordEncoder.encode("clave12345"))
                .rol(RolUsuario.PREVENTISTA)
                .claveTemporal(false)
                .proveedorAuth(ProveedorAuth.LOCAL)
                .eliminado(false)
                .build());
        tokenPreventista = jwtProvider.generarToken(prev);
    }

    @Test
    @DisplayName("POST /promociones crea promoción con código en mayúsculas")
    void crearPromocion() throws Exception {
        LocalDate hoy = LocalDate.now();
        PromocionRequest req = new PromocionRequest(
                "navidad2026",
                "Descuento navideño",
                20.0,
                5,
                null,
                hoy,
                hoy.plusDays(60)
        );

        mockMvc.perform(post("/promociones")
                        .header("Authorization", "Bearer " + tokenJefe)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo").value("NAVIDAD2026"))
                .andExpect(jsonPath("$.porcentajeDescuento").value(20.0));
    }

    @Test
    @DisplayName("GET /promociones/codigo/{codigo} accesible por PREVENTISTA y responde 200 OK si está vigente")
    void validarCodigoPromocionPreventista() throws Exception {
        LocalDate hoy = LocalDate.now();
        promocionRepository.save(Promocion.builder()
                .codigo("OFERTA10")
                .descripcion("Promo 10%")
                .porcentajeDescuento(10.0)
                .fechaDesde(hoy.minusDays(1))
                .fechaHasta(hoy.plusDays(10))
                .eliminado(false)
                .build());

        mockMvc.perform(get("/promociones/codigo/{codigo}", "oferta10")
                        .header("Authorization", "Bearer " + tokenPreventista))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value("OFERTA10"));
    }

    @Test
    @DisplayName("GET /promociones/codigo/{codigo} responde 404 si la promoción venció")
    void validarCodigoPromocionVencida() throws Exception {
        LocalDate hoy = LocalDate.now();
        promocionRepository.save(Promocion.builder()
                .codigo("EXPIRADA")
                .descripcion("Venció ayer")
                .porcentajeDescuento(10.0)
                .fechaDesde(hoy.minusDays(10))
                .fechaHasta(hoy.minusDays(1))
                .eliminado(false)
                .build());

        mockMvc.perform(get("/promociones/codigo/{codigo}", "EXPIRADA")
                        .header("Authorization", "Bearer " + tokenPreventista))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NO_ENCONTRADO"));
    }
}
