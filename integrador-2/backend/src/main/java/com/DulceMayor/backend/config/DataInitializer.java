package com.DulceMayor.backend.config;

import com.DulceMayor.backend.entities.RolUsuario;
import com.DulceMayor.backend.services.UsuarioService;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Inicializador de datos semilla (seed) para usuarios base por rol.
 * Es idempotente y solo se ejecuta cuando app.seed.enabled=true.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Map<String, RolUsuario> USUARIOS_SEMILLA = Map.of(
            "jefe", RolUsuario.JEFE,
            "administrativo", RolUsuario.ADMINISTRATIVO,
            "preventista", RolUsuario.PREVENTISTA,
            "distribuidor", RolUsuario.DISTRIBUIDOR,
            "minorista", RolUsuario.MINORISTA
    );

    private final UsuarioService usuarioService;
    private final boolean seedEnabled;

    public DataInitializer(
            UsuarioService usuarioService,
            @Value("${app.seed.enabled:false}") boolean seedEnabled) {
        this.usuarioService = usuarioService;
        this.seedEnabled = seedEnabled;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!seedEnabled) {
            return;
        }
        ejecutarSeed();
    }

    @Transactional
    public void ejecutarSeed() {
        USUARIOS_SEMILLA.forEach(usuarioService::crearUsuarioSemillaSiNoExiste);
    }
}

