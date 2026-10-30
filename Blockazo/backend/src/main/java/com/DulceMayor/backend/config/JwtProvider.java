package com.DulceMayor.backend.config;

import com.DulceMayor.backend.entities.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Componente encargado de la emisión y validación de tokens JWT (HS256).
 * Valida en el arranque que el secreto tenga al menos 32 caracteres.
 * No almacena claveTemporal en los claims para consultar siempre el estado actual en base de datos.
 */
@Component
public class JwtProvider {

    private static final int MIN_SECRET_LENGTH = 32;

    private final SecretKey signingKey;
    private final long expirationMinutes;

    public JwtProvider(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-minutes:60}") long expirationMinutes) {
        if (secret == null || secret.trim().length() < MIN_SECRET_LENGTH) {
            throw new IllegalStateException(
                    "La variable de entorno JWT_SECRET es obligatoria y debe tener al menos 32 caracteres");
        }
        if (expirationMinutes <= 0) {
            throw new IllegalStateException("La propiedad app.jwt.expiration-minutes debe ser mayor a 0");
        }
        this.signingKey = Keys.hmacShaKeyFor(secret.trim().getBytes(StandardCharsets.UTF_8));
        this.expirationMinutes = expirationMinutes;
    }

    public String generarToken(Usuario usuario) {
        Instant ahora = Instant.now();
        Instant expiracion = ahora.plusSeconds(getExpiraEnSegundos());

        return Jwts.builder()
                .setSubject(usuario.getId())
                .claim("rol", usuario.getRol().name())
                .setIssuedAt(Date.from(ahora))
                .setExpiration(Date.from(expiracion))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims validarYObtenerClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String extraerUsuarioId(String token) {
        return validarYObtenerClaims(token).getSubject();
    }

    public long getExpiraEnSegundos() {
        return expirationMinutes * 60L;
    }
}

