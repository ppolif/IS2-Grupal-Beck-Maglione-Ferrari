package com.DulceMayor.backend;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.DulceMayor.backend.config.DataInitializer;
import com.DulceMayor.backend.config.JwtProvider;
import com.DulceMayor.backend.dtos.CambiarClaveRequest;
import com.DulceMayor.backend.dtos.LoginRequest;
import com.DulceMayor.backend.entities.ProveedorAuth;
import com.DulceMayor.backend.entities.RolUsuario;
import com.DulceMayor.backend.entities.Usuario;
import com.DulceMayor.backend.repositories.UsuarioRepository;
import com.DulceMayor.backend.services.UsuarioService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

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

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        usuarioRepository.deleteAll();
    }

    @Test
    @DisplayName("Login correcto devuelve 200, token JWT y debeCambiarClave=true para usuario con clave temporal")
    void loginCorrectoConClaveTemporal() throws Exception {
        usuarioService.crearUsuarioConClavePorDefecto("jefe", RolUsuario.JEFE);

        LoginRequest request = new LoginRequest("jefe", Usuario.CLAVE_POR_DEFECTO);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.expiraEn").value(3600))
                .andExpect(jsonPath("$.debeCambiarClave").value(true))
                .andExpect(jsonPath("$.usuario.id").isNotEmpty())
                .andExpect(jsonPath("$.usuario.nombreUsuario").value("jefe"))
                .andExpect(jsonPath("$.usuario.rol").value("JEFE"))
                .andExpect(jsonPath("$.usuario.claveTemporal").value(true))
                .andExpect(jsonPath("$.usuario.proveedorAuth").value("LOCAL"))
                .andExpect(jsonPath("$.usuario.clave").doesNotExist());
    }

    @Test
    @DisplayName("Login con clave incorrecta, usuario inexistente y usuario eliminado devuelve 401 con el mismo code y mensaje")
    void loginFallidoUnificado() throws Exception {
        Usuario activo = usuarioService.crearUsuarioConClavePorDefecto("activo", RolUsuario.ADMINISTRATIVO);

        Usuario eliminado = usuarioService.crearUsuarioConClavePorDefecto("borrado", RolUsuario.PREVENTISTA);
        eliminado.setEliminado(true);
        usuarioRepository.save(eliminado);

        // 1. Clave incorrecta
        MvcResult resClaveIncorrecta = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(activo.getNombreUsuario(), "otraClave123"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.code").value("CREDENCIALES_INVALIDAS"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.path").value("/auth/login"))
                .andReturn();

        // 2. Usuario inexistente
        MvcResult resInexistente = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("noexiste", Usuario.CLAVE_POR_DEFECTO))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.code").value("CREDENCIALES_INVALIDAS"))
                .andReturn();

        // 3. Usuario eliminado
        MvcResult resEliminado = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("borrado", Usuario.CLAVE_POR_DEFECTO))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.code").value("CREDENCIALES_INVALIDAS"))
                .andReturn();

        JsonNode json1 = objectMapper.readTree(resClaveIncorrecta.getResponse().getContentAsString());
        JsonNode json2 = objectMapper.readTree(resInexistente.getResponse().getContentAsString());
        JsonNode json3 = objectMapper.readTree(resEliminado.getResponse().getContentAsString());

        assertThat(json1.get("code").asText()).isEqualTo("CREDENCIALES_INVALIDAS");
        assertThat(json2.get("code").asText()).isEqualTo(json1.get("code").asText());
        assertThat(json3.get("code").asText()).isEqualTo(json1.get("code").asText());
        assertThat(json2.get("message").asText()).isEqualTo(json1.get("message").asText());
        assertThat(json3.get("message").asText()).isEqualTo(json1.get("message").asText());
    }

    @Test
    @DisplayName("Acceso sin token, con token adulterado o con token vencido responde 401 con ErrorResponse")
    void accesoSinTokenOConTokenInvalidoOVencido() throws Exception {
        Usuario usuario = crearUsuarioActivoConClaveDefinitiva("jefe_token", "claveSegura123", RolUsuario.JEFE);
        String tokenValido = jwtProvider.generarToken(usuario);

        // 1. Sin token
        mockMvc.perform(get("/test/autenticado"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.code").isNotEmpty())
                .andExpect(jsonPath("$.path").value("/test/autenticado"));

        // 2. Token adulterado
        String tokenAdulterado = tokenValido.substring(0, tokenValido.length() - 4) + "xxxx";
        mockMvc.perform(get("/test/autenticado")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdulterado))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.code").value("NO_AUTENTICADO"));

        // 3. Token vencido
        String tokenVencido = Jwts.builder()
                .setSubject(usuario.getId())
                .claim("rol", usuario.getRol().name())
                .setIssuedAt(Date.from(Instant.now().minusSeconds(7200)))
                .setExpiration(Date.from(Instant.now().minusSeconds(3600)))
                .signWith(
                        Keys.hmacShaKeyFor(jwtSecret.trim().getBytes(StandardCharsets.UTF_8)),
                        SignatureAlgorithm.HS256
                )
                .compact();

        mockMvc.perform(get("/test/autenticado")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenVencido))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.code").value("NO_AUTENTICADO"));
    }

    @Test
    @DisplayName("Usuario con clave temporal solo puede acceder a GET /auth/perfil y POST /auth/cambiar-clave; el resto da 403 CAMBIO_CLAVE_REQUERIDO")
    void reglaDeClaveTemporal() throws Exception {
        Usuario usuario = usuarioService.crearUsuarioConClavePorDefecto("preventista", RolUsuario.PREVENTISTA);
        String token = jwtProvider.generarToken(usuario);

        // GET /auth/perfil permitido (200)
        mockMvc.perform(get("/auth/perfil")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuario.id").value(usuario.getId()))
                .andExpect(jsonPath("$.usuario.nombreUsuario").value("preventista"))
                .andExpect(jsonPath("$.usuario.claveTemporal").value(true))
                .andExpect(jsonPath("$.personaId").isEmpty())
                .andExpect(jsonPath("$.nombre").isEmpty())
                .andExpect(jsonPath("$.apellido").isEmpty());

        // Cualquier otro endpoint bloqueado con 403 CAMBIO_CLAVE_REQUERIDO
        mockMvc.perform(get("/test/autenticado")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.code").value("CAMBIO_CLAVE_REQUERIDO"))
                .andExpect(jsonPath("$.path").value("/test/autenticado"));
    }

    @Test
    @DisplayName("cambiar-clave valida reglas y al tener éxito devuelve 204 y el mismo token ya permite acceder a otros endpoints")
    void flujoCompletoCambiarClave() throws Exception {
        Usuario usuario = usuarioService.crearUsuarioConClavePorDefecto("jefe", RolUsuario.JEFE);
        String token = jwtProvider.generarToken(usuario);

        // 1. Rechaza clave actual incorrecta
        mockMvc.perform(post("/auth/cambiar-clave")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CambiarClaveRequest("claveErronea", "nuevaClave123", "nuevaClave123"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("VALIDACION"));

        // 2. Rechaza confirmación distinta
        mockMvc.perform(post("/auth/cambiar-clave")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CambiarClaveRequest(Usuario.CLAVE_POR_DEFECTO, "nuevaClave123", "otraConfirmacion"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("VALIDACION"));

        // 3. Rechaza menos de 8 caracteres
        mockMvc.perform(post("/auth/cambiar-clave")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CambiarClaveRequest(Usuario.CLAVE_POR_DEFECTO, "corta1", "corta1"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("VALIDACION"));

        // 4. Rechaza la clave por defecto "dulcemayor"
        mockMvc.perform(post("/auth/cambiar-clave")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CambiarClaveRequest(
                                        Usuario.CLAVE_POR_DEFECTO,
                                        Usuario.CLAVE_POR_DEFECTO,
                                        Usuario.CLAVE_POR_DEFECTO))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("VALIDACION"));

        // 5. Cambio válido devuelve 204
        mockMvc.perform(post("/auth/cambiar-clave")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CambiarClaveRequest(Usuario.CLAVE_POR_DEFECTO, "miNuevaClaveSegura", "miNuevaClaveSegura"))))
                .andExpect(status().isNoContent());

        // 6. El mismo token (sin re-loguear) ahora accede a endpoints normales porque el filtro consulta la BD
        mockMvc.perform(get("/test/solo-jefe")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.acceso").value("JEFE"));
    }

    @Test
    @DisplayName("Autorización por roles: verifica 200 y 403 según cada rol en el controlador de prueba")
    void autorizacionPorRoles() throws Exception {
        String tokenJefe = jwtProvider.generarToken(
                crearUsuarioActivoConClaveDefinitiva("u_jefe", "claveSegura123", RolUsuario.JEFE));
        String tokenAdmin = jwtProvider.generarToken(
                crearUsuarioActivoConClaveDefinitiva("u_admin", "claveSegura123", RolUsuario.ADMINISTRATIVO));
        String tokenPreventista = jwtProvider.generarToken(
                crearUsuarioActivoConClaveDefinitiva("u_prev", "claveSegura123", RolUsuario.PREVENTISTA));
        String tokenDistribuidor = jwtProvider.generarToken(
                crearUsuarioActivoConClaveDefinitiva("u_dist", "claveSegura123", RolUsuario.DISTRIBUIDOR));
        String tokenMinorista = jwtProvider.generarToken(
                crearUsuarioActivoConClaveDefinitiva("u_min", "claveSegura123", RolUsuario.MINORISTA));

        // Endpoint solo JEFE
        mockMvc.perform(get("/test/solo-jefe").header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenJefe))
                .andExpect(status().isOk());
        mockMvc.perform(get("/test/solo-jefe").header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESO_DENEGADO"));
        mockMvc.perform(get("/test/solo-jefe").header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenPreventista))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/test/solo-jefe").header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenDistribuidor))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/test/solo-jefe").header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenMinorista))
                .andExpect(status().isForbidden());

        // Endpoint JEFE o ADMINISTRATIVO
        mockMvc.perform(get("/test/jefe-o-admin").header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenJefe))
                .andExpect(status().isOk());
        mockMvc.perform(get("/test/jefe-o-admin").header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin))
                .andExpect(status().isOk());
        mockMvc.perform(get("/test/jefe-o-admin").header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenPreventista))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESO_DENEGADO"));
        mockMvc.perform(get("/test/jefe-o-admin").header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenDistribuidor))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/test/jefe-o-admin").header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenMinorista))
                .andExpect(status().isForbidden());

        // Endpoint cualquier autenticado
        for (String token : new String[]{tokenJefe, tokenAdmin, tokenPreventista, tokenDistribuidor, tokenMinorista}) {
            mockMvc.perform(get("/test/autenticado").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                    .andExpect(status().isOk());
        }
    }

    @Test
    @DisplayName("CORS: preflight desde origen permitido responde OK y desde origen no permitido es rechazado")
    void corsPreflightOrigenPermitidoYNoPermitido() throws Exception {
        // Origen permitido (http://localhost:3000)
        mockMvc.perform(options("/auth/login")
                        .header(HttpHeaders.ORIGIN, "http://localhost:3000")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Authorization,Content-Type"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:3000"));

        // Origen NO permitido (http://sitio-malicioso.com)
        mockMvc.perform(options("/auth/login")
                        .header(HttpHeaders.ORIGIN, "http://sitio-malicioso.com")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    @Test
    @DisplayName("La clave guardada en la base es siempre un hash BCrypt y nunca el texto plano; el seed es idempotente")
    void claveEncriptadaConBCryptYSeedIdempotente() {
        dataInitializer.ejecutarSeed();
        dataInitializer.ejecutarSeed(); // Segunda ejecución para verificar idempotencia

        assertThat(usuarioRepository.count()).isEqualTo(5);

        for (String nombre : new String[]{"jefe", "administrativo", "preventista", "distribuidor", "minorista"}) {
            Usuario u = usuarioRepository.findByNombreUsuario(nombre).orElseThrow();
            assertThat(u.getClave()).isNotEqualTo(Usuario.CLAVE_POR_DEFECTO);
            assertThat(u.getClave()).startsWith("$2");
            assertThat(passwordEncoder.matches(Usuario.CLAVE_POR_DEFECTO, u.getClave())).isTrue();
            assertThat(u.isClaveTemporal()).isTrue();
            assertThat(u.getProveedorAuth()).isEqualTo(ProveedorAuth.LOCAL);
            assertThat(u.isEliminado()).isFalse();
            assertThat(u.toString()).doesNotContain(u.getClave());
        }
    }

    @Test
    @DisplayName("Validación de secreto JWT al arrancar y manejo uniforme de errores 404, 409 y 500 sin filtrar detalles")
    void validacionSecretoJwtYManejoDeErrores() throws Exception {
        assertThatThrownBy(() -> new JwtProvider("", 60))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> new JwtProvider("secreto_demasiado_corto", 60))
                .isInstanceOf(IllegalStateException.class);

        String tokenJefe = jwtProvider.generarToken(
                crearUsuarioActivoConClaveDefinitiva("jefe_err", "claveSegura123", RolUsuario.JEFE));

        // Ruta pública sin token responde 200
        mockMvc.perform(get("/publico/test-catalogo"))
                .andExpect(status().isOk());

        // 404 NO_ENCONTRADO
        mockMvc.perform(get("/test/no-encontrado")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenJefe))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("NO_ENCONTRADO"));

        // 409 CONFLICTO
        mockMvc.perform(get("/test/conflicto")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenJefe))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.code").value("TRANSICION_INVALIDA"));

        // 500 ERROR_INTERNO sin filtrar stack trace ni mensaje interno
        MvcResult res500 = mockMvc.perform(get("/test/error-interno")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenJefe))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.code").value("ERROR_INTERNO"))
                .andReturn();

        String body500 = res500.getResponse().getContentAsString();
        assertThat(body500).doesNotContain("Detalle sensible interno de SQL");
        assertThat(body500).doesNotContain("RuntimeException");
    }

    private Usuario crearUsuarioActivoConClaveDefinitiva(String nombreUsuario, String clavePlana, RolUsuario rol) {
        Usuario usuario = Usuario.builder()
                .nombreUsuario(nombreUsuario)
                .clave(passwordEncoder.encode(clavePlana))
                .rol(rol)
                .claveTemporal(false)
                .proveedorAuth(ProveedorAuth.LOCAL)
                .eliminado(false)
                .build();
        return usuarioRepository.save(usuario);
    }
}

