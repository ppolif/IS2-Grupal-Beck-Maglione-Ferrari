package org.example.seguridad;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // Habilita @PreAuthorize en controladores/servicios para defensa en profundidad
public class SeguridadWeb {

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Encripta contraseñas usando el algoritmo BCrypt, haciéndolas irrecuperables en caso de robo de base de datos.
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Autorización de rutas (Principio de mínimo privilegio)
                .authorizeHttpRequests(auth -> auth
                        // 1. Recursos estáticos y rutas públicas (No requieren autenticación)
                        .requestMatchers("/css/**", "/js/**", "/img/**", "/webjars/**").permitAll()
                        .requestMatchers("/", "/index", "/login", "/registro", "/error").permitAll()
                        .requestMatchers(HttpMethod.POST, "/registro").permitAll() // Nuestro nuevo endpoint MVC de registro

                        // 2. Rutas que requieren rol ADMINISTRADOR específicamente (Gestión interna)
                        .requestMatchers( "/autores/**", "/editoriales/**", "/imagenes/**", "/gestion", "/usuarios/**", "/prestamos/devolver/**").hasRole("ADMINISTRADOR")

                        // 3. Rutas que requieren estar autenticado (Panel, mis préstamos, catálogo de consulta)
                        .requestMatchers("/inicio", "/libros", "/prestamos").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/prestamos/usuario/**").authenticated() // Mis préstamos

                        // 4. Red de seguridad (Cierre por defecto)
                        .anyRequest().permitAll()
                )

                // Configuración del Form Login (Mitiga accesos no autorizados guiando al flujo correcto)
                .formLogin(login -> login
                        .loginPage("/login") // Vista personalizada
                        .loginProcessingUrl("/logincheck") // Action del formulario
                        .usernameParameter("mail")
                        .passwordParameter("clave")
                        .defaultSuccessUrl("/inicio", true) // Redirección forzada tras login exitoso
                        .failureUrl("/login?error=true") // Redirección tras fallo (informa al usuario)
                        .permitAll()
                )

                // Configuración de Logout (Cierre seguro de sesión)
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true) // Destruye los datos de la sesión en el servidor
                        .deleteCookies("JSESSIONID") // Borra la cookie del navegador del cliente
                        .permitAll()
                )

                // Gestión de Sesión
                .sessionManagement(session -> session
                        .sessionFixation().migrateSession() // Previene ataques de fijación de sesión creando un nuevo SessionID al autenticarse
                )

                // Configuración de excepciones (Redirección amigable si falla la autorización)
                .exceptionHandling(ex -> ex
                        .accessDeniedPage("/403") // Redirige a una vista personalizada si se intenta acceder a una ruta de admin siendo usuario normal
                );


        return http.build();
    }
}