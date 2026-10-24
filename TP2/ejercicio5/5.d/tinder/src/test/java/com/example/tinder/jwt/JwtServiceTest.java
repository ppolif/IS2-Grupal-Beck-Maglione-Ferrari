package com.example.tinder.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class JwtServiceTest {

    private JwtService jwtService;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        userDetails = new User(
                "usuario@tinder.com",
                "1234567",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    @Test
    void testGenerarYValidarToken() {
        String token = jwtService.getToken(userDetails);
        assertNotNull(token, "El token no debe ser nulo");
        assertFalse(token.isEmpty(), "El token no debe estar vacío");

        String username = jwtService.getUsernameFromToken(token);
        assertEquals("usuario@tinder.com", username, "El subject del token debe coincidir con el email");

        boolean isValid = jwtService.isTokenValid(token, userDetails);
        assertTrue(isValid, "El token debe ser válido para el usuario original");
    }

    @Test
    void testTokenInvalidoParaOtroUsuario() {
        String token = jwtService.getToken(userDetails);

        UserDetails otroUsuario = new User(
                "otro@tinder.com",
                "1234567",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        boolean isValid = jwtService.isTokenValid(token, otroUsuario);
        assertFalse(isValid, "El token no debe ser válido para un usuario diferente");
    }
}

