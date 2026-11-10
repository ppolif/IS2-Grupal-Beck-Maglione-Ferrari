package com.DulceMayor.backend.config;

import com.DulceMayor.backend.entities.RolUsuario;
import com.DulceMayor.backend.entities.direccion.Departamento;
import com.DulceMayor.backend.entities.direccion.Localidad;
import com.DulceMayor.backend.entities.direccion.Pais;
import com.DulceMayor.backend.entities.direccion.Provincia;
import com.DulceMayor.backend.entities.mayorista.ConfiguracionCorreoMayorista;
import com.DulceMayor.backend.entities.mayorista.Mayorista;
import com.DulceMayor.backend.entities.mayorista.TipoEmpresa;
import com.DulceMayor.backend.repositories.direccion.DepartamentoRepository;
import com.DulceMayor.backend.repositories.direccion.LocalidadRepository;
import com.DulceMayor.backend.repositories.direccion.PaisRepository;
import com.DulceMayor.backend.repositories.direccion.ProvinciaRepository;
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
    private final PaisRepository paisRepository;
    private final ProvinciaRepository provinciaRepository;
    private final DepartamentoRepository departamentoRepository;
    private final LocalidadRepository localidadRepository;
    private final boolean seedEnabled;
    private final String mailUsername;
    private final String mailPassword;

    public DataInitializer(
            UsuarioService usuarioService,
            MayoristaRepository mayoristaRepository,
            ConfiguracionCorreoMayoristaRepository configuracionCorreoRepository,
            PaisRepository paisRepository,
            ProvinciaRepository provinciaRepository,
            DepartamentoRepository departamentoRepository,
            LocalidadRepository localidadRepository,
            @Value("${app.seed.enabled:false}") boolean seedEnabled,
            @Value("${spring.mail.username:}") String mailUsername,
            @Value("${spring.mail.password:}") String mailPassword) {
        this.usuarioService = usuarioService;
        this.mayoristaRepository = mayoristaRepository;
        this.configuracionCorreoRepository = configuracionCorreoRepository;
        this.paisRepository = paisRepository;
        this.provinciaRepository = provinciaRepository;
        this.departamentoRepository = departamentoRepository;
        this.localidadRepository = localidadRepository;
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

        inicializarGeografiaSiEsNecesario();

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

    private void inicializarGeografiaSiEsNecesario() {
        Provincia mendoza;
        if (paisRepository.count() == 0) {
            Pais argentina = paisRepository.save(Pais.builder().nombre("Argentina").eliminado(false).build());
            mendoza = provinciaRepository.save(Provincia.builder().nombre("Mendoza").pais(argentina).eliminado(false).build());
        } else {
            mendoza = provinciaRepository.findAllByEliminadoFalseOrderByNombreAsc().stream()
                    .filter(p -> "Mendoza".equalsIgnoreCase(p.getNombre()))
                    .findFirst()
                    .orElse(null);
        }

        if (mendoza != null) {
            crearDeptoYLocalidades(mendoza, "Capital", Map.of("Ciudad de Mendoza", "5500"));
            crearDeptoYLocalidades(mendoza, "Godoy Cruz", Map.of("Godoy Cruz", "5501"));
            crearDeptoYLocalidades(mendoza, "Guaymallén", Map.of("Villa Nueva", "5521", "Dorrego", "5519", "San José", "5519"));
            crearDeptoYLocalidades(mendoza, "Las Heras", Map.of("Las Heras", "5539", "El Plumerillo", "5539"));
            crearDeptoYLocalidades(mendoza, "Maipú", Map.of("Maipú", "5515", "Gutiérrez", "5511"));
            crearDeptoYLocalidades(mendoza, "Luján de Cuyo", Map.of("Luján de Cuyo", "5507", "Chacras de Coria", "5505"));
            crearDeptoYLocalidades(mendoza, "San Martín", Map.of("San Martín", "5570"));
        }
    }

    private void crearDeptoYLocalidades(Provincia provincia, String nombreDepto, Map<String, String> localidades) {
        Departamento depto = departamentoRepository.findByNombreIgnoreCaseAndProvinciaIdAndEliminadoFalse(nombreDepto, provincia.getId())
                .orElseGet(() -> departamentoRepository.save(Departamento.builder().nombre(nombreDepto).provincia(provincia).eliminado(false).build()));

        localidades.forEach((nomLoc, cp) -> {
            if (!localidadRepository.existsByNombreIgnoreCaseAndDepartamentoIdAndEliminadoFalse(nomLoc, depto.getId())) {
                localidadRepository.save(Localidad.builder()
                        .nombre(nomLoc)
                        .codigoPostal(cp)
                        .departamento(depto)
                        .eliminado(false)
                        .build());
            }
        });
    }
}
