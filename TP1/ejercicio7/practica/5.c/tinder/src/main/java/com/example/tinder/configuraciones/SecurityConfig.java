package com.example.tinder.configuraciones;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import jakarta.servlet.DispatcherType;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    // Encriptador de contraseñas
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Cadena de filtros y permisos
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth

                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()

                        // Recursos estáticos públicos (CSS, JS, imágenes)
                        .requestMatchers("/css/**", "/js/**", "/img/**", "/imagenes/**", "/styles.css", "/*.css", "/vendor/**").permitAll()
                        // Vistas públicas
                        .requestMatchers(
                                "/",
                                "/index",
                                "/index.html",
                                "/registro",
                                "/registro.html",
                                "/login",
                                "/login.html",
                                "/error",
                                "/error.html"
                        ).permitAll()
                        // Rutas de procesamiento de registro
                        .requestMatchers("/registro/guardar", "/usuario/registrar", "/registrar").permitAll()
                        // Cualquier otra petición requiere login
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/logincheck")
                        .usernameParameter("email")
                        .passwordParameter("clave")
                        .defaultSuccessUrl("/inicio", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                )

                //Spring Security tiene una función nativa llamada rememberMe que crea,
                // encripta y valida cookies automaticamente
                .rememberMe(rememberMe -> rememberMe
                        .key("secretoTinderMascotas") // clave de encriptacion
                        .rememberMeParameter("recordarme") // el nombre del checkbox en el html
                        .tokenValiditySeconds(172800) // el tiempo tiene que estar en segundos, estos son 2 dias
                )


                .csrf(csrf -> csrf.disable());

        return http.build();
    }
}