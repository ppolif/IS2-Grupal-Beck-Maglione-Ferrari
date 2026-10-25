package com.example.tinder.configuraciones;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * NUEVO (Ejercicio 5.e): Validador personalizado para los tokens JWT en el servidor (Resource Server).
 * Por defecto Spring Security solo revisa que la firma del token sea válida, que no esté vencido (exp)
 * y que venga de nuestro dominio de Auth0 (iss).
 * Con esta clase agregamos la validación del claim "aud" (Audience) para asegurarnos de que el token JWT
 * haya sido emitido específicamente para nuestra API de Tinder (https://api.tinder.com) y no para otra.
 */
public class AudienceValidator implements OAuth2TokenValidator<Jwt> {

    // Guardamos el identifier de nuestra API que inyectamos desde application.properties
    private final String audience;

    public AudienceValidator(String audience) {
        this.audience = audience;
    }

    /**
     * Este metodo lo llama Spring Security automaticamente cada vez que entra una peticion con un Bearer Token.
     */
    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        // Armamos el error estandar de OAuth2 por si el token no trae el audience que esperamos
        OAuth2Error error = new OAuth2Error(
                "invalid_token",
                "El audience requerido (" + audience + ") no está presente en el token JWT",
                null
        );

        // Si dentro de la lista de destinatarios ("aud") del token figura "https://api.tinder.com", pasa la validacion
        if (jwt.getAudience() != null && jwt.getAudience().contains(audience)) {
            return OAuth2TokenValidatorResult.success();
        }

        // Si no coincide, rechazamos el token (devuelve 401 Unauthorized)
        return OAuth2TokenValidatorResult.failure(error);
    }
}
