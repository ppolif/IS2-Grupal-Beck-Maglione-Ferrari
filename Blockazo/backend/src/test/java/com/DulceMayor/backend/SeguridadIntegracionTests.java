package com.DulceMayor.backend;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.DulceMayor.backend.config.DataInitializer;
import com.DulceMayor.backend.config.JwtProvider;
import com.DulceMayor.backend.dtos.CambiarClaveRequest;
import com.DulceMayor.backend.dtos.LoginRequest;
import com.DulceMayor.backend.entities.ProveedorAuth;
import com.DulceMayor.backend.entities.Usuario;
import com.DulceMayor.backend.repositories.UsuarioRepository;
import com.DulceMayor.backend.services.UsuarioService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Date;
import java.util.UUID;
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
 * Suite integral de pruebas de seguridad que valida los requisitos de seguridad.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SeguridadIntegracionTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private DataInitializer dataInitializer;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        usuarioRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        usuarioRepository.deleteAll();
    }

    @Test
    @DisplayName("Seed inicial idempotente: al ejecutar dos veces no duplica usuarios")
    void seedInicialIdempotente() {
        dataInitializer.ejecutarSeed();
        long countPrimera = usuarioRepository.count();

        dataInitializer.ejecutarSeed();
        long countSegunda = usuarioRepository.count();

        assertThat(countPrimera).isGreaterThan(0);
        assertThat(countSegunda).isEqualTo(countPrimera);
    }

    @Test
    @DisplayName("Login correcto devuelve 200, token JWT y debeCambiarClave=true para usuario con clave temporal")
    void loginCorrectoConClaveTemporal() throws Exception {
        usuarioService.crearUsuarioConClavePorDefecto("usuario1");

        LoginRequest request = new LoginRequest("usuario1", Usuario.CLAVE_POR_DEFECTO);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.expiraEn").isNumber())
                .andExpect(jsonPath("$.debeCambiarClave").value(true))
                .andExpect(jsonPath("$.usuario.id").isString())
                .andExpect(jsonPath("$.usuario.nombreUsuario").value("usuario1"))
                .andExpect(jsonPath("$.usuario.claveTemporal").value(true))
                .andExpect(jsonPath("$.usuario.clave").doesNotExist());
    }

    @Test
    @DisplayName("Login con clave incorrecta, usuario inexistente y usuario eliminado devuelve 401 con el mismo code y mensaje")
    void loginFallidoUnificado() throws Exception {
        Usuario activo = usuarioService.crearUsuarioConClavePorDefecto("activo");

        Usuario eliminado = usuarioService.crearUsuarioConClavePorDefecto("borrado");
        eliminado.setEliminado(true);
        usuarioRepository.save(eliminado);

        // 1. Clave incorrecta
        MvcResult resClaveIncorrecta = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("activo", "claveEquivocada"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.timestamp").isString())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.code").value("CREDENCIALES_INVALIDAS"))
                .andExpect(jsonPath("$.message").value("Usuario o clave inválidos"))
                .andExpect(jsonPath("$.path").value("/auth/login"))
                .andReturn();

        // 2. Usuario inexistente
        MvcResult resInexistente = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("inexistente", "cualquiera"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("CREDENCIALES_INVALIDAS"))
                .andExpect(jsonPath("$.message").value("Usuario o clave inválidos"))
                .andReturn();

        // 3. Usuario eliminado lógicamente
        MvcResult resEliminado = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("borrado", Usuario.CLAVE_POR_DEFECTO))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("CREDENCIALES_INVALIDAS"))
                .andExpect(jsonPath("$.message").value("Usuario o clave inválidos"))
                .andReturn();

        JsonNode jsonClaveIncorrecta = objectMapper.readTree(resClaveIncorrecta.getResponse().getContentAsString());
        JsonNode jsonInexistente = objectMapper.readTree(resInexistente.getResponse().getContentAsString());
        JsonNode jsonEliminado = objectMapper.readTree(resEliminado.getResponse().getContentAsString());

        assertThat(jsonClaveIncorrecta.get("code").asText())
                .isEqualTo(jsonInexistente.get("code").asText())
                .isEqualTo(jsonEliminado.get("code").asText());

        assertThat(jsonClaveIncorrecta.get("message").asText())
                .isEqualTo(jsonInexistente.get("message").asText())
                .isEqualTo(jsonEliminado.get("message").asText());
    }

    @Test
    @DisplayName("Acceso sin token, con token adulterado o con token vencido responde 401 con ErrorResponse")
    void accesoSinTokenOConTokenInvalidoOVencido() throws Exception {
        Usuario usuario = crearUsuarioActivoConClaveDefinitiva("user_token", "claveSegura123");
        String tokenValido = jwtProvider.generarToken(usuario);

        // 1. Sin token
        mockMvc.perform(get("/test/autenticado"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("NO_AUTENTICADO"))
                .andExpect(jsonPath("$.message").value("Autenticación requerida para acceder a este recurso"))
                .andExpect(jsonPath("$.status").value(401));

        // 2. Token adulterado (firma inválida)
        String tokenAdulterado = tokenValido.substring(0, tokenValido.length() - 4) + "XXXX";
        mockMvc.perform(get("/test/autenticado")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdulterado))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("NO_AUTENTICADO"));

        // 3. Token vencido
        io.jsonwebtoken.security.Keys.hmacShaKeyFor(
                "secreto_super_seguro_para_pruebas_de_integracion_123456".getBytes());
        String tokenVencido = io.jsonwebtoken.Jwts.builder()
                .setSubject("user_token")
                .claim("userId", usuario.getId())
                .setIssuedAt(new Date(System.currentTimeMillis() - 120_000))
                .setExpiration(new Date(System.currentTimeMillis() - 60_000))
                .signWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(
                        "secreto_super_seguro_para_pruebas_de_integracion_123456".getBytes()))
                .compact();

        mockMvc.perform(get("/test/autenticado")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenVencido))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("NO_AUTENTICADO"));
    }

    @Test
    @DisplayName("Usuario con clave temporal solo puede acceder a GET /auth/perfil y POST /auth/cambiar-clave; el resto da 403 CAMBIO_CLAVE_REQUERIDO")
    void reglaDeClaveTemporal() throws Exception {
        Usuario usuario = usuarioService.crearUsuarioConClavePorDefecto("tempuser");
        String token = jwtProvider.generarToken(usuario);

        // GET /auth/perfil permitido (200)
        mockMvc.perform(get("/auth/perfil")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuario.nombreUsuario").value("tempuser"));

        // POST /auth/cambiar-clave permitido (con body inválido responde 400 por validación, pero NO 403)
        mockMvc.perform(post("/auth/cambiar-clave")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDACION"));

        // Cualquier otro endpoint protegido responde 403 CAMBIO_CLAVE_REQUERIDO
        mockMvc.perform(get("/test/autenticado")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("CAMBIO_CLAVE_REQUERIDO"))
                .andExpect(jsonPath("$.message").value("Debe modificar su clave temporal antes de utilizar el sistema"));
    }

    @Test
    @DisplayName("cambiar-clave valida reglas y al tener éxito devuelve 204 y el mismo token ya permite acceder a otros endpoints")
    void flujoCompletoCambiarClave() throws Exception {
        Usuario usuario = usuarioService.crearUsuarioConClavePorDefecto("cambio_user");
        String token = jwtProvider.generarToken(usuario);

        // 1. Rechaza clave actual incorrecta
        mockMvc.perform(post("/auth/cambiar-clave")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CambiarClaveRequest("claveMala", "nuevaClave123", "nuevaClave123"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDACION"))
                .andExpect(jsonPath("$.message").value("La clave actual no coincide"));

        // 2. Rechaza clave nueva menor a 8 caracteres
        mockMvc.perform(post("/auth/cambiar-clave")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CambiarClaveRequest(Usuario.CLAVE_POR_DEFECTO, "corta", "corta"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDACION"));

        // 3. Rechaza que la nueva clave sea igual a la clave por defecto
        mockMvc.perform(post("/auth/cambiar-clave")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CambiarClaveRequest(Usuario.CLAVE_POR_DEFECTO, Usuario.CLAVE_POR_DEFECTO, Usuario.CLAVE_POR_DEFECTO))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDACION"))
                .andExpect(jsonPath("$.message").value("La nueva clave no puede ser la clave por defecto"));

        // 4. Rechaza si nueva y confirmación no coinciden
        mockMvc.perform(post("/auth/cambiar-clave")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CambiarClaveRequest(Usuario.CLAVE_POR_DEFECTO, "miNuevaClave123", "otraCosaDiferente"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDACION"))
                .andExpect(jsonPath("$.message").value("La nueva clave y la confirmación no coinciden"));

        // 5. Éxito: actualiza y devuelve 204
        mockMvc.perform(post("/auth/cambiar-clave")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CambiarClaveRequest(Usuario.CLAVE_POR_DEFECTO, "miNuevaClaveSegura", "miNuevaClaveSegura"))))
                .andExpect(status().isNoContent());

        // 6. El mismo token (sin re-loguear) ahora accede a endpoints normales
        mockMvc.perform(get("/test/autenticado")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.acceso").value("AUTENTICADO"));
    }

    @Test
    @DisplayName("Autorización: verifica 200 para usuario autenticado en el endpoint de prueba")
    void autorizacionAutenticado() throws Exception {
        String tokenUser = jwtProvider.generarToken(
                crearUsuarioActivoConClaveDefinitiva("u_test", "claveSegura123"));

        mockMvc.perform(get("/test/autenticado").header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.acceso").value("AUTENTICADO"));

        mockMvc.perform(get("/test/autenticado"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("CORS: preflight OPTIONS responde headers correctos según origen configurado")
    void corsPreflightConfigurado() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options("/auth/login")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"))
                .andExpect(header().string("Access-Control-Allow-Methods", containsString("POST")));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options("/auth/login")
                        .header("Origin", "http://sitio-malicioso.com")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }

    @Test
    @DisplayName("Errores uniformes: 404, 409 y 500 respetan estructura ErrorResponse y no filtran datos sensibles")
    void formatoUniformeDeErrores() throws Exception {
        assertThatThrownBy(() -> new JwtProvider(null, 60))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> new JwtProvider("secreto_demasiado_corto", 60))
                .isInstanceOf(IllegalStateException.class);

        String tokenUser = jwtProvider.generarToken(
                crearUsuarioActivoConClaveDefinitiva("user_err", "claveSegura123"));

        // Ruta pública sin token responde 200
        mockMvc.perform(get("/publico/test-catalogo"))
                .andExpect(status().isOk());

        // 404 Recurso No Encontrado
        mockMvc.perform(get("/test/no-encontrado").header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenUser))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("NO_ENCONTRADO"))
                .andExpect(jsonPath("$.message").value("Elemento de prueba inexistente"));

        // 409 Conflicto
        mockMvc.perform(get("/test/conflicto").header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenUser))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.code").value("TRANSICION_INVALIDA"))
                .andExpect(jsonPath("$.message").value("No se permite el cambio de estado solicitado"));

        // 500 Error Interno (no debe exponer stack trace ni SQL interno)
        MvcResult res500 = mockMvc.perform(get("/test/error-interno").header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenUser))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.code").value("ERROR_INTERNO"))
                .andExpect(jsonPath("$.message").value("Ha ocurrido un error interno en el servidor"))
                .andReturn();

        String body500 = res500.getResponse().getContentAsString();
        assertThat(body500).doesNotContain("Detalle sensible interno de SQL");
        assertThat(body500).doesNotContain("RuntimeException");
    }

    private Usuario crearUsuarioActivoConClaveDefinitiva(String nombreUsuario, String clavePlana) {
        Usuario usuario = Usuario.builder()
                .nombreUsuario(nombreUsuario)
                .clave(passwordEncoder.encode(clavePlana))
                .claveTemporal(false)
                .proveedorAuth(ProveedorAuth.LOCAL)
                .eliminado(false)
                .build();
        return usuarioRepository.save(usuario);
    }
}
