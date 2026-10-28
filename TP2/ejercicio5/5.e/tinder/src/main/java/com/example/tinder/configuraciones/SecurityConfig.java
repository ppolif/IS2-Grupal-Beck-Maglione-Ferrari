package com.example.tinder.configuraciones;

import com.example.tinder.entidades.Usuario;
import com.example.tinder.jwt.JwtAuthenticationFilter;
import com.example.tinder.jwt.JwtService;
import com.example.tinder.repositorios.UsuarioRepositorio;
import com.example.tinder.servicios.UsuarioServicio;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    // NUEVO: Traemos desde application.properties el identificador de nuestra API en Auth0 (https://api.tinder.com)
    @Value("${auth0.audience}")
    private String audience;

    // NUEVO: Traemos la URL de nuestro servidor de autorizacion de Auth0 (Issuer)
    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuer;

    // NUEVO: Repositorio interno de Spring que guarda la configuracion del cliente OAuth2 (client-id, secret, etc.)
    @Autowired
    private ClientRegistrationRepository clientRegistrationRepository;

    // NUEVO: Inyectamos UsuarioServicio y UsuarioRepositorio para poder vincular el usuario que se loguea en Auth0
    // con nuestra tabla de usuarios en MySQL y cargarle su rol y su sesion ("usuariosession")
    @Autowired
    private UsuarioServicio usuarioServicio;

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private JwtService jwtService;

    // Encriptador de contraseñas
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Administrador de autenticación
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    // Cadena de filtros y permisos (ahora soporta OAuth2 Cliente, OAuth2 Servidor de Recursos y el login local)
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // NUEVO: Habilitamos CORS en Spring Security usando el bean corsConfigurationSource() de mas abajo
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth

                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()

                        // Recursos estáticos públicos (CSS, JS, imágenes)
                        .requestMatchers("/css/**", "/js/**", "/img/**", "/imagenes/**", "/styles.css", "/*.css", "/vendor/**", "/favicon.ico").permitAll()

                        // Vistas públicas
                        .requestMatchers(
                                "/",
                                "/index",
                                "/index.html",
                                "/registro",
                                "/registro.html",
                                "/login",
                                "/login.html",
                                "/error",
                                "/error.html"
                        ).permitAll()

                        // Rutas de procesamiento de registro y fotos públicas
                        .requestMatchers("/registro/guardar", "/usuario/registrar", "/registrar", "/foto/**").permitAll()

                        // Rutas exclusivas de administrador
                        .requestMatchers("/admin/*").hasRole("ADMIN")

                        // NUEVO: Protegemos los endpoints REST (/api/v1/**) para que pidan estar autenticado (por sesion OAuth2 o por Bearer Token JWT)
                        .requestMatchers("/api/v1/**").authenticated()

                        // Cualquier otra petición requiere autenticación
                        .anyRequest().authenticated()
                )

                // 1. NUEVO: SEGURIDAD CLIENTE OAUTH2 (Login con Auth0 desde el navegador)
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        // Usamos nuestro resolver personalizado para agregar "?audience=https://api.tinder.com" cuando redirige a Auth0
                        .authorizationEndpoint(authorization -> authorization
                                .authorizationRequestResolver(
                                        authorizationRequestResolver(this.clientRegistrationRepository)
                                )
                        )
                        // Cuando el usuario termina de loguearse en Auth0 con exito, ejecutamos este handler:
                        // Esto fue necesario porque toda la app de Tinder (controladores y vistas Thymeleaf) espera que
                        // exista el atributo "usuariosession" en la HttpSession y que el usuario tenga el rol ROLE_USER o ROLE_ADMIN.
                        .successHandler((request, response, authentication) -> {
                            if (authentication.getPrincipal() instanceof OidcUser oidcUser) {
                                String email = oidcUser.getEmail();
                                if (email == null || email.isBlank()) {
                                    email = oidcUser.getPreferredUsername();
                                }

                                // Buscamos al usuario por mail en nuestra BD MySQL; si es la primera vez que entra con Auth0, lo creamos automaticamente
                                Usuario usuario = usuarioServicio.obtenerOCrearUsuarioOAuth2(
                                        email,
                                        oidcUser.getFullName(),
                                        oidcUser.getGivenName(),
                                        oidcUser.getFamilyName()
                                );

                                if (usuario != null) {
                                    // Guardamos el objeto Usuario en la sesion igual que hacia el login tradicional
                                    HttpSession session = request.getSession(true);
                                    session.setAttribute("usuariosession", usuario);

                                    // Le agregamos el rol local (ej. ROLE_USER) a las autoridades de OAuth2 para que funcionen los @PreAuthorize
                                    List<GrantedAuthority> updatedAuthorities = new ArrayList<>(authentication.getAuthorities());
                                    updatedAuthorities.add(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().toString()));

                                    if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
                                        OAuth2AuthenticationToken newAuth = new OAuth2AuthenticationToken(
                                                oidcUser,
                                                updatedAuthorities,
                                                oauthToken.getAuthorizedClientRegistrationId()
                                        );
                                        SecurityContextHolder.getContext().setAuthentication(newAuth);
                                        session.setAttribute(
                                                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                                                SecurityContextHolder.getContext()
                                        );
                                    }
                                }
                            }
                            response.sendRedirect("/inicio");
                        })
                        // Si Auth0 devuelve algun error, lo mandamos por parametro para mostrar el motivo real en login.html
                        .failureHandler((request, response, exception) -> {
                            String msg = exception.getMessage();
                            if (exception instanceof OAuth2AuthenticationException oauthEx && oauthEx.getError().getDescription() != null) {
                                msg = oauthEx.getError().getDescription();
                            }
                            response.sendRedirect("/login?oauthError=" + URLEncoder.encode(msg, StandardCharsets.UTF_8));
                        })
                )

                // 2. NUEVO: SEGURIDAD SERVIDOR DE RECURSOS OAUTH2 (Resource Server)
                // Valida los tokens JWT que llegan en el header "Authorization: Bearer <token>" (por ejemplo desde Postman)
                // usando nuestro jwtDecoder() que verifica la firma RS256 de Auth0, el issuer y el audience.
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .decoder(jwtDecoder())
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                )

                // Intercepta peticiones del navegador para validar el JWT de la cookie (si se uso el login local)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // 3. Proceso de inicio de sesión local de Spring MVC (lo dejamos para poder seguir entrando tambien con cuentas locales)
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/logincheck")
                        .usernameParameter("email")
                        .passwordParameter("clave")
                        .successHandler((request, response, authentication) -> {
                            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                            String token = jwtService.getToken(userDetails);

                            // Emite la cookie HTTP con el JWT
                            Cookie cookie = new Cookie("jwt", token);
                            cookie.setHttpOnly(true);
                            cookie.setPath("/");
                            cookie.setMaxAge(3600); // 1 hora
                            response.addCookie(cookie);

                            response.sendRedirect("/inicio");
                        })
                        .permitAll()
                )

                // 4. NUEVO: Cierre de sesión actualizado.
                // Destruye la sesion local y las cookies, y usa oidcLogoutSuccessHandler() para cerrar tambien la sesion en Auth0.
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .deleteCookies("jwt", "JSESSIONID")
                        .invalidateHttpSession(true)
                        .logoutSuccessHandler(oidcLogoutSuccessHandler())
                        .permitAll()
                );

        return http.build();
    }

    /**
     * NUEVO: Configura el decodificador de tokens JWT para cuando nuestra app actua como servidor (Resource Server).
     * Descarga las claves publicas (JWKS) desde Auth0 para validar la firma RS256, y combina el validador de Issuer
     * con nuestro AudienceValidator para exigir que el token sea para "https://api.tinder.com".
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder jwtDecoder = JwtDecoders.fromOidcIssuerLocation(issuer);

        OAuth2TokenValidator<Jwt> audienceValidator = new AudienceValidator(audience);
        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuer);
        OAuth2TokenValidator<Jwt> withAudience = new DelegatingOAuth2TokenValidator<>(withIssuer, audienceValidator);

        jwtDecoder.setJwtValidator(withAudience);
        return jwtDecoder;
    }

    /**
     * NUEVO: Convierte los permisos que trae el JWT de Auth0 en autoridades de Spring Security
     * y le asigna ROLE_USER (o el rol que tenga ese mail en nuestra base de datos).
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter scopesConverter = new JwtGrantedAuthoritiesConverter();

        JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
        jwtConverter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Collection<GrantedAuthority> authorities = new ArrayList<>(scopesConverter.convert(jwt));
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

            String email = jwt.getClaimAsString("email");
            if (email != null) {
                Usuario u = usuarioRepositorio.buscarPorMail(email);
                if (u != null && u.getRol() != null) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + u.getRol().toString()));
                }
            }
            return authorities;
        });
        return jwtConverter;
    }

    /**
     * NUEVO: Intercepta la redireccion de login hacia Auth0 y le inyecta el parametro "audience=https://api.tinder.com".
     * Si no mandamos el audience al hacer login en Auth0, Auth0 devuelve un token opaco en vez de un JWT valido para nuestra API.
     */
    private OAuth2AuthorizationRequestResolver authorizationRequestResolver(
            ClientRegistrationRepository clientRegistrationRepository) {

        DefaultOAuth2AuthorizationRequestResolver authorizationRequestResolver =
                new DefaultOAuth2AuthorizationRequestResolver(
                        clientRegistrationRepository, "/oauth2/authorization");

        authorizationRequestResolver.setAuthorizationRequestCustomizer(
                customizer -> customizer.additionalParameters(params -> params.put("audience", audience))
        );

        return authorizationRequestResolver;
    }

    /**
     * NUEVO: Manejador de logout para Auth0.
     * Si el usuario habia entrado con OAuth2, al tocar "Salir" redirigimos al endpoint /v2/logout de Auth0
     * pasandole nuestro client_id y el returnTo (http://localhost:9000) que autorizamos en el panel de Auth0.
     * Si habia entrado con el login comun local, simplemente lo mandamos a /login?logout.
     */
    private LogoutSuccessHandler oidcLogoutSuccessHandler() {
        return (request, response, authentication) -> {
            if (authentication instanceof OAuth2AuthenticationToken) {
                String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
                String clientId = clientRegistrationRepository.findByRegistrationId("auth0").getClientId();
                String logoutUrl = issuer + "v2/logout?client_id=" + clientId + "&returnTo=" + baseUrl;
                response.sendRedirect(logoutUrl);
            } else {
                response.sendRedirect("/login?logout");
            }
        };
    }

    /**
     * NUEVO: Configuracion de CORS para que cuando se consuma el servidor como API enviando el header
     * "Authorization: Bearer <token>", el navegador o cliente HTTP no sea bloqueado.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
