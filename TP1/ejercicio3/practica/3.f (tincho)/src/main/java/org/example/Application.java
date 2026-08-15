package org.example;

import org.example.entities.Cliente;
import org.example.entities.Factura;
import org.example.repositories.ClienteRepository;
import org.example.repositories.FacturaRepository;
import org.example.repositories.ProveedorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.math.BigInteger;
import java.util.List;
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

    // El método run se ejecuta automáticamente al iniciar la aplicación
    @Override
    public void run(String... args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        int opcion;

        System.out.println("-------- Conexión a H2 exitosa --------");

        do {
            System.out.println("\n============= MENÚ PRINCIPAL =============");
            System.out.println("1. Gestión de Clientes");
            System.out.println("2. Gestión de Proveedores");
            System.out.println("3. Gestión de Facturas");
            System.out.println("0. Salir del programa");
            System.out.print("Seleccione una opción: ");

            opcion = leerOpcion(scanner);

            switch (opcion) {
                case 1 -> menuCliente(scanner);
                case 2 -> menuProveedor(scanner);
                case 3 -> menuFactura(scanner);
                case 0 -> System.out.println("Saliendo del sistema");
                default -> System.out.println("Opción no valida.");
            }
        } while (opcion != 0);

        scanner.close();
        System.exit(0);
    }


    /**
     * Menú para CLIENTE
     */
    private void menuCliente(Scanner scanner) {
        int opcion;
        do {
            System.out.println("\n--- Módulo ABM: CLIENTE ---");
            System.out.println("1. Alta (Registrar nuevo)");
            System.out.println("2. Baja (Eliminar existente)");
            System.out.println("3. Modificación (Actualizar datos)");
            System.out.println("4. Listar");
            System.out.println("0. Volver al Menú Principal");
            System.out.print("Seleccione una acción: ");

            opcion = leerOpcion(scanner);

            switch (opcion) {
                case 1 -> {
                    // alta de clientes
                    System.out.println("\n-- Nuevo Cliente --");
                    System.out.print("Ingrese DNI: ");
                    String dni = scanner.nextLine().trim();

                    // Spring Data JPA da existsById para verificar si la primary key ya existe
                    if (clienteRepository.existsById(dni)) {
                        System.out.println("ya existe un cliente registrado con ese dni");
                        break;
                    }

                    System.out.print("Ingrese Nombre: ");
                    String nombre = scanner.nextLine();

                    System.out.print("Ingrese Apellido: ");
                    String apellido = scanner.nextLine();

                    // Instanciamos usando el patrón Builder
                    Cliente nuevoCliente = Cliente.builder()
                            .dni(dni)
                            .nombre(nombre)
                            .apellido(apellido)
                            .eliminado(false)
                            .build();

                    // save() genera el INSERT en SQL
                    clienteRepository.save(nuevoCliente);
                    System.out.println("cliente guardado con exito");
                }
                case 2 -> {
                    // baja de clientes
                    System.out.println("\n-- Eliminar Cliente --");
                    System.out.print("ingrese el dni del cliente a eliminar: ");
                    String dni = scanner.nextLine().trim();

                    if (clienteRepository.existsById(dni)) {
                        // deleteById() genera el DELETE FROM cliente WHERE dni = ?
                        clienteRepository.deleteById(dni);
                        System.out.println("cliente eliminado correctamente.");
                    } else {
                        System.out.println("no se encontró ningún cliente con ese dni");
                    }
                }
                case 3 -> {
                    // update de cliente
                    System.out.println("\n-- Modificar Cliente --");
                    System.out.print("Ingrese el DNI del cliente a modificar: ");
                    String dni = scanner.nextLine().trim();

                    // findById devuelve un Optional (caja que puede o no contener al cliente)
                    var clienteOpt = clienteRepository.findById(dni);

                    if (clienteOpt.isPresent()) {
                        // Extraemos el cliente
                        Cliente clienteAModificar = clienteOpt.get();
                        System.out.println("cliente actual: " + clienteAModificar.getNombre() + " " + clienteAModificar.getApellido());

                        System.out.print("ingrese nuevo nombre (dejar en blanco para mantener el actual): ");
                        String nuevoNombre = scanner.nextLine();
                        if (!nuevoNombre.trim().isEmpty()) {
                            clienteAModificar.setNombre(nuevoNombre);
                        }

                        System.out.print("ingrese nuevo apellido (dejar en blanco para mantener el actual): ");
                        String nuevoApellido = scanner.nextLine();
                        if (!nuevoApellido.trim().isEmpty()) {
                            clienteAModificar.setApellido(nuevoApellido);
                        }

                        // save() sobre objeto ya existente hace update no insert
                        clienteRepository.save(clienteAModificar);
                        System.out.println("datos del cliente actualizados con exito");
                    } else {
                        System.out.println("no se encontró ningún cliente con ese dni");
                    }
                }
                case 4 -> {
                    // listado de clientes
                    System.out.println("\n-- Lista de Clientes --");
                    java.util.List<Cliente> clientes = clienteRepository.findAll();

                    if(clientes.isEmpty()) {
                        System.out.println("No hay clientes registrados.");
                    } else {
                        System.out.printf("%-12s | %-20s | %-20s%n", "DNI", "NOMBRE", "APELLIDO");
                        System.out.println("----------------------------------------------------------");
                        for (Cliente c : clientes) {
                            // Cambiamos el %-12d por %-12s
                            System.out.printf("%-12s | %-20s | %-20s%n", c.getDni(), c.getNombre(), c.getApellido());
                        }
                    }
                }
                case 0 -> System.out.println("volviendo al menú principal");
                default -> System.out.println("Opción no válida");
            }
        } while (opcion != 0);
    }


    /**
     * Menú para PROVEEDOR
     */
    private void menuProveedor(Scanner scanner) {
        int opcion;
        do {
            System.out.println("\n--- Módulo ABM: PROVEEDOR ---");
            System.out.println("1. Alta (Registrar nuevo)");
            System.out.println("2. Baja (Eliminar existente)");
            System.out.println("3. Modificación (Actualizar datos)");
            System.out.println("4. Listar (Ver todos)");
            System.out.println("0. Volver al Menú Principal");
            System.out.print("Seleccione una acción: ");

            opcion = leerOpcion(scanner);

            switch (opcion) {
                case 1 -> {
                    // alta de proveedores
                    System.out.println("\n-- Nuevo Proveedor --");
                    System.out.print("Ingrese cuit: ");
                    String cuit = scanner.nextLine().trim();

                    if (proveedorRepository.existsById(cuit)) {
                        System.out.println("ya existe un proveedor registrado con ese cuit");
                        break;
                    }

                    System.out.print("Ingrese Razón Social: ");
                    String razonSocial = scanner.nextLine();

                    // Instancicions usando Lombok Builder
                    org.example.entities.Proveedor nuevoProveedor = org.example.entities.Proveedor.builder()
                            .cuit(cuit)
                            .razonSocial(razonSocial)
                            .eliminado(false)
                            .build();

                    proveedorRepository.save(nuevoProveedor);
                    System.out.println("proveedor guardado con exito");
                }
                case 2 -> {
                    // baja de proovedores
                    System.out.println("\n-- Eliminar Proveedor --");
                    System.out.print("Ingrese el CUIT del proveedor a eliminar: ");
                    String cuit = scanner.nextLine().trim();

                    if (proveedorRepository.existsById(cuit)) {
                        proveedorRepository.deleteById(cuit);
                        System.out.println("proveedor eliminado correctamente");
                    } else {
                        System.out.println("No se encontró ningún proveedor con ese cuit");
                    }
                }
                case 3 -> {
                    // update de proveedores
                    System.out.println("\n-- Modificar Proveedor --");
                    System.out.print("Ingrese el cuit del proveedor a modificar: ");
                    String cuit = scanner.nextLine().trim();

                    var proveedorOpt = proveedorRepository.findById(cuit);

                    if (proveedorOpt.isPresent()) {
                        org.example.entities.Proveedor proveedorAModificar = proveedorOpt.get();
                        System.out.println("Proveedor actual: " + proveedorAModificar.getRazonSocial());

                        System.out.print("Ingrese nueva Razón Social (deje en blanco para mantener la actual): ");
                        String nuevaRazonSocial = scanner.nextLine();
                        if (!nuevaRazonSocial.trim().isEmpty()) {
                            proveedorAModificar.setRazonSocial(nuevaRazonSocial);
                        }

                        proveedorRepository.save(proveedorAModificar);
                        System.out.println("Datos del proveedor actualizados con éxito");
                    } else {
                        System.out.println("NNo se encontró ningún proveedor con ese cuit.");
                    }
                }
                case 4 -> {
                    // listado de proveedores
                    System.out.println("\n-- Lista de Proveedores --");
                    java.util.List<org.example.entities.Proveedor> proveedores = proveedorRepository.findAll();

                    if(proveedores.isEmpty()) {
                        System.out.println("No hay proveedores registrados.");
                    } else {
                        System.out.printf("%-15s | %-30s%n", "CUIT", "RAZÓN SOCIAL");
                        System.out.println("-----------------------------------------------");
                        for (org.example.entities.Proveedor p : proveedores) {
                            // Cambiamos el %-15d por %-15s
                            System.out.printf("%-15s | %-30s%n", p.getCuit(), p.getRazonSocial());
                        }
                    }
                }
                case 0 -> System.out.println("volviendo al menú principal");
                default -> System.out.println(" opción no válida.");
            }
        } while (opcion != 0);
    }

    /**
     * Menú para FACTURA
     */
    private void menuFactura(Scanner scanner) {
        int opcion;
        do {
            System.out.println("\n--- Módulo ABM: FACTURA ---");
            System.out.println("1. Alta (Registrar nueva)");
            System.out.println("2. Listar todas las facturas");
            System.out.println("0. Volver");
            System.out.print("Seleccione: ");
            opcion = leerOpcion(scanner);

            switch (opcion) {
                case 1 -> {
                    System.out.println("\n-- Nueva Factura --");
                    System.out.println("1. Factura de Venta (A Cliente)");
                    System.out.println("2. Factura de Compra (A Proveedor)");
                    System.out.print("Seleccione el tipo: ");
                    int tipo = leerOpcion(scanner);

                    if (tipo != 1 && tipo != 2) {
                        System.out.println("tipo no válido.Cancelando alta");
                        break;
                    }

                    System.out.print("Ingrese el total a pagar: ");
                    double total = 0;
                    try {
                        total = Double.parseDouble(scanner.nextLine());
                    } catch (NumberFormatException e) {
                        System.out.println("monto inválido.");
                        break;
                    }

                    if (tipo == 1) {
                        // alta factura de venta
                        System.out.print("Ingrese DNI del Cliente existente: ");
                        String dni = scanner.nextLine().trim();

                        // Buscamos el cliente. findById devuelve un Optional
                        var clienteOpt = clienteRepository.findById(dni);

                        if (clienteOpt.isEmpty()) {
                            System.out.println("Error: No existe un cliente con ese dni. Regístrelo primero en el Menú de Clientes");
                            break;
                        }

                        // Usamos Lombok Builder para armar la entidad
                        Factura nuevaVenta = Factura.builder()
                                .tipo(org.example.enums.TipoFactura.VENTA)
                                .estado(org.example.enums.EstadoFactura.PENDIENTE)
                                .fecha(new java.util.Date())
                                .totalPagado(total)
                                .cliente(clienteOpt.get()) // .get() extrae el Cliente del Optional
                                .eliminado(false)
                                .build();

                        facturaRepository.save(nuevaVenta);
                        System.out.println("factura de Venta guardada con éxito");

                    } else {
                        // alta factura compra
                        System.out.print("Ingrese CUIT del Proveedor existente: ");
                        String cuit = scanner.nextLine().trim();

                        var proveedorOpt = proveedorRepository.findById(cuit);

                        if (proveedorOpt.isEmpty()) {
                            System.out.println("error: No existe un proveedor con ese CUIT.");
                            break;
                        }

                        Factura nuevaCompra = Factura.builder()
                                .tipo(org.example.enums.TipoFactura.COMPRA)
                                .estado(org.example.enums.EstadoFactura.PENDIENTE)
                                .fecha(new java.util.Date())
                                .totalPagado(total)
                                .proveedor(proveedorOpt.get())
                                .eliminado(false)
                                .build();

                        facturaRepository.save(nuevaCompra);
                        System.out.println("factura de compra guardada con éxito");
                    }
                }
                case 2 -> {
                    System.out.println("\n-- Lista de Facturas --");
                    List<Factura> facturas = facturaRepository.findAll();

                    if (facturas.isEmpty()) {
                        System.out.println("No hay facturas registradas.");
                    } else {
                        System.out.printf("%-38s | %-10s | %-12s | %-10s | %s%n", "ID", "TIPO", "ESTADO", "TOTAL", "ASOCIADO");
                        System.out.println("--------------------------------------------------------------------------------------------------");

                        for (Factura f : facturas) {
                            String asociado = "";
                            // se verifica el enumerador para saber a quién está asociada
                            if (f.getTipo() == org.example.enums.TipoFactura.VENTA && f.getCliente() != null) {
                                asociado = "DNI Cliente: " + f.getCliente().getDni();
                            } else if (f.getTipo() == org.example.enums.TipoFactura.COMPRA && f.getProveedor() != null) {
                                asociado = "CUIT Prov: " + f.getProveedor().getCuit();
                            }

                            System.out.printf("%-38s | %-10s | %-12s | $%-9.2f | %s%n",
                                    f.getId(), f.getTipo(), f.getEstado(), f.getTotalPagado(), asociado);
                        }
                    }
                }
            }
        } while (opcion != 0);
    }

    private int leerOpcion(Scanner scanner) {
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
