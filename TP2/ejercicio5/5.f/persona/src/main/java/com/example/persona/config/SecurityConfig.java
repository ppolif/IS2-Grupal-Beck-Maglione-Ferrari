package com.example.persona.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Inyectamos el identificador de la API (https://api.persona.com) desde application.properties
    @Value("${auth0.audience}")
    private String audience;

    // Inyectamos la URL del servidor de autorización de Auth0 (Issuer) desde application.properties
    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuer;

    // Repositorio interno de Spring que contiene los datos del cliente OAuth2 (client-id, client-secret, etc.)
    private final ClientRegistrationRepository clientRegistrationRepository;

    public SecurityConfig(ClientRegistrationRepository clientRegistrationRepository) {
        this.clientRegistrationRepository = clientRegistrationRepository;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // NUEVO: Habilita CORS dentro de la cadena de filtros de Spring Security.
                // Utiliza el bean corsConfigurationSource() definido más abajo para permitir headers "Authorization".
                .cors(Customizer.withDefaults())

                // Deshabilitamos CSRF para permitir peticiones POST, PUT y DELETE a la API REST
                .csrf(csrf -> csrf.disable())

                // Reglas de protección de los Endpoints
                .authorizeHttpRequests(auth -> auth
                        // Permitimos recursos del navegador como favicon.ico para que no disparen un doble login OAuth2
                        .requestMatchers("/favicon.ico", "/error").permitAll()
                        // Protegemos explícitamente todos los endpoints de la API REST (/api/v1/**)
                        .requestMatchers("/api/v1/**").authenticated()
                        // Cualquier otra ruta (como index.html y app.js) también requiere iniciar sesión
                        .anyRequest().authenticated()
                )

                // 1. Habilita el Login con Auth0 cuando entras desde el navegador (http://localhost:8080)
                // NUEVO: Personalizamos el resolver de la petición de autorización para inyectar el parámetro
                // "?audience=https://api.persona.com" al redirigir a Auth0. Gracias a esto, Auth0 emite un JWT válido.
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(authorization -> authorization
                                .authorizationRequestResolver(
                                        authorizationRequestResolver(this.clientRegistrationRepository)
                                )
                        )
                        // Al iniciar sesión con éxito, redirige siempre a la página principal (index.html)
                        .defaultSuccessUrl("/", true)
                )

                // 2. NUEVO: Configura el cierre de sesión (Logout).
                // Cuando el usuario navega a "/logout", destruye la sesión local en Spring Boot y redirige
                // a Auth0 para cerrar también la sesión única (SSO).
                .logout(logout -> logout
                        .logoutSuccessHandler(oidcLogoutSuccessHandler())
                )

                // 3. Habilita la validación de Tokens JWT cuando se consumen los endpoints desde Postman o fetch.
                // NUEVO: Le indicamos explícitamente que use nuestro bean jwtDecoder() que valida Issuer + Audience.
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.decoder(jwtDecoder()))
                );

        return http.build();
    }

    /**
     * NUEVO: Construye y configura el decodificador de tokens JWT para el Resource Server.
     * Descarga automáticamente las claves públicas (JWKS) desde Auth0 para verificar la firma RS256,
     * y combina dos validaciones obligatorias: que el emisor sea nuestro tenant de Auth0 (withIssuer)
     * y que la audiencia sea https://api.persona.com (audienceValidator).
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        // Crea el decodificador apuntando a https://dev-28ksfq3b24zrctsw.us.auth0.com/.well-known/openid-configuration
        NimbusJwtDecoder jwtDecoder = JwtDecoders.fromOidcIssuerLocation(issuer);

        // Instanciamos nuestro validador personalizado de Audience (https://api.persona.com)
        OAuth2TokenValidator<Jwt> audienceValidator = new AudienceValidator(audience);

        // Validador estándar de Spring que comprueba el emisor (iss) y la fecha de expiración (exp)
        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuer);

        // Unimos ambos validadores para que el token deba cumplir ambas condiciones simultáneamente
        OAuth2TokenValidator<Jwt> withAudience = new DelegatingOAuth2TokenValidator<>(withIssuer, audienceValidator);

        jwtDecoder.setJwtValidator(withAudience);
        return jwtDecoder;
    }

    /**
     * NUEVO: Intercepta la redirección de login hacia Auth0 y le agrega el parámetro "audience".
     * En Auth0, si el navegador inicia sesión sin pedir un "audience", Auth0 devuelve un Access Token opaco
     * (que no sirve para validar como JWT en el Resource Server). Al enviar "audience=https://api.persona.com",
     * Auth0 nos entrega un Access Token en formato JWT firmado para nuestra API.
     */
    private OAuth2AuthorizationRequestResolver authorizationRequestResolver(
            ClientRegistrationRepository clientRegistrationRepository) {

        DefaultOAuth2AuthorizationRequestResolver authorizationRequestResolver =
                new DefaultOAuth2AuthorizationRequestResolver(
                        clientRegistrationRepository, "/oauth2/authorization");

        // Agregamos "audience: https://api.persona.com" a los parámetros de la URL de autorización
        authorizationRequestResolver.setAuthorizationRequestCustomizer(
                customizer -> customizer.additionalParameters(params -> params.put("audience", audience))
        );

        return authorizationRequestResolver;
    }

    /**
     * NUEVO: Manejador de éxito al cerrar sesión (Logout Federado).
     * Construye la URL de cierre de sesión de Auth0 (/v2/logout) pasándole el client_id y la URL de retorno
     * (http://localhost:9000) para que Auth0 invalide su cookie de sesión y devuelva al usuario al inicio.
     */
    private LogoutSuccessHandler oidcLogoutSuccessHandler() {
        return (request, response, authentication) -> {
            // Obtiene la URL base de nuestra app (ej. http://localhost:9000)
            String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
            // Obtiene el client-id configurado en application.properties
            String clientId = clientRegistrationRepository.findByRegistrationId("auth0").getClientId();
            // Construye la URL de logout de Auth0 y redirige al navegador
            String logoutUrl = issuer + "v2/logout?client_id=" + clientId + "&returnTo=" + baseUrl;
            response.sendRedirect(logoutUrl);
        };
    }

    /**
     * NUEVO: Configuración global de CORS integrada con Spring Security.
     * Es indispensable para que el navegador permita enviar la cabecera "Authorization: Bearer <token>"
     * y permita los métodos GET, POST, PUT, DELETE y OPTIONS (preflight) sin ser bloqueados por seguridad.
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
