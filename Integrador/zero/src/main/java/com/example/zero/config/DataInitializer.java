package com.example.zero.config;

import com.example.zero.entidades.persona.Nacionalidad;
import com.example.zero.entidades.producto.Categoria;
import com.example.zero.entidades.producto.SubCategoria;
import com.example.zero.entidades.zona.Departamento;
import com.example.zero.entidades.zona.Localidad;
import com.example.zero.entidades.zona.Pais;
import com.example.zero.entidades.zona.Provincia;
import com.example.zero.enums.RolUsuario;
import com.example.zero.repositories.*;
import com.example.zero.services.CategoriaService;
import com.example.zero.services.producto.ProductoService;
import com.example.zero.services.ProveedorService;
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
    private final ProveedorRepository proveedorRepository;
    private final ProveedorService proveedorService;
    private final NacionalidadRepository nacionalidadRepository;
    private final PaisRepository paisRepository;
    private final ProvinciaRepository provinciaRepository;
    private final DepartamentoRepository departamentoRepository;
    private final LocalidadRepository localidadRepository;
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    public DataInitializer(UsuarioRepository usuarioRepository,
                           UsuarioService usuarioService,
                           CategoriaRepository categoriaRepository,
                           CategoriaService categoriaService,
                           SubCategoriaRepository subCategoriaRepository,
                           SubCategoriaService subCategoriaService,
                           ProductoRepository productoRepository,
                           ProductoService productoService,
                           ProveedorRepository proveedorRepository,
                           ProveedorService proveedorService,
                           NacionalidadRepository nacionalidadRepository,
                           PaisRepository paisRepository,
                           ProvinciaRepository provinciaRepository,
                           DepartamentoRepository departamentoRepository,
                           LocalidadRepository localidadRepository) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioService = usuarioService;
        this.categoriaRepository = categoriaRepository;
        this.categoriaService = categoriaService;
        this.subCategoriaRepository = subCategoriaRepository;
        this.subCategoriaService = subCategoriaService;
        this.productoRepository = productoRepository;
        this.productoService = productoService;
        this.proveedorRepository = proveedorRepository;
        this.proveedorService = proveedorService;
        this.nacionalidadRepository = nacionalidadRepository;
        this.paisRepository = paisRepository;
        this.provinciaRepository = provinciaRepository;
        this.departamentoRepository = departamentoRepository;
        this.localidadRepository = localidadRepository;
    }

    @Override
    public void run(String... args) {
        // 0. Corrección automática de esquema para MySQL (Factura / Producto / Imagen)
        try {
            if (jdbcTemplate != null) {
                try { jdbcTemplate.execute("ALTER TABLE factura MODIFY COLUMN cliente_id VARCHAR(36) NULL"); } catch (Exception ignored) {}
                try { jdbcTemplate.execute("ALTER TABLE factura MODIFY COLUMN proveedor_id VARCHAR(36) NULL"); } catch (Exception ignored) {}
                try { jdbcTemplate.execute("ALTER TABLE factura MODIFY COLUMN orden_compra_id VARCHAR(36) NULL"); } catch (Exception ignored) {}

                // Eliminar restricción obsoleta de CHECK en tabla imagen creada cuando TipoImagen solo contenía PERSONA
                try { jdbcTemplate.execute("ALTER TABLE imagen DROP CHECK imagen_chk_1"); } catch (Exception ignored) {}
                try { jdbcTemplate.execute("ALTER TABLE imagen DROP CONSTRAINT imagen_chk_1"); } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {
        }

        // 1. Usuarios de prueba
        try {
            if (usuarioRepository.findByNombreUsuarioAndEliminadoFalse("admin@zero.com").isEmpty()) {
                usuarioService.crearUsuario("admin@zero.com", "admin123", RolUsuario.ADMINISTRATIVO, null, true);
                System.out.println(">> [DataInitializer] Usuario administrador creado: admin@zero.com / admin123");
            } else {
                usuarioRepository.findByNombreUsuarioAndEliminadoFalse("admin@zero.com").ifPresent(u -> {
                    if (!u.isActivo()) { u.setActivo(true); usuarioRepository.save(u); }
                });
            }

            if (usuarioRepository.findByNombreUsuarioAndEliminadoFalse("cliente@zero.com").isEmpty()) {
                usuarioService.crearUsuario("cliente@zero.com", "cliente123", RolUsuario.CLIENTE, null, true);
                System.out.println(">> [DataInitializer] Usuario cliente creado: cliente@zero.com / cliente123");
            } else {
                usuarioRepository.findByNombreUsuarioAndEliminadoFalse("cliente@zero.com").ifPresent(u -> {
                    if (!u.isActivo()) { u.setActivo(true); usuarioRepository.save(u); }
                });
            }
        } catch (Exception e) {
            System.err.println(">> [DataInitializer] Error en usuarios iniciales: " + e.getMessage());
        }

        // 2. Categorías y Subcategorías de prueba
        Categoria catCalzado = null;
        Categoria catRopa = null;
        SubCategoria subZapatillas = null;
        SubCategoria subRemeras = null;
        SubCategoria subPantalones = null;
        try {
            catCalzado = categoriaRepository.findByNombreAndEliminadoFalse("Calzado Deportivo")
                    .orElseGet(() -> categoriaService.crearCategoria("Calzado Deportivo"));

            catRopa = categoriaRepository.findByNombreAndEliminadoFalse("Indumentaria")
                    .orElseGet(() -> categoriaService.crearCategoria("Indumentaria"));

            final Categoria finalCatCalzado = catCalzado;
            subZapatillas = subCategoriaRepository.findByNombreAndEliminadoFalse("Zapatillas Running")
                    .orElseGet(() -> subCategoriaService.crearSubCategoria("Zapatillas Running", finalCatCalzado.getId()));

            final Categoria finalCatRopa = catRopa;
            subRemeras = subCategoriaRepository.findByNombreAndEliminadoFalse("Remeras y Tops")
                    .orElseGet(() -> subCategoriaService.crearSubCategoria("Remeras y Tops", finalCatRopa.getId()));

            subPantalones = subCategoriaRepository.findByNombreAndEliminadoFalse("Pantalones y Joggers")
                    .orElseGet(() -> subCategoriaService.crearSubCategoria("Pantalones y Joggers", finalCatRopa.getId()));
        } catch (Exception e) {
            System.err.println(">> [DataInitializer] Error en categorías iniciales: " + e.getMessage());
        }

        // 3. Productos de prueba
        try {
            if (subZapatillas != null && productoRepository.findByCodigoAndEliminadoFalse("PROD-001").isEmpty()) {
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

            if (subRemeras != null && productoRepository.findByCodigoAndEliminadoFalse("PROD-002").isEmpty()) {
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

            if (subPantalones != null && productoRepository.findByCodigoAndEliminadoFalse("PROD-003").isEmpty()) {
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
            System.err.println(">> [DataInitializer] Error en productos iniciales: " + e.getMessage());
        }

        // 4. Proveedores de prueba (siempre se ejecutan aunque falle otro bloque)
        try {
            if (proveedorRepository.findByCuitAndEliminadoFalse("30-71234567-8").isEmpty()) {
                proveedorService.crearProveedor("Indumentaria Textil S.A.", "30-71234567-8");
                System.out.println(">> [DataInitializer] Proveedor inicial creado: Indumentaria Textil S.A.");
            }

            if (proveedorRepository.findByCuitAndEliminadoFalse("30-65432109-7").isEmpty()) {
                proveedorService.crearProveedor("Calzados Deportivos del Plata", "30-65432109-7");
                System.out.println(">> [DataInitializer] Proveedor inicial creado: Calzados Deportivos del Plata");
            }
        } catch (Exception e) {
            System.err.println(">> [DataInitializer] Error en proveedores iniciales: " + e.getMessage());
        }

        // 5. Nacionalidades de prueba
        try {
            if (nacionalidadRepository != null && nacionalidadRepository.count() == 0) {
                Nacionalidad nacArg = Nacionalidad.builder().id("nac-01").nombre("Argentina").eliminado(false).build();
                Nacionalidad nacBra = Nacionalidad.builder().id("nac-02").nombre("Brasileña").eliminado(false).build();
                Nacionalidad nacUry = Nacionalidad.builder().id("nac-03").nombre("Uruguaya").eliminado(false).build();
                Nacionalidad nacChl = Nacionalidad.builder().id("nac-04").nombre("Chilena").eliminado(false).build();
                nacionalidadRepository.save(nacArg);
                nacionalidadRepository.save(nacBra);
                nacionalidadRepository.save(nacUry);
                nacionalidadRepository.save(nacChl);
                System.out.println(">> [DataInitializer] Nacionalidades inicializadas");
            }
        } catch (Exception e) {
            System.err.println(">> [DataInitializer] Error en nacionalidades iniciales: " + e.getMessage());
        }

        // 6. Jerarquía geográfica inicial (País -> Provincia -> Departamento -> Localidad)
        try {
            if (paisRepository != null && paisRepository.count() == 0) {
                Pais arg = new Pais(); arg.setId("pais-arg"); arg.setNombre("Argentina"); arg.setEliminado(false);
                Pais bra = new Pais(); bra.setId("pais-bra"); bra.setNombre("Brasil"); bra.setEliminado(false);
                Pais ury = new Pais(); ury.setId("pais-ury"); ury.setNombre("Uruguay"); ury.setEliminado(false);
                paisRepository.save(arg);
                paisRepository.save(bra);
                paisRepository.save(ury);

                // Provincias
                Provincia cba = new Provincia(); cba.setId("prov-arg-cba"); cba.setNombre("Córdoba"); cba.setPais(arg); cba.setEliminado(false);
                Provincia bue = new Provincia(); bue.setId("prov-arg-bue"); bue.setNombre("Buenos Aires"); bue.setPais(arg); bue.setEliminado(false);
                Provincia sfe = new Provincia(); sfe.setId("prov-arg-sfe"); sfe.setNombre("Santa Fe"); sfe.setPais(arg); sfe.setEliminado(false);
                provinciaRepository.save(cba);
                provinciaRepository.save(bue);
                provinciaRepository.save(sfe);

                // Departamentos Córdoba
                Departamento depCap = new Departamento(); depCap.setId("dep-cba-cap"); depCap.setNombre("Capital"); depCap.setProvincia(cba); depCap.setEliminado(false);
                Departamento depCol = new Departamento(); depCol.setId("dep-cba-col"); depCol.setNombre("Colón"); depCol.setProvincia(cba); depCol.setEliminado(false);
                Departamento depPun = new Departamento(); depPun.setId("dep-cba-pun"); depPun.setNombre("Punilla"); depPun.setProvincia(cba); depPun.setEliminado(false);
                departamentoRepository.save(depCap);
                departamentoRepository.save(depCol);
                departamentoRepository.save(depPun);

                // Departamentos Buenos Aires
                Departamento depLp = new Departamento(); depLp.setId("dep-bue-lp"); depLp.setNombre("La Plata"); depLp.setProvincia(bue); depLp.setEliminado(false);
                Departamento depGp = new Departamento(); depGp.setId("dep-bue-gp"); depGp.setNombre("General Pueyrredón"); depGp.setProvincia(bue); depGp.setEliminado(false);
                departamentoRepository.save(depLp);
                departamentoRepository.save(depGp);

                // Localidades
                Localidad locCba = new Localidad(); locCba.setId("loc-cba-cba"); locCba.setNombre("Córdoba Ciudad"); locCba.setCodigoPostal("5000"); locCba.setDepartamento(depCap); locCba.setEliminado(false);
                Localidad locVa = new Localidad(); locVa.setId("loc-cba-va"); locVa.setNombre("Villa Allende"); locVa.setCodigoPostal("5105"); locVa.setDepartamento(depCol); locVa.setEliminado(false);
                Localidad locVcp = new Localidad(); locVcp.setId("loc-cba-vcp"); locVcp.setNombre("Villa Carlos Paz"); locVcp.setCodigoPostal("5152"); locVcp.setDepartamento(depPun); locVcp.setEliminado(false);
                Localidad locLp = new Localidad(); locLp.setId("loc-bue-lp"); locLp.setNombre("La Plata"); locLp.setCodigoPostal("1900"); locLp.setDepartamento(depLp); locLp.setEliminado(false);
                Localidad locMdp = new Localidad(); locMdp.setId("loc-bue-mdp"); locMdp.setNombre("Mar del Plata"); locMdp.setCodigoPostal("7600"); locMdp.setDepartamento(depGp); locMdp.setEliminado(false);
                localidadRepository.save(locCba);
                localidadRepository.save(locVa);
                localidadRepository.save(locVcp);
                localidadRepository.save(locLp);
                localidadRepository.save(locMdp);
                System.out.println(">> [DataInitializer] Jerarquía geográfica inicializada");
            }
        } catch (Exception e) {
            System.err.println(">> [DataInitializer] Error en geografía inicial: " + e.getMessage());
        }
    }
}
