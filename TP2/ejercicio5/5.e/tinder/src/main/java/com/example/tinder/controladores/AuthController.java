package com.example.tinder.controladores;

import com.example.tinder.entidades.Usuario;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * NUEVO (Ejercicio 5.e): Controlador REST que agregamos para poder ver el estado de la sesion OAuth2
 * y obtener el Access Token JWT que nos dio Auth0.
 * Nos sirve tanto para probar el rol de Cliente (entrando desde el navegador después de loguearnos en Auth0)
 * como para probar el rol de Servidor de Recursos (mandando una petición desde Postman con el header Authorization: Bearer <token>).
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping(path = "/api/v1/auth")
public class AuthController {

    // Servicio de Spring Security que guarda los clientes autorizados de OAuth2 junto con sus tokens
    @Autowired
    private OAuth2AuthorizedClientService authorizedClientService;

    /**
     * Endpoint GET /api/v1/auth/me
     * Funciona con los dos flujos de seguridad que configuramos en SecurityConfig:
     * 1) Si entramos desde el navegador (OidcUser), nos muestra nuestros datos de Auth0, el ID Token,
     *    el Access Token JWT emitido para https://api.tinder.com y el usuario sincronizado en nuestra BD local.
     * 2) Si llamamos al endpoint desde Postman mandando "Authorization: Bearer <token>" (Jwt),
     *    el Resource Server valida el token y aca devolvemos sus claims decodificados.
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(
            Authentication authentication,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        // Si no hay nadie autenticado devolvemos 401
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        response.put("authenticated", true);
        response.put("principalType", authentication.getPrincipal().getClass().getSimpleName());
        response.put("authorities", authentication.getAuthorities());

        // CASO 1: El usuario inició sesión por el navegador usando OAuth2 Login con Auth0 (OpenID Connect)
        if (authentication.getPrincipal() instanceof OidcUser oidcUser) {
            response.put("name", oidcUser.getFullName() != null ? oidcUser.getFullName() : oidcUser.getPreferredUsername());
            response.put("email", oidcUser.getEmail());
            response.put("picture", oidcUser.getPicture());
            // Token de identidad (ID Token)
            response.put("idToken", oidcUser.getIdToken().getTokenValue());

            // Buscamos el cliente autorizado en sesion para sacar el Access Token JWT (que podemos copiar para probar en Postman)
            if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
                OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(
                        oauthToken.getAuthorizedClientRegistrationId(),
                        oauthToken.getName()
                );
                if (authorizedClient != null && authorizedClient.getAccessToken() != null) {
                    response.put("accessToken", authorizedClient.getAccessToken().getTokenValue());
                }
            }

            // Mostramos tambien el ID y rol con el que quedo vinculado en nuestra base de datos de Tinder
            if (session != null && session.getAttribute("usuariosession") instanceof Usuario usuarioLocal) {
                response.put("usuarioLocalId", usuarioLocal.getId());
                response.put("rolLocal", usuarioLocal.getRol());
            }
        }
        // CASO 2: Se consumio el endpoint mandando el header "Authorization: Bearer <JWT>" (Resource Server)
        else if (authentication.getPrincipal() instanceof Jwt jwt) {
            response.put("subject", jwt.getSubject());
            response.put("audience", jwt.getAudience());
            response.put("claims", jwt.getClaims());
            response.put("accessToken", jwt.getTokenValue());
        }

        return ResponseEntity.ok(response);
    }
}
