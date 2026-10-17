package com.example.carrito.config;

import com.example.carrito.entidades.Articulo;
import com.example.carrito.entidades.Direccion;
import com.example.carrito.entidades.Imagen;
import com.example.carrito.entidades.Proveedor;
import com.example.carrito.entidades.Usuario;
import com.example.carrito.entidades.enumeraciones.Rol;
import com.example.carrito.repositorios.ArticuloRepositorio;
import com.example.carrito.repositorios.ImagenRepositorio;
import com.example.carrito.repositorios.ProveedorRepositorio;
import com.example.carrito.repositorios.UsuarioRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    @Autowired
    private ProveedorRepositorio proveedorRepositorio;

    @Autowired
    private ArticuloRepositorio articuloRepositorio;

    @Autowired
    private ImagenRepositorio imagenRepositorio;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Inicializar Usuarios demo
        if (usuarioRepositorio.count() == 0) {
            Usuario admin = Usuario.builder()
                    .nombre("Administrador BloomShop")
                    .email("admin@bloomshop.com")
                    .clave(passwordEncoder.encode("admin123"))
                    .rol(Rol.ADMIN)
                    .eliminado(false)
                    .build();
            usuarioRepositorio.save(admin);

            Usuario cliente = Usuario.builder()
                    .nombre("Cliente Demo")
                    .email("cliente@bloomshop.com")
                    .clave(passwordEncoder.encode("cliente123"))
                    .rol(Rol.CLIENTE)
                    .eliminado(false)
                    .build();
            usuarioRepositorio.save(cliente);
        }

        // Inicializar Proveedores demo con sus respectivas Direcciones (latitud y longitud)
        if (proveedorRepositorio.count() == 0) {
            Proveedor prov1 = proveedorRepositorio.save(Proveedor.builder()
                    .nombre("Vivero Las Lilas")
                    .direccion(Direccion.builder()
                            .latitud(-32.8895)
                            .longitud(-68.8458)
                            .eliminado(false)
                            .build())
                    .eliminado(false)
                    .build());

            Proveedor prov2 = proveedorRepositorio.save(Proveedor.builder()
                    .nombre("Botánica Primavera")
                    .direccion(Direccion.builder()
                            .latitud(-32.9012)
                            .longitud(-68.8584)
                            .eliminado(false)
                            .build())
                    .eliminado(false)
                    .build());

            Proveedor prov3 = proveedorRepositorio.save(Proveedor.builder()
                    .nombre("EcoFlores Mendoza")
                    .direccion(Direccion.builder()
                            .latitud(-32.8754)
                            .longitud(-68.8321)
                            .eliminado(false)
                            .build())
                    .eliminado(false)
                    .build());

            // Crear imágenes SVG placeholder como bytes
            Imagen img1 = crearImagenSvg("Rosas", "#f43f5e", "Ramo Rosas");
            Imagen img2 = crearImagenSvg("Orquídea", "#8b5cf6", "Orquídea Zen");
            Imagen img3 = crearImagenSvg("Tulipanes", "#f59e0b", "Tulipanes");
            Imagen img4 = crearImagenSvg("Suculentas", "#10b981", "Suculentas");

            // Inicializar Artículos demo
            articuloRepositorio.save(Articulo.builder()
                    .nombre("Ramo de Rosas Silvestres")
                    .precio(12500.0)
                    .proveedor(prov1)
                    .imagen(img1)
                    .eliminado(false)
                    .build());

            articuloRepositorio.save(Articulo.builder()
                    .nombre("Orquídea Imperial Blanca")
                    .precio(24000.0)
                    .proveedor(prov2)
                    .imagen(img2)
                    .eliminado(false)
                    .build());

            articuloRepositorio.save(Articulo.builder()
                    .nombre("Arreglo de Tulipanes Primaverales")
                    .precio(18500.0)
                    .proveedor(prov3)
                    .imagen(img3)
                    .eliminado(false)
                    .build());

            articuloRepositorio.save(Articulo.builder()
                    .nombre("Jardín Zen de Suculentas")
                    .precio(9900.0)
                    .proveedor(prov1)
                    .imagen(img4)
                    .eliminado(false)
                    .build());
        } else {
            // Si la base de datos ya tenía proveedores creados antes de agregar Direccion,
            // se les asignan coordenadas por defecto para que se visualicen en el mapa.
            double[][] coordsDemo = {
                    {-32.8895, -68.8458},
                    {-32.9012, -68.8584},
                    {-32.8754, -68.8321}
            };
            var existentes = proveedorRepositorio.findAll();
            for (int i = 0; i < existentes.size(); i++) {
                Proveedor p = existentes.get(i);
                if (p.getDireccion() == null) {
                    double[] c = coordsDemo[i % coordsDemo.length];
                    p.setDireccion(Direccion.builder()
                            .latitud(c[0])
                            .longitud(c[1])
                            .eliminado(false)
                            .build());
                    proveedorRepositorio.save(p);
                }
            }
        }
    }

    private Imagen crearImagenSvg(String texto, String colorHex, String nombre) {
        String svg = "<svg xmlns='http://www.w3.org/2000/svg' width='400' height='400' viewBox='0 0 400 400'>" +
                "<rect width='400' height='400' fill='" + colorHex + "' opacity='0.15'/>" +
                "<circle cx='200' cy='180' r='80' fill='" + colorHex + "' opacity='0.3'/>" +
                "<text x='200' y='200' font-family='sans-serif' font-size='26' font-weight='bold' fill='" + colorHex + "' text-anchor='middle'>" + texto + "</text>" +
                "<text x='200' y='320' font-family='sans-serif' font-size='16' fill='#64748b' text-anchor='middle'>BloomShop Boutique</text>" +
                "</svg>";

        Imagen img = Imagen.builder()
                .nombre(nombre + ".svg")
                .mime("image/svg+xml")
                .contenido(svg.getBytes(StandardCharsets.UTF_8))
                .eliminado(false)
                .build();

        return imagenRepositorio.save(img);
    }
}

