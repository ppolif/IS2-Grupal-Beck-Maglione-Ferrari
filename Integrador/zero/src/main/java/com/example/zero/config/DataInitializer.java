package com.example.zero.config;

import com.example.zero.entidades.producto.Categoria;
import com.example.zero.entidades.producto.SubCategoria;
import com.example.zero.enums.RolUsuario;
import com.example.zero.repositories.CategoriaRepository;
import com.example.zero.repositories.ProductoRepository;
import com.example.zero.repositories.SubCategoriaRepository;
import com.example.zero.repositories.UsuarioRepository;
import com.example.zero.services.CategoriaService;
import com.example.zero.services.ProductoService;
import com.example.zero.services.SubCategoriaService;
import com.example.zero.services.persona.UsuarioService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;
    private final CategoriaRepository categoriaRepository;
    private final CategoriaService categoriaService;
    private final SubCategoriaRepository subCategoriaRepository;
    private final SubCategoriaService subCategoriaService;
    private final ProductoRepository productoRepository;
    private final ProductoService productoService;

    public DataInitializer(UsuarioRepository usuarioRepository,
                           UsuarioService usuarioService,
                           CategoriaRepository categoriaRepository,
                           CategoriaService categoriaService,
                           SubCategoriaRepository subCategoriaRepository,
                           SubCategoriaService subCategoriaService,
                           ProductoRepository productoRepository,
                           ProductoService productoService) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioService = usuarioService;
        this.categoriaRepository = categoriaRepository;
        this.categoriaService = categoriaService;
        this.subCategoriaRepository = subCategoriaRepository;
        this.subCategoriaService = subCategoriaService;
        this.productoRepository = productoRepository;
        this.productoService = productoService;
    }

    @Override
    public void run(String... args) {
        try {
            // 1. Usuarios de prueba
            if (usuarioRepository.findByNombreUsuarioAndEliminadoFalse("admin@zero.com").isEmpty()) {
                usuarioService.crearUsuario("admin@zero.com", "admin123", RolUsuario.ADMINISTRATIVO, null);
                System.out.println(">> [DataInitializer] Usuario administrador creado: admin@zero.com / admin123");
            }

            if (usuarioRepository.findByNombreUsuarioAndEliminadoFalse("cliente@zero.com").isEmpty()) {
                usuarioService.crearUsuario("cliente@zero.com", "cliente123", RolUsuario.CLIENTE, null);
                System.out.println(">> [DataInitializer] Usuario cliente creado: cliente@zero.com / cliente123");
            }

            // 2. Categorías y Subcategorías de prueba
            Categoria catCalzado = categoriaRepository.findByNombreAndEliminadoFalse("Calzado Deportivo")
                    .orElseGet(() -> categoriaService.crearCategoria("Calzado Deportivo"));

            Categoria catRopa = categoriaRepository.findByNombreAndEliminadoFalse("Indumentaria")
                    .orElseGet(() -> categoriaService.crearCategoria("Indumentaria"));

            SubCategoria subZapatillas = subCategoriaRepository.findByNombreAndEliminadoFalse("Zapatillas Running")
                    .orElseGet(() -> subCategoriaService.crearSubCategoria("Zapatillas Running", catCalzado.getId()));

            SubCategoria subRemeras = subCategoriaRepository.findByNombreAndEliminadoFalse("Remeras y Tops")
                    .orElseGet(() -> subCategoriaService.crearSubCategoria("Remeras y Tops", catRopa.getId()));

            SubCategoria subPantalones = subCategoriaRepository.findByNombreAndEliminadoFalse("Pantalones y Joggers")
                    .orElseGet(() -> subCategoriaService.crearSubCategoria("Pantalones y Joggers", catRopa.getId()));

            // 3. Productos de prueba
            if (productoRepository.findByCodigoAndEliminadoFalse("PROD-001").isEmpty()) {
                productoService.crearProducto(
                        "PROD-001",
                        "Zero Velocity Nitro",
                        "Calzado ultraligero con placa de propulsión y suela de máxima tracción para maratones.",
                        "42",
                        subZapatillas.getId(),
                        149.99,
                        true
                );
                System.out.println(">> [DataInitializer] Producto inicial creado: PROD-001 (Zero Velocity Nitro)");
            }

            if (productoRepository.findByCodigoAndEliminadoFalse("PROD-002").isEmpty()) {
                productoService.crearProducto(
                        "PROD-002",
                        "Remera Zero Pro Breathable",
                        "Camiseta de alta respirabilidad con costuras planas antirozaduras para entrenamientos.",
                        "M",
                        subRemeras.getId(),
                        45.50,
                        false
                );
                System.out.println(">> [DataInitializer] Producto inicial creado: PROD-002 (Remera Zero Pro Breathable)");
            }

            if (productoRepository.findByCodigoAndEliminadoFalse("PROD-003").isEmpty()) {
                productoService.crearProducto(
                        "PROD-003",
                        "Pantalón Jogger Dry-Fit",
                        "Pantalón deportivo con bolsillos con cierre y ajuste térmico elástico.",
                        "L",
                        subPantalones.getId(),
                        68.00,
                        true
                );
                System.out.println(">> [DataInitializer] Producto inicial creado: PROD-003 (Pantalón Jogger Dry-Fit)");
            }

        } catch (Exception e) {
            System.err.println(">> [DataInitializer] Advertencia al inicializar datos de prueba: " + e.getMessage());
        }
    }
}
