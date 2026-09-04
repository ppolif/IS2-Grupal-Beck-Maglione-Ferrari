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

//esta anotacion es para seguridad a nivel de metodos, es el que permite el uso
//de por ejemplo @PreAuthorize
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

                //sección donde se indica al sistema qué URLs están protegidas y cuáles no
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

                        //agregamos que hay paginas que solo las acceden admins
                        .requestMatchers("/admin/*").hasRole("ADMIN")

                        // Cualquier otra petición requiere login
                        .anyRequest().authenticated()
                )

                //Configura cómo será el proceso de inicio de sesión
                .formLogin(form -> form
                        //Reemplaza la pantalla de login que trae Spring por defecto por tu propio archivo HTML
                        .loginPage("/login")

                        .loginProcessingUrl("/logincheck")
                        .usernameParameter("email")
                        .passwordParameter("clave")

                        //si el inicio de sesión es exitoso, redirija al usuario a la pantalla de inicio
                        .defaultSuccessUrl("/inicio", true)
                        .permitAll()
                )

                //Maneja automáticamente el proceso de limpieza y cierre de sesión cuando
                // el usuario ingresa a la URL /logout
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                )
                .csrf(csrf -> csrf.disable());

        return http.build();
    }
}