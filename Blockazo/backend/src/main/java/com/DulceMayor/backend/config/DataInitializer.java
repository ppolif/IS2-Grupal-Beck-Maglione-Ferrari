package com.DulceMayor.backend.config;

import com.DulceMayor.backend.entities.direccion.Departamento;
import com.DulceMayor.backend.entities.direccion.Localidad;
import com.DulceMayor.backend.entities.direccion.Pais;
import com.DulceMayor.backend.entities.direccion.Provincia;
import com.DulceMayor.backend.repositories.direccion.DepartamentoRepository;
import com.DulceMayor.backend.repositories.direccion.LocalidadRepository;
import com.DulceMayor.backend.repositories.direccion.PaisRepository;
import com.DulceMayor.backend.repositories.direccion.ProvinciaRepository;
import com.DulceMayor.backend.services.UsuarioService;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Inicializador de datos semilla (seed) para usuarios y datos geográficos.
 * Es idempotente y solo se ejecuta cuando app.seed.enabled=true.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final List<String> USUARIOS_SEMILLA = List.of(
            "admin",
            "usuario",
            "minorista"
    );

    private final UsuarioService usuarioService;
    private final PaisRepository paisRepository;
    private final ProvinciaRepository provinciaRepository;
    private final DepartamentoRepository departamentoRepository;
    private final LocalidadRepository localidadRepository;
    private final boolean seedEnabled;

    public DataInitializer(
            UsuarioService usuarioService,
            PaisRepository paisRepository,
            ProvinciaRepository provinciaRepository,
            DepartamentoRepository departamentoRepository,
            LocalidadRepository localidadRepository,
            @Value("${app.seed.enabled:false}") boolean seedEnabled) {
        this.usuarioService = usuarioService;
        this.paisRepository = paisRepository;
        this.provinciaRepository = provinciaRepository;
        this.departamentoRepository = departamentoRepository;
        this.localidadRepository = localidadRepository;
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
        // 1. Usuarios base sin rol
        USUARIOS_SEMILLA.forEach(usuarioService::crearUsuarioSemillaSiNoExiste);

        // 2. Geografía base
        Pais argentina = paisRepository.findByNombreIgnoreCaseAndEliminadoFalse("Argentina")
                .orElseGet(() -> paisRepository.save(Pais.builder().nombre("Argentina").eliminado(false).build()));

        Provincia mendoza = provinciaRepository.findByNombreIgnoreCaseAndPaisIdAndEliminadoFalse("Mendoza", argentina.getId())
                .orElseGet(() -> provinciaRepository.save(Provincia.builder().nombre("Mendoza").pais(argentina).eliminado(false).build()));

        Departamento capital = departamentoRepository.findByNombreIgnoreCaseAndProvinciaIdAndEliminadoFalse("Capital", mendoza.getId())
                .orElseGet(() -> departamentoRepository.save(Departamento.builder().nombre("Capital").provincia(mendoza).eliminado(false).build()));

        Departamento godoyCruz = departamentoRepository.findByNombreIgnoreCaseAndProvinciaIdAndEliminadoFalse("Godoy Cruz", mendoza.getId())
                .orElseGet(() -> departamentoRepository.save(Departamento.builder().nombre("Godoy Cruz").provincia(mendoza).eliminado(false).build()));

        Departamento guaymallen = departamentoRepository.findByNombreIgnoreCaseAndProvinciaIdAndEliminadoFalse("Guaymallén", mendoza.getId())
                .orElseGet(() -> departamentoRepository.save(Departamento.builder().nombre("Guaymallén").provincia(mendoza).eliminado(false).build()));

        if (!localidadRepository.existsByNombreIgnoreCaseAndDepartamentoIdAndEliminadoFalse("Ciudad de Mendoza", capital.getId())) {
            localidadRepository.save(Localidad.builder()
                    .nombre("Ciudad de Mendoza")
                    .codigoPostal("5500")
                    .departamento(capital)
                    .eliminado(false)
                    .build());
        }

        if (!localidadRepository.existsByNombreIgnoreCaseAndDepartamentoIdAndEliminadoFalse("Godoy Cruz Centro", godoyCruz.getId())) {
            localidadRepository.save(Localidad.builder()
                    .nombre("Godoy Cruz Centro")
                    .codigoPostal("5501")
                    .departamento(godoyCruz)
                    .eliminado(false)
                    .build());
        }

        if (!localidadRepository.existsByNombreIgnoreCaseAndDepartamentoIdAndEliminadoFalse("San José", guaymallen.getId())) {
            localidadRepository.save(Localidad.builder()
                    .nombre("San José")
                    .codigoPostal("5519")
                    .departamento(guaymallen)
                    .eliminado(false)
                    .build());
        }
    }
}
