package org.example;

import org.example.entities.Cliente;
import org.example.entities.Factura;
import org.example.entities.Proveedor;
import org.example.repositories.ClienteRepository;
import org.example.repositories.FacturaRepository;
import org.example.repositories.ProveedorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

@SpringBootApplication
public class Application implements CommandLineRunner {
    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ProveedorRepository proveedorRepository;

    @Autowired
    private FacturaRepository facturaRepository;

    public static void main(String[] args) {
        // Levanta el contexto de Spring Boot
        SpringApplication.run(Application.class, args);
    }

    // El metodo run se ejecuta al iniciar la aplicación
    @Override
    public void run(String... args) throws Exception {
        Scanner scanner = new Scanner(System.in);

        /////////hacemos abm de proveedor///////

        //Alta
        //usamos builder de lombok y el metodo save del repo de proveedor
        Proveedor nuevoProveedor = Proveedor.builder()
                .cuit("4900000")
                .razonSocial("Virgen del Valle")
                .eliminado(false)
                .build();

        proveedorRepository.save(nuevoProveedor);

        //Modificacion
        //digamos que primero hay que buscarlo en la base de datos
        // usamos el metodo finById del repositorio
        Optional<Proveedor> proveedorBuscado = proveedorRepository.findById("4900000");

        if (proveedorBuscado.isPresent()) {
            Proveedor encontrado = proveedorBuscado.get();

            encontrado.setRazonSocial("nuevo nombre");
        }

        //Baja
        // Elimina el objeto de la base de datos
        proveedorRepository.delete(nuevoProveedor);

        /////////hacemos abm de proveedor///////

        //Alta
        Cliente cliente = Cliente.builder()
                .dni("45000000")
                .nombre("Augusto")
                .apellido("Beck")
                .eliminado(false)
                .build();

        clienteRepository.save(cliente);

        //Modificacion
        Optional<Cliente> clienteBuscado = clienteRepository.findById("45000000");

        if (clienteBuscado.isPresent()) {
            Cliente encontrado2 = clienteBuscado.get();

            encontrado2.setNombre("Camilo");
        }

        //Baja
        clienteRepository.delete(cliente);

    }
}
