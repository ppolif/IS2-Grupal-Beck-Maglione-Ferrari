package org.example;

//import org.example.entities.Detalle;
//import org.example.entities.Factura;
//import org.example.entities.Stock;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//import java.util.Scanner;

@SpringBootApplication
public class Application {
//    // Inyectamos nuestro repositorio unificado
//    @Autowired
//    private FacturaRepository facturaRepository;
//
//    public static void main(String[] args) {
//        SpringApplication.run(Application.class, args);
//    }
//
//    @Override
//    public void run(String... args) throws Exception {
//        System.out.println("--- INICIANDO EL SISTEMA ---");
//
//        // 1. Simular la creación de una Factura (Lo que antes hacía el Controller por POST)
//        FacturaVenta venta = FacturaVenta.builder()
//                .fecha(new Date())
//                .totalPagado(1500.50)
//                .estado(EstadoFactura.PAGADA)
//                .nombreClienteTemporal("Juan Perez")
//                .eliminado(false)
//                .build();
//
//        facturaRepository.save(venta);
//        System.out.println("Factura de venta guardada con ID: " + venta.getId());
//
//        // 2. Probar nuestra lógica de negocio colapsada (Baja lógica)
//        boolean exito = facturaRepository.aplicarBajaLogica(venta.getId());
//        if (exito) {
//            System.out.println("La factura fue dada de baja lógicamente.");
//        }
//    }
}