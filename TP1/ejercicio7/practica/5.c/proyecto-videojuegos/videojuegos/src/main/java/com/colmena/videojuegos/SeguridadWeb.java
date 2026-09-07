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
                .csrf().disable()
                .authorizeRequests()
                // 1. Recursos estáticos e imágenes (con antMatchers acepta múltiples comodines sin error)
                .antMatchers("/css/**", "/js/**", "/img/**", "/webjars/**", "/imagenes/**").permitAll()

                // 2. Vistas principales del ABM
                .antMatchers("/", "/inicio", "/busqueda", "/crud", "/detalle/**", "/error").permitAll()

                // 3. Rutas de operaciones de formulario
                .antMatchers("/formulario/**").permitAll()

                // 4. Cualquier otra ruta queda habilitada
                .anyRequest().permitAll();

        return http.build();
    }
}