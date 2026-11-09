package com.DulceMayor.backend.config;

import com.DulceMayor.backend.dtos.producto.CategoriaRequest;
import com.DulceMayor.backend.dtos.producto.CategoriaResponse;
import com.DulceMayor.backend.dtos.producto.ProductoRequest;
import com.DulceMayor.backend.dtos.producto.SubCategoriaRequest;
import com.DulceMayor.backend.dtos.producto.SubCategoriaResponse;
import com.DulceMayor.backend.entities.RolUsuario;
import com.DulceMayor.backend.services.UsuarioService;
import com.DulceMayor.backend.services.producto.CategoriaService;
import com.DulceMayor.backend.services.producto.ProductoService;
import com.DulceMayor.backend.services.producto.SubCategoriaService;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Inicializador de datos semilla (seed) para usuarios base por rol y catálogo inicial.
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
    private final CategoriaService categoriaService;
    private final SubCategoriaService subCategoriaService;
    private final ProductoService productoService;
    private final boolean seedEnabled;

    public DataInitializer(
            UsuarioService usuarioService,
            CategoriaService categoriaService,
            SubCategoriaService subCategoriaService,
            ProductoService productoService,
            @Value("${app.seed.enabled:false}") boolean seedEnabled) {
        this.usuarioService = usuarioService;
        this.categoriaService = categoriaService;
        this.subCategoriaService = subCategoriaService;
        this.productoService = productoService;
        this.seedEnabled = seedEnabled;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!seedEnabled) {
            return;
        }
        ejecutarSeed();
        ejecutarSeedCatalogoSiVacio();
    }

    @Transactional
    public void ejecutarSeed() {
        USUARIOS_SEMILLA.forEach(usuarioService::crearUsuarioSemillaSiNoExiste);
    }

    @Transactional
    public void ejecutarSeedCatalogoSiVacio() {
        if (!productoService.listar().isEmpty()) {
            return;
        }

        CategoriaResponse catAlfajores = obtenerOCrearCategoria("Alfajores");
        CategoriaResponse catGolosinas = obtenerOCrearCategoria("Golosinas");
        CategoriaResponse catChupetines = obtenerOCrearCategoria("Chupetines");
        CategoriaResponse catChocolates = obtenerOCrearCategoria("Chocolates");

        SubCategoriaResponse subAlfajores = obtenerOCrearSubCategoria("Alfajores Bañados", catAlfajores.id());
        SubCategoriaResponse subGolosinas = obtenerOCrearSubCategoria("Gomitas y Caramelos", catGolosinas.id());
        SubCategoriaResponse subChupetines = obtenerOCrearSubCategoria("Chupetines Frutales", catChupetines.id());
        SubCategoriaResponse subChocolates = obtenerOCrearSubCategoria("Tabletas y Bombones", catChocolates.id());

        productoService.crear(new ProductoRequest(
                "ALF-001", "Alfajor triple chocolate", "Alfajor triple relleno con dulce de leche y baño de chocolate",
                "Caja x 24 u.", 24, true, null, subAlfajores.id(), 18900.0, 128
        ));
        productoService.crear(new ProductoRequest(
                "GOL-001", "Gomitas frutales", "Gomitas azucaradas sabores frutales surtidos",
                "Bolsa x 1 kg", 1, false, null, subGolosinas.id(), 6450.0, 84
        ));
        productoService.crear(new ProductoRequest(
                "CHU-001", "Chupetines surtidos", "Display exhibidor de chupetines frutales",
                "Display x 50 u.", 50, false, null, subChupetines.id(), 8200.0, 96
        ));
        productoService.crear(new ProductoRequest(
                "CHO-001", "Chocolate con maní", "Tableta de chocolate con leche y maní tostado",
                "Caja x 20 u.", 20, true, null, subChocolates.id(), 15600.0, 45
        ));
        productoService.crear(new ProductoRequest(
                "GOL-002", "Caramelos masticables", "Caramelos masticables frutales",
                "Bolsa x 800 g", 1, false, null, subGolosinas.id(), 5200.0, 120
        ));
        productoService.crear(new ProductoRequest(
                "ALF-002", "Alfajor de dulce de leche glaseado", "Alfajor clásico con baño de merengue italiano",
                "Caja x 24 u.", 24, false, null, subAlfajores.id(), 17400.0, 60
        ));
        productoService.crear(new ProductoRequest(
                "CHO-002", "Bombones surtidos", "Caja de bombones rellenos surtidos",
                "Caja x 30 u.", 30, false, null, subChocolates.id(), 22500.0, 35
        ));
        productoService.crear(new ProductoRequest(
                "GOL-003", "Turrón de maní crocante", "Turrón de maní clásico en display mayorista",
                "Display x 36 u.", 36, false, null, subGolosinas.id(), 9800.0, 110
        ));
    }

    private CategoriaResponse obtenerOCrearCategoria(String nombre) {
        return categoriaService.listar().stream()
                .filter(c -> c.nombre().equalsIgnoreCase(nombre))
                .findFirst()
                .orElseGet(() -> categoriaService.crear(new CategoriaRequest(nombre)));
    }

    private SubCategoriaResponse obtenerOCrearSubCategoria(String nombre, String categoriaId) {
        return subCategoriaService.listar().stream()
                .filter(s -> s.nombre().equalsIgnoreCase(nombre) && s.categoriaId().equals(categoriaId))
                .findFirst()
                .orElseGet(() -> subCategoriaService.crear(new SubCategoriaRequest(nombre, categoriaId)));
    }
}


