package com.example.tinder.configuraciones;

import com.example.tinder.jwt.JwtAuthenticationFilter;
import com.example.tinder.jwt.JwtService;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetails;
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

    @Autowired
    private JwtService jwtService;

    // Encriptador de contraseñas
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Administrador de autenticación
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    // Cadena de filtros y permisos
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())

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

                        // Rutas de procesamiento de registro y fotos públicas
                        .requestMatchers("/registro/guardar", "/usuario/registrar", "/registrar", "/foto/**").permitAll()

                        // Rutas exclusivas de administrador
                        .requestMatchers("/admin/*").hasRole("ADMIN")

                        // Cualquier otra petición requiere autenticación
                        .anyRequest().authenticated()
                )

                // Intercepta peticiones del navegador para validar el JWT de la cookie
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // Proceso de inicio de sesión de Spring MVC con emisión de Cookie JWT
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/logincheck")
                        .usernameParameter("email")
                        .passwordParameter("clave")
                        .successHandler((request, response, authentication) -> {
                            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                            String token = jwtService.getToken(userDetails);

                            // Emite la cookie HTTP con el JWT
                            Cookie cookie = new Cookie("jwt", token);
                            cookie.setHttpOnly(true);
                            cookie.setPath("/");
                            cookie.setMaxAge(3600); // 1 hora
                            response.addCookie(cookie);

                            response.sendRedirect("/inicio");
                        })
                        .permitAll()
                )

                // Cierre de sesión que destruye la cookie JWT y la sesión
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .deleteCookies("jwt", "JSESSIONID")
                        .invalidateHttpSession(true)
                        .permitAll()
                );

        return http.build();
    }
}
