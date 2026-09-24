package com.example.zero.services;

import com.example.zero.entidades.compraCliente.DetalleCompra;
import com.example.zero.entidades.compraCliente.OrdenCompra;
import com.example.zero.entidades.persona.Cliente;
import com.example.zero.entidades.persona.Nacionalidad;
import com.example.zero.entidades.persona.Usuario;
import com.example.zero.entidades.producto.Producto;
import com.example.zero.enums.EstadoOrdenCompra;
<<<<<<< HEAD
import com.example.zero.enums.TipoDocumento;
import com.example.zero.repositories.*;
=======
import com.example.zero.enums.RolUsuario;
import com.example.zero.enums.TipoDocumento;
import com.example.zero.repositories.*;
import com.example.zero.services.producto.ProductoService;
>>>>>>> augusto
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

/**
 * Servicio para la gestión del carrito de compras y órdenes de compra del cliente.
 * Representa la orden de compra en estado PENDIENTE_COMPLETAR como el carrito persistente del cliente.
 * Opera directamente sobre las entidades del dominio sin utilizar DTOs intermedios.
 * Toda la lógica de negocio (cálculo de totales, filtrado de ítems activos) reside en este servicio.
 */
@Service
@RequiredArgsConstructor
public class OrdenCompraService {

    private final OrdenCompraRepository ordenCompraRepository;
    private final DetalleCompraRepository detalleCompraRepository;
    private final ProductoRepository productoRepository;
    private final ProductoService productoService;
    private final ClienteRepository clienteRepository;
    private final NacionalidadRepository nacionalidadRepository;
    private final UsuarioRepository usuarioRepository;

    /**
     * Recalcula y asigna el total acumulado de una orden de compra sumando los subtotales
     * de los detalles activos (no eliminados).
     */
    public void recalcularTotal(OrdenCompra ordenCompra) {
        if (ordenCompra == null) return;
        if (ordenCompra.getDetalles() == null) {
            ordenCompra.setTotal(0.0);
            return;
        }
        double sum = ordenCompra.getDetalles().stream()
                .filter(d -> !d.isEliminado())
                .mapToDouble(DetalleCompra::getSubtotal)
                .sum();
        ordenCompra.setTotal(Math.round(sum * 100.0) / 100.0);
    }

    /**
     * Recalcula y asigna el subtotal de un detalle de compra (cantidad * precio unitario).
     */
    public void recalcularSubtotal(DetalleCompra detalle) {
        if (detalle == null) return;
        detalle.setSubtotal(Math.round((detalle.getCantidad() * detalle.getPrecioUnitario()) * 100.0) / 100.0);
    }

    /**
     * Retorna la lista de ítems (detalles de compra) activos (no eliminados) de una orden.
     */
    public List<DetalleCompra> obtenerItemsActivos(OrdenCompra ordenCompra) {
        if (ordenCompra == null || ordenCompra.getDetalles() == null) {
            return Collections.emptyList();
        }
        return ordenCompra.getDetalles().stream()
                .filter(d -> !d.isEliminado())
                .toList();
    }

    /**
     * Cuenta la cantidad total de unidades contenidas en la orden de compra especificada.
     */
    public int contarItems(OrdenCompra ordenCompra) {
        if (ordenCompra == null || ordenCompra.getDetalles() == null) {
            return 0;
        }
        return ordenCompra.getDetalles().stream()
                .filter(d -> !d.isEliminado())
                .mapToInt(DetalleCompra::getCantidad)
                .sum();
    }

    /**
     * Cuenta la cantidad de unidades totales presentes en el carrito activo del cliente.
     */
    @Transactional(readOnly = true)
    public int contarItems(Cliente cliente) {
        if (cliente == null) return 0;
        OrdenCompra carrito = obtenerOCrearCarrito(cliente);
        return contarItems(carrito);
    }

    /**
     * Obtiene o asocia el Cliente correspondiente a un Usuario autenticado.
<<<<<<< HEAD
=======
     * Reasocia el usuario a la sesión activa de JPA para evitar LazyInitializationException
     * al consultar la relación ManyToOne con Persona/Cliente.
>>>>>>> augusto
     */
    @Transactional
    public Cliente obtenerOAsociarCliente(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("Usuario no autenticado");
        }
<<<<<<< HEAD

        if (usuario.getPersona() != null) {
            String doc = usuario.getPersona().getNumeroDocumento();
            Optional<Cliente> clienteOpt = clienteRepository.findByNumeroDocumentoAndEliminadoFalse(doc);
            if (clienteOpt.isPresent()) {
                return clienteOpt.get();
            }
            if (usuario.getPersona() instanceof Cliente) {
                return (Cliente) usuario.getPersona();
            }
        }

        String doc = "CLI-" + (usuario.getId() != null ? usuario.getId().replace("-", "").substring(0, Math.min(10, usuario.getId().replace("-", "").length())) : UUID.randomUUID().toString().substring(0, 8));
        Optional<Cliente> existente = clienteRepository.findByNumeroDocumentoAndEliminadoFalse(doc);
        if (existente.isPresent()) {
            Cliente cliente = existente.get();
            usuario.setPersona(cliente);
            usuarioRepository.save(usuario);
=======
        if (usuario.getRol() != RolUsuario.CLIENTE) {
            throw new IllegalArgumentException("Solo los usuarios con rol CLIENTE pueden poseer o gestionar un carrito de compras.");
        }

        // Reasociar usuario a la sesión de persistencia actual
        Usuario uPersistente = null;
        if (usuario.getId() != null) {
            uPersistente = usuarioRepository.findById(usuario.getId()).orElse(null);
        }
        if (uPersistente == null && usuario.getNombreUsuario() != null) {
            uPersistente = usuarioRepository.findByNombreUsuarioAndEliminadoFalse(usuario.getNombreUsuario()).orElse(null);
        }
        if (uPersistente == null) {
            uPersistente = usuario;
        }

        // 1. Si el usuario ya tiene asociada una Persona (Cliente)
        if (uPersistente.getPersona() != null) {
            if (uPersistente.getPersona() instanceof Cliente) {
                return (Cliente) uPersistente.getPersona();
            }
            String doc = uPersistente.getPersona().getNumeroDocumento();
            if (doc != null) {
                Optional<Cliente> clienteOpt = clienteRepository.findByNumeroDocumentoAndEliminadoFalse(doc);
                if (clienteOpt.isPresent()) {
                    return clienteOpt.get();
                }
            }
        }

        // 2. Si es un usuario cliente sin persona asociada previa (ej. usuario semilla):
        String doc = "CLI-" + (uPersistente.getId() != null
                ? uPersistente.getId().replace("-", "").substring(0, Math.min(10, uPersistente.getId().replace("-", "").length()))
                : UUID.randomUUID().toString().substring(0, 8));

        Optional<Cliente> existente = clienteRepository.findByNumeroDocumentoAndEliminadoFalse(doc);
        if (existente.isPresent()) {
            Cliente cliente = existente.get();
            uPersistente.setPersona(cliente);
            usuarioRepository.save(uPersistente);
>>>>>>> augusto
            return cliente;
        }

        Nacionalidad nacionalidad = nacionalidadRepository.findByEliminadoFalse().stream().findFirst().orElseGet(() -> {
<<<<<<< HEAD
            Nacionalidad nac = Nacionalidad.builder().nombre("Argentina").eliminado(false).build();
            return nacionalidadRepository.save(nac);
        });

        String nombreUsuario = usuario.getNombreUsuario() != null ? usuario.getNombreUsuario() : "Cliente";
=======
            Nacionalidad nac = Nacionalidad.builder().id("nac-01").nombre("Argentina").eliminado(false).build();
            return nacionalidadRepository.save(nac);
        });

        String nombreUsuario = uPersistente.getNombreUsuario() != null ? uPersistente.getNombreUsuario() : "Cliente";
>>>>>>> augusto
        Cliente nuevoCliente = Cliente.builder()
                .numeroDocumento(doc)
                .nombre(nombreUsuario.contains("@") ? nombreUsuario.substring(0, nombreUsuario.indexOf("@")) : nombreUsuario)
                .apellido("Cliente")
                .fechaNacimiento(LocalDate.of(2000, 1, 1))
                .tipoDocumento(TipoDocumento.DNI)
                .nacionalidad(nacionalidad)
                .eliminado(false)
                .build();

        nuevoCliente = clienteRepository.save(nuevoCliente);
<<<<<<< HEAD
        usuario.setPersona(nuevoCliente);
        usuarioRepository.save(usuario);
=======
        uPersistente.setPersona(nuevoCliente);
        usuarioRepository.save(uPersistente);
>>>>>>> augusto
        return nuevoCliente;
    }

    /**
     * Obtiene el carrito activo (OrdenCompra en PENDIENTE_COMPLETAR) del cliente,
     * o crea uno nuevo si aún no existe.
     */
    @Transactional
    public OrdenCompra obtenerOCrearCarrito(Cliente cliente) {
        if (cliente == null) {
            throw new IllegalArgumentException("El cliente no puede ser nulo");
        }

        Optional<OrdenCompra> carritoExistente = ordenCompraRepository
                .findByClienteAndEstadoOrdenCompraAndEliminadoFalse(cliente, EstadoOrdenCompra.PENDIENTE_COMPLETAR);

        if (carritoExistente.isPresent()) {
            OrdenCompra carrito = carritoExistente.get();
            if (carrito.getDetalles() == null) {
                carrito.setDetalles(new ArrayList<>());
            }
            recalcularTotal(carrito);
            return carrito;
        }

        OrdenCompra nuevoCarrito = OrdenCompra.builder()
                .identificadorCompra("CART-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .fecha(new Date())
                .total(0.0)
                .estadoOrdenCompra(EstadoOrdenCompra.PENDIENTE_COMPLETAR)
                .cliente(cliente)
                .eliminado(false)
                .detalles(new ArrayList<>())
                .build();

        return ordenCompraRepository.save(nuevoCarrito);
    }

    /**
     * Agrega un producto al carrito persistente. Si ya existe, incrementa la cantidad.
     */
    @Transactional
    public OrdenCompra agregarProducto(Cliente cliente, String productoId, int cantidad) {
        if (cliente == null) {
            throw new IllegalArgumentException("El cliente no puede ser nulo");
        }
        if (productoId == null || productoId.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del producto no puede estar vacío");
        }
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        }

        Producto producto = productoRepository.findActive(productoId.trim())
                .or(() -> productoRepository.findByCodigoAndEliminadoFalse(productoId.trim()))
                .orElseGet(() -> {
                    List<Producto> activos = productoRepository.findByEliminadoFalse();
                    if (!activos.isEmpty()) {
                        try {
                            int index = Integer.parseInt(productoId.trim()) - 1;
                            if (index >= 0 && index < activos.size()) {
                                return activos.get(index);
                            }
                        } catch (NumberFormatException ignored) {}
                        return activos.get(0);
                    }
                    throw new IllegalArgumentException("No se encontró el producto activo con ID: " + productoId);
                });

        double precioUnitario;
        try {
            precioUnitario = productoService.obtenerPrecioActual(producto.getId());
        } catch (Exception e) {
            Double pActual = producto.getPrecioActual();
            precioUnitario = (pActual != null && pActual > 0) ? pActual : 100.0;
        }

        OrdenCompra carrito = obtenerOCrearCarrito(cliente);

        Optional<DetalleCompra> detalleExistente = carrito.getDetalles().stream()
                .filter(d -> !d.isEliminado() && d.getProducto() != null && d.getProducto().getId().equals(producto.getId()))
                .findFirst();

        if (detalleExistente.isPresent()) {
            DetalleCompra detalle = detalleExistente.get();
            detalle.setCantidad(detalle.getCantidad() + cantidad);
            detalle.setPrecioUnitario(precioUnitario);
            recalcularSubtotal(detalle);
            detalleCompraRepository.save(detalle);
        } else {
            DetalleCompra nuevoDetalle = DetalleCompra.builder()
                    .ordenCompra(carrito)
                    .producto(producto)
                    .cantidad(cantidad)
                    .precioUnitario(precioUnitario)
                    .subtotal(Math.round((precioUnitario * cantidad) * 100.0) / 100.0)
                    .eliminado(false)
                    .build();
            DetalleCompra guardado = detalleCompraRepository.save(nuevoDetalle);
            carrito.getDetalles().add(guardado);
        }

        recalcularTotal(carrito);
        carrito.setFecha(new Date());
        return ordenCompraRepository.save(carrito);
    }

    /**
     * Actualiza la cantidad de un ítem en el carrito persistente.
     */
    @Transactional
    public OrdenCompra actualizarCantidad(Cliente cliente, String detalleId, int nuevaCantidad) {
        if (cliente == null) {
            throw new IllegalArgumentException("El cliente no puede ser nulo");
        }
        if (detalleId == null || detalleId.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del ítem no puede estar vacío");
        }
        if (nuevaCantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        }

        OrdenCompra carrito = obtenerOCrearCarrito(cliente);

        DetalleCompra detalle = carrito.getDetalles().stream()
                .filter(d -> !d.isEliminado() && d.getId().equals(detalleId.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el ítem en el carrito con ID: " + detalleId));

        detalle.setCantidad(nuevaCantidad);
        recalcularSubtotal(detalle);
        detalleCompraRepository.save(detalle);

        recalcularTotal(carrito);
        return ordenCompraRepository.save(carrito);
    }

    /**
     * Elimina un ítem del carrito (borrado lógico).
     */
    @Transactional
    public OrdenCompra eliminarProducto(Cliente cliente, String detalleId) {
        if (cliente == null) {
            throw new IllegalArgumentException("El cliente no puede ser nulo");
        }
        if (detalleId == null || detalleId.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del ítem no puede estar vacío");
        }

        OrdenCompra carrito = obtenerOCrearCarrito(cliente);

        DetalleCompra detalle = carrito.getDetalles().stream()
                .filter(d -> !d.isEliminado() && d.getId().equals(detalleId.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el ítem en el carrito con ID: " + detalleId));

        detalle.setEliminado(true);
        detalleCompraRepository.save(detalle);

        recalcularTotal(carrito);
        return ordenCompraRepository.save(carrito);
    }

    /**
     * Vacía todos los ítems del carrito activo.
     */
    @Transactional
    public OrdenCompra vaciarCarrito(Cliente cliente) {
        if (cliente == null) {
            throw new IllegalArgumentException("El cliente no puede ser nulo");
        }

        OrdenCompra carrito = obtenerOCrearCarrito(cliente);
        for (DetalleCompra detalle : carrito.getDetalles()) {
            if (!detalle.isEliminado()) {
                detalle.setEliminado(true);
                detalleCompraRepository.save(detalle);
            }
        }

        recalcularTotal(carrito);
        return ordenCompraRepository.save(carrito);
    }
}
<<<<<<< HEAD
=======

>>>>>>> augusto
