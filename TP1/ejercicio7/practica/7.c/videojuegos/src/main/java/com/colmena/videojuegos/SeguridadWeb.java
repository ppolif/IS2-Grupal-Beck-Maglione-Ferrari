package com.colmena.videojuegos;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SeguridadWeb {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. Apagamos la protección CSRF con la sintaxis lambda moderna
                .csrf(csrf -> csrf.disable())

                // 2. Usamos authorizeHttpRequests en lugar de authorizeRequests
                .authorizeHttpRequests(auth -> auth
                        // 1. Recursos estáticos e imágenes (reemplazamos antMatchers por requestMatchers)
                        .requestMatchers("/css/**", "/js/**", "/img/**", "/webjars/**", "/imagenes/**").permitAll()

                        // 2. Vistas principales del ABM
                        .requestMatchers("/", "/inicio", "/busqueda", "/crud", "/detalle/**", "/error").permitAll()

                        // 3. Rutas de operaciones de formulario
                        .requestMatchers("/formulario/**").permitAll()

                        // 4. Cualquier otra ruta queda habilitada
                        .anyRequest().permitAll()
                );

        return http.build();
    }
}