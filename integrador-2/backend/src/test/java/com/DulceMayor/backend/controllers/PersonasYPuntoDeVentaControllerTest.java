package com.DulceMayor.backend.controllers;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.DulceMayor.backend.config.JwtProvider;
import com.DulceMayor.backend.dtos.CambiarClaveRequest;
import com.DulceMayor.backend.dtos.ContactoRequest;
import com.DulceMayor.backend.dtos.direccion.DireccionRequest;
import com.DulceMayor.backend.dtos.persona.ActualizarPerfilMinoristaRequest;
import com.DulceMayor.backend.dtos.persona.EmpleadoRequest;
import com.DulceMayor.backend.dtos.persona.MinoristaRequest;
import com.DulceMayor.backend.dtos.persona.PuntoDeVentaRequest;
import com.DulceMayor.backend.dtos.persona.RegistroMinoristaRequest;
import com.DulceMayor.backend.dtos.persona.RolEmpleado;
import com.DulceMayor.backend.entities.ProveedorAuth;
import com.DulceMayor.backend.entities.RolUsuario;
import com.DulceMayor.backend.entities.Usuario;
import com.DulceMayor.backend.entities.contacto.TipoContacto;
import com.DulceMayor.backend.entities.direccion.Departamento;
import com.DulceMayor.backend.entities.direccion.Localidad;
import com.DulceMayor.backend.entities.direccion.Pais;
import com.DulceMayor.backend.entities.direccion.Provincia;
import com.DulceMayor.backend.entities.persona.TipoDocumento;
import com.DulceMayor.backend.repositories.UsuarioRepository;
import com.DulceMayor.backend.repositories.direccion.DepartamentoRepository;
import com.DulceMayor.backend.repositories.direccion.DireccionRepository;
import com.DulceMayor.backend.repositories.direccion.LocalidadRepository;
import com.DulceMayor.backend.repositories.direccion.PaisRepository;
import com.DulceMayor.backend.repositories.direccion.ProvinciaRepository;
import com.DulceMayor.backend.repositories.persona.EmpleadoRepository;
import com.DulceMayor.backend.repositories.persona.MinoristaRepository;
import com.DulceMayor.backend.repositories.persona.PersonaRepository;
import com.DulceMayor.backend.repositories.persona.PuntoDeVentaRepository;
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
 * Tests de integración con MockMvc para los controladores de Empleado, Minorista, PuntoDeVenta y Auth.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PersonasYPuntoDeVentaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private PuntoDeVentaRepository puntoDeVentaRepository;

    @Autowired
    private EmpleadoRepository empleadoRepository;

    @Autowired
    private MinoristaRepository minoristaRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private DireccionRepository direccionRepository;

    @Autowired
    private LocalidadRepository localidadRepository;

    @Autowired
    private DepartamentoRepository departamentoRepository;

    @Autowired
    private ProvinciaRepository provinciaRepository;

    @Autowired
    private PaisRepository paisRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtProvider jwtProvider;

    private Localidad localidadInicial;

    @BeforeEach
    void setUp() {
        limpiarTablas();

        Pais pais = paisRepository.save(Pais.builder().nombre("Argentina").eliminado(false).build());
        Provincia prov = provinciaRepository.save(Provincia.builder().nombre("Mendoza").pais(pais).eliminado(false).build());
        Departamento depto = departamentoRepository.save(Departamento.builder().nombre("Capital").provincia(prov).eliminado(false).build());
        localidadInicial = localidadRepository.save(Localidad.builder().nombre("Ciudad").codigoPostal("5500").departamento(depto).eliminado(false).build());
    }

    @AfterEach
    void tearDown() {
        limpiarTablas();
    }

    private void limpiarTablas() {
        puntoDeVentaRepository.deleteAll();
        empleadoRepository.deleteAll();
        minoristaRepository.deleteAll();
        personaRepository.deleteAll();
        direccionRepository.deleteAll();
        localidadRepository.deleteAll();
        departamentoRepository.deleteAll();
        provinciaRepository.deleteAll();
        paisRepository.deleteAll();
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
    @DisplayName("Flujo completo: crear Empleado PREVENTISTA -> alta de Minorista por preventor -> PuntoDeVenta -> auto-registro en portal y perfil")
    void flujoCompletoPersonasYPuntosDeVenta() throws Exception {
        Usuario jefe = crearUsuario("jefe_general", RolUsuario.JEFE);
        String tokenJefe = jwtProvider.generarToken(jefe);

        // 1. Jefe da de alta un empleado PREVENTISTA
        EmpleadoRequest empReq = new EmpleadoRequest(
                "Esteban",
                "Quito",
                null,
                TipoDocumento.DNI,
                "33111222",
                List.of(),
                List.of(),
                "LEG-PREV-88",
                "equito",
                RolEmpleado.PREVENTISTA
        );

        MvcResult empRes = mockMvc.perform(post("/empleados")
                        .header("Authorization", "Bearer " + tokenJefe)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(empReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.legajo").value("LEG-PREV-88"))
                .andExpect(jsonPath("$.usuario.claveTemporal").value(true))
                .andReturn();

        JsonNode empJson = objectMapper.readTree(empRes.getResponse().getContentAsString());
        String preventorId = empJson.get("id").asText();

        // 2. Preventista ingresa con clave por defecto y actualiza contraseña
        Usuario preventistaUsuario = usuarioRepository.findByNombreUsuario("equito").orElseThrow();
        String tokenPreventistaTemp = jwtProvider.generarToken(preventistaUsuario);

        mockMvc.perform(post("/auth/cambiar-clave")
                        .header("Authorization", "Bearer " + tokenPreventistaTemp)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CambiarClaveRequest(
                                "dulcemayor", "NuevaClave123", "NuevaClave123"
                        ))))
                .andExpect(status().isNoContent());

        Usuario preventistaActualizado = usuarioRepository.findByNombreUsuario("equito").orElseThrow();
        assertFalse(preventistaActualizado.isClaveTemporal());
        String tokenPreventista = jwtProvider.generarToken(preventistaActualizado);

        // 3. Preventista registra un Minorista en terreno con contacto de correo
        ContactoRequest contactoCorreo = new ContactoRequest(
                "CORREO", TipoContacto.LABORAL, "Contacto Kiosco", "kiosco_sanmartin@gmail.com", null, null
        );

        MinoristaRequest minReq = new MinoristaRequest(
                "Roberto",
                "Sanchez",
                null,
                TipoDocumento.DNI,
                "25666777",
                List.of(),
                List.of(contactoCorreo)
        );

        MvcResult minRes = mockMvc.perform(post("/minoristas")
                        .header("Authorization", "Bearer " + tokenPreventista)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(minReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tieneUsuario").value(true))
                .andReturn();

        String minoristaId = objectMapper.readTree(minRes.getResponse().getContentAsString()).get("id").asText();

        // 4. Preventista registra un Punto de Venta para ese minorista
        DireccionRequest dirReq = new DireccionRequest(
                "Av. San Martín", "500", "Centro", null, null, "Frente a plaza", -32.889, -68.845, localidadInicial.getId()
        );
        PuntoDeVentaRequest pdvReq = new PuntoDeVentaRequest(
                "Kiosco Central San Martín", minoristaId, dirReq, null
        );

        MvcResult pdvRes = mockMvc.perform(post("/puntos-de-venta")
                        .header("Authorization", "Bearer " + tokenPreventista)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pdvReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombreComercio").value("Kiosco Central San Martín"))
                .andExpect(jsonPath("$.preventor.id").value(preventorId))
                .andReturn();

        // 5. Auto-registro público de un nuevo cliente minorista en portal (POST /auth/registro - Caso C)
        RegistroMinoristaRequest regReq = new RegistroMinoristaRequest(
                "Laura",
                "Fernandez",
                TipoDocumento.DNI,
                "37888999",
                "laura@portal.com",
                "ClaveLaura123",
                "Minimarket Laura"
        );

        MvcResult regRes = mockMvc.perform(post("/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.debeCambiarClave").value(false))
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        String tokenLaura = objectMapper.readTree(regRes.getResponse().getContentAsString()).get("token").asText();

        // 6. Laura consulta su perfil (GET /auth/perfil)
        mockMvc.perform(get("/auth/perfil")
                        .header("Authorization", "Bearer " + tokenLaura))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Laura"))
                .andExpect(jsonPath("$.apellido").value("Fernandez"))
                .andExpect(jsonPath("$.personaId").exists())
                .andExpect(jsonPath("$.usuario.nombreUsuario").value("laura@portal.com"));

        // 7. Laura consulta su perfil completo de minorista (GET /api/v1/mi-cuenta/perfil)
        mockMvc.perform(get("/api/v1/mi-cuenta/perfil")
                        .header("Authorization", "Bearer " + tokenLaura))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Laura"))
                .andExpect(jsonPath("$.numeroDocumento").value("37888999"));

        // 8. Laura actualiza sus datos y modifica su mail de ingreso (PUT /api/v1/mi-cuenta/perfil)
        ActualizarPerfilMinoristaRequest actReq = new ActualizarPerfilMinoristaRequest(
                "Laura Beatriz",
                "Fernandez",
                null,
                TipoDocumento.DNI,
                "37888999",
                "laura.nueva@portal.com",
                "2615559988"
        );

        mockMvc.perform(put("/api/v1/mi-cuenta/perfil")
                        .header("Authorization", "Bearer " + tokenLaura)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(actReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Laura Beatriz"));

        // 9. Laura se da de baja del sistema (DELETE /api/v1/mi-cuenta/perfil)
        mockMvc.perform(delete("/api/v1/mi-cuenta/perfil")
                        .header("Authorization", "Bearer " + tokenLaura))
                .andExpect(status().isNoContent());

        // 10. El token ya no permite operar (401 NO_AUTENTICADO)
        mockMvc.perform(get("/api/v1/mi-cuenta/perfil")
                        .header("Authorization", "Bearer " + tokenLaura))
                .andExpect(status().isUnauthorized());
    }
}

