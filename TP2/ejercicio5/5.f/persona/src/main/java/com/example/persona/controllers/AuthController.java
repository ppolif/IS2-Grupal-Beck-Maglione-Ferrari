package com.example.persona.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador REST para consultar el estado de autenticación y obtener el Token JWT activo.
 * Permite tanto al frontend (app.js) como al desarrollador (para usar en Postman) obtener
 * la información del perfil del usuario y el Bearer Token JWT emitido por Auth0.
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping(path = "api/v1/auth")
public class AuthController {

    /**
     * Endpoint GET /api/v1/auth/me
     * Soporta ambos mecanismos de autenticación configurados en SecurityConfig:
     * 1) Si se llama desde el navegador tras el login con Auth0 (OidcUser + OAuth2AuthorizedClient),
     *    extrae los datos del usuario y el Access Token JWT destinado a https://api.persona.com.
     * 2) Si se llama desde Postman enviando un header "Authorization: Bearer <token>" (Jwt),
     *    devuelve los claims decodificados del token JWT.
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(
            Authentication authentication,
            @RegisteredOAuth2AuthorizedClient("auth0") OAuth2AuthorizedClient authorizedClient) {

        Map<String, Object> response = new HashMap<>();

        // Si por algún motivo no hay contexto de autenticación, devolvemos 401 Unauthorized
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        response.put("authenticated", true);
        response.put("principalType", authentication.getPrincipal().getClass().getSimpleName());

        // CASO 1: El usuario entró por el navegador usando OAuth2 Login (OpenID Connect)
        if (authentication.getPrincipal() instanceof OidcUser oidcUser) {
            response.put("name", oidcUser.getFullName() != null ? oidcUser.getFullName() : oidcUser.getPreferredUsername());
            response.put("email", oidcUser.getEmail());
            response.put("picture", oidcUser.getPicture());
            // Token de identidad (ID Token)
            response.put("idToken", oidcUser.getIdToken().getTokenValue());

            // Access Token JWT (emitido para la audiencia https://api.persona.com)
            if (authorizedClient != null && authorizedClient.getAccessToken() != null) {
                response.put("accessToken", authorizedClient.getAccessToken().getTokenValue());
            }
        }
        // CASO 2: El usuario consumió el endpoint enviando el header "Authorization: Bearer <JWT>"
        else if (authentication.getPrincipal() instanceof Jwt jwt) {
            response.put("subject", jwt.getSubject());
            response.put("audience", jwt.getAudience());
            response.put("claims", jwt.getClaims());
            response.put("accessToken", jwt.getTokenValue());
        }

        return ResponseEntity.ok(response);
    }
}

