package com.DulceMayor.backend.config;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.UrlJwkProvider;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import java.security.interfaces.RSAPublicKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Servicio encargado de validar el token de identidad provisto por Auth0
 * y extraer la información básica del usuario (email, nombre, apellido, sub).
 */
@Service
public class Auth0TokenValidatorService {

    @Value("${auth0.domain:}")
    private String domain;

    public record Auth0UserInfo(String email, String nombre, String apellido, String sub) {}

    public Auth0UserInfo validarYExtraerUsuario(String token) {
        try {
            DecodedJWT jwt = JWT.decode(token);

            if (domain != null && !domain.isBlank() && !"dummy".equalsIgnoreCase(domain)) {
                String cleanDomain = domain.replace("https://", "").replace("http://", "");
                if (cleanDomain.endsWith("/")) {
                    cleanDomain = cleanDomain.substring(0, cleanDomain.length() - 1);
                }
                JwkProvider provider = new UrlJwkProvider(cleanDomain);
                Jwk jwk = provider.get(jwt.getKeyId());
                Algorithm algorithm = Algorithm.RSA256((RSAPublicKey) jwk.getPublicKey(), null);
                JWTVerifier verifier = JWT.require(algorithm).build();
                jwt = verifier.verify(token);
            }

            String email = jwt.getClaim("email").asString();
            if (email == null || email.isBlank()) {
                email = jwt.getSubject();
            }

            String name = jwt.getClaim("name").asString();
            String givenName = jwt.getClaim("given_name").asString();
            String familyName = jwt.getClaim("family_name").asString();

            String nombre = givenName != null ? givenName : (name != null ? name : "Usuario");
            String apellido = familyName != null ? familyName : "";

            return new Auth0UserInfo(email, nombre, apellido, jwt.getSubject());
        } catch (Exception e) {
            throw new ValidacionException("Token de Auth0 inválido o no verificado: " + e.getMessage());
        }
    }
}

