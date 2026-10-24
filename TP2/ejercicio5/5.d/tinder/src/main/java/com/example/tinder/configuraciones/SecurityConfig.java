package com.example.tinder.configuraciones;

import com.example.tinder.jwt.JwtAuthenticationFilter;
import jakarta.servlet.DispatcherType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    // Encriptador de contraseñas
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Administrador de autenticación expuesto como Bean para el controlador REST de autenticación
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    // Cadena de filtros y permisos
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Deshabilitar CSRF para permitir peticiones REST sin estado
                .csrf(csrf -> csrf.disable())

                // Sección donde se indica al sistema qué URLs están protegidas y cuáles no
                .authorizeHttpRequests(auth -> auth

                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()

                        // Recursos estáticos públicos (CSS, JS, imágenes)
                        .requestMatchers("/css/**", "/js/**", "/img/**", "/imagenes/**", "/styles.css", "/*.css", "/vendor/**").permitAll()

                        // Endpoints públicos de autenticación REST (JWT)
                        .requestMatchers("/auth/**", "/api/auth/**").permitAll()

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

                        // Rutas de procesamiento de registro y fotos públicas
                        .requestMatchers("/registro/guardar", "/usuario/registrar", "/registrar", "/foto/**").permitAll()

                        // Rutas exclusivas de administrador
                        .requestMatchers("/admin/*").hasRole("ADMIN")

                        // Cualquier otra petición requiere autenticación (sea por sesión o por Bearer Token JWT)
                        .anyRequest().authenticated()
                )

                // Filtro JWT antes del filtro de usuario/contraseña estándar
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // Configuración de formulario de login para vistas Spring MVC clásicas
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/logincheck")
                        .usernameParameter("email")
                        .passwordParameter("clave")
                        .defaultSuccessUrl("/inicio", true)
                        .permitAll()
                )

                // Configuración de logout
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );

        return http.build();
    }
}
