package com.example.tinder.configuraciones;

import com.example.tinder.entidades.Mascota;
import com.example.tinder.entidades.Usuario;
import com.example.tinder.entidades.Zona;
import com.example.tinder.enumeraciones.Rol;
import com.example.tinder.enumeraciones.Sexo;
import com.example.tinder.enumeraciones.Tipo;
import com.example.tinder.repositorios.MascotaRepositorio;
import com.example.tinder.repositorios.UsuarioRepositorio;
import com.example.tinder.repositorios.ZonaRepositorio;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class DataInitializer implements CommandLineRunner {

    private final ZonaRepositorio zonaRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;
    private final MascotaRepositorio mascotaRepositorio;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(
            ZonaRepositorio zonaRepositorio,
            UsuarioRepositorio usuarioRepositorio,
            MascotaRepositorio mascotaRepositorio,
            PasswordEncoder passwordEncoder) {
        this.zonaRepositorio = zonaRepositorio;
        this.usuarioRepositorio = usuarioRepositorio;
        this.mascotaRepositorio = mascotaRepositorio;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (zonaRepositorio.count() == 0) {
            Zona centro = new Zona();
            centro.setNombre("Zona Centro");
            centro.setDescripcion("Área urbana céntrica");
            zonaRepositorio.save(centro);

            Zona norte = new Zona();
            norte.setNombre("Zona Norte");
            norte.setDescripcion("Zona residencial norte");
            zonaRepositorio.save(norte);

            Zona sur = new Zona();
            sur.setNombre("Zona Sur");
            sur.setDescripcion("Zona residencial sur");
            zonaRepositorio.save(sur);

            Zona este = new Zona();
            este.setNombre("Zona Este");
            este.setDescripcion("Zona este");
            zonaRepositorio.save(este);

            Zona oeste = new Zona();
            oeste.setNombre("Zona Oeste");
            oeste.setDescripcion("Zona oeste");
            zonaRepositorio.save(oeste);

            // Usuarios de demostración para pruebas inmediatas de la API REST
            if (usuarioRepositorio.count() == 0) {
                Usuario admin = new Usuario();
                admin.setNombre("Administrador");
                admin.setApellido("Tinder");
                admin.setEmail("admin@tinder.com");
                admin.setClave(passwordEncoder.encode("admin123"));
                admin.setRol(Rol.ADMIN);
                admin.setZona(centro);
                admin.setAlta(new Date());
                usuarioRepositorio.save(admin);

                Usuario user = new Usuario();
                user.setNombre("Juan");
                user.setApellido("Perez");
                user.setEmail("juan@tinder.com");
                user.setClave(passwordEncoder.encode("usuario123"));
                user.setRol(Rol.USER);
                user.setZona(norte);
                user.setAlta(new Date());
                usuarioRepositorio.save(user);

                // Mascotas de demostración
                Mascota firulais = new Mascota();
                firulais.setNombre("Firulais");
                firulais.setSexo(Sexo.MACHO);
                firulais.setTipo(Tipo.PERRO);
                firulais.setUsuario(user);
                firulais.setAlta(new Date());
                mascotaRepositorio.save(firulais);

                Mascota luna = new Mascota();
                luna.setNombre("Luna");
                luna.setSexo(Sexo.HEMBRA);
                luna.setTipo(Tipo.GATO);
                luna.setUsuario(admin);
                luna.setAlta(new Date());
                mascotaRepositorio.save(luna);
            }
        }
    }
}

