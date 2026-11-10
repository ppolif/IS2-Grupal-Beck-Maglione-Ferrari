package com.DulceMayor.backend.config;

import com.DulceMayor.backend.entities.RolUsuario;
import com.DulceMayor.backend.entities.mayorista.ConfiguracionCorreoMayorista;
import com.DulceMayor.backend.entities.mayorista.Mayorista;
import com.DulceMayor.backend.entities.mayorista.TipoEmpresa;
import com.DulceMayor.backend.repositories.mayorista.ConfiguracionCorreoMayoristaRepository;
import com.DulceMayor.backend.repositories.mayorista.MayoristaRepository;
import com.DulceMayor.backend.services.UsuarioService;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Inicializador de datos semilla (seed) para usuarios base por rol, mayorista central y configuración SMTP.
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
    private final MayoristaRepository mayoristaRepository;
    private final ConfiguracionCorreoMayoristaRepository configuracionCorreoRepository;
    private final boolean seedEnabled;
    private final String mailUsername;
    private final String mailPassword;

    public DataInitializer(
            UsuarioService usuarioService,
            MayoristaRepository mayoristaRepository,
            ConfiguracionCorreoMayoristaRepository configuracionCorreoRepository,
            @Value("${app.seed.enabled:false}") boolean seedEnabled,
            @Value("${spring.mail.username:}") String mailUsername,
            @Value("${spring.mail.password:}") String mailPassword) {
        this.usuarioService = usuarioService;
        this.mayoristaRepository = mayoristaRepository;
        this.configuracionCorreoRepository = configuracionCorreoRepository;
        this.seedEnabled = seedEnabled;
        this.mailUsername = mailUsername;
        this.mailPassword = mailPassword;
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

        if (mayoristaRepository.count() == 0) {
            Mayorista mayorista = Mayorista.builder()
                    .razonSocial("Dulce Mayor S.A. - Casa Central")
                    .cuit("30-71234567-9")
                    .tipoEmpresa(TipoEmpresa.SEDE_CENTRAL)
                    .eliminado(false)
                    .build();
            Mayorista guardado = mayoristaRepository.save(mayorista);

            if (mailUsername != null && !mailUsername.isBlank()) {
                ConfiguracionCorreoMayorista configMail = ConfiguracionCorreoMayorista.builder()
                        .mayorista(guardado)
                        .correo(mailUsername)
                        .clave(mailPassword)
                        .smtp("smtp.gmail.com")
                        .puerto("587")
                        .tls(true)
                        .eliminado(false)
                        .build();
                configuracionCorreoRepository.save(configMail);
            }
        }
    }
}
