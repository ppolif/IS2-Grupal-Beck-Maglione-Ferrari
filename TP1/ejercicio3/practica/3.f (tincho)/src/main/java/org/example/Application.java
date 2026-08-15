package org.example;

import org.example.entities.Producto;
import org.example.repositories.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application implements CommandLineRunner {

    // Inyectamos un repositorio para hacer la prueba de base de datos
    @Autowired
    private ProductoRepository productoRepository;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("--------Conexion a H2 exitosa");

        // Hacemos una prueba rápida creando y guardando un producto
        System.out.println("--------Insertamos producto de prueba: monitor samsung");
        Producto productoPrueba = Producto.builder()
                .nombre("Monitor 24 Pulgadas")
                .marca("Samsung")
                .build();

        Producto productoGuardado = productoRepository.save(productoPrueba);

        // Guardamos el id para mas tarde verificar que esta dentro de la bbdd
        String idGenerado = productoGuardado.getId();

        // Recuperamos el producto DESDE la base de datos por su ID
        Producto productoDesdeBbdd = productoRepository.findById(idGenerado)
                .orElseThrow(() -> new RuntimeException("No se encontró el producto en la BBDD"));

        // Traemos e imprimimos
        System.out.println("--------Traemos el producto ingresado para verificar:");
        System.out.println("-----------------------------------------------------");
        System.out.println("ID: " + productoDesdeBbdd.getId());
        System.out.println("Nombre: " + productoDesdeBbdd.getNombre());
        System.out.println("Marca: " + productoDesdeBbdd.getMarca());
        System.out.println("Estado(Eliminado): " + productoDesdeBbdd.isEliminado());
        System.out.println("-----------------------------------------------------");
        System.out.println("-----Total de productos en la BD: " + productoRepository.count());
        System.out.println("=====================================================\n");
    }
    }
