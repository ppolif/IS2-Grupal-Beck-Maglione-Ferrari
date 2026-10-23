package com.example.persona.config;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Validador personalizado para tokens JWT.
 * Por defecto, Spring Security solo valida la firma, la fecha de expiración (exp) y el emisor (iss).
 * Esta clase agrega la validación del claim "aud" (Audience) para garantizar que el token JWT
 * fue emitido específicamente para nuestra API (https://api.persona.com) y no para otra aplicación.
 */
public class AudienceValidator implements OAuth2TokenValidator<Jwt> {

    // Almacena el identificador de la API configurado en application.properties (https://api.persona.com)
    private final String audience;

    public AudienceValidator(String audience) {
        this.audience = audience;
    }

    /**
     * Método que ejecuta Spring Security automáticamente cada vez que llega una petición con un Bearer Token.
     * @param jwt El token JWT ya decodificado y con su firma RS256 verificada.
     * @return Éxito si el token contiene nuestra audience, o Fallo (401 Unauthorized) en caso contrario.
     */
    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        // Definimos el error estándar OAuth2 en caso de que el token no tenga el audience correcto
        OAuth2Error error = new OAuth2Error(
                "invalid_token",
                "El audience requerido (" + audience + ") no está presente en el token JWT",
                null
        );

        // Verificamos si la lista de destinatarios ("aud") dentro del payload del JWT contiene "https://api.persona.com"
        if (jwt.getAudience().contains(audience)) {
            return OAuth2TokenValidatorResult.success();
        }

        // Si no lo contiene, rechazamos el token
        return OAuth2TokenValidatorResult.failure(error);
    }
}

