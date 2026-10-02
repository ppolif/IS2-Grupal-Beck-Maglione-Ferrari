package com.example.zero.services;

import com.example.zero.entidades.compraCliente.DetalleCompra;
import com.example.zero.entidades.compraCliente.OrdenCompra;
import com.example.zero.entidades.persona.Cliente;
import com.example.zero.entidades.persona.Nacionalidad;
import com.example.zero.entidades.persona.Usuario;
import com.example.zero.entidades.producto.Producto;
import com.example.zero.enums.EstadoOrdenCompra;
import com.example.zero.enums.RolUsuario;
import com.example.zero.enums.TipoDocumento;
import com.example.zero.repositories.*;
import com.example.zero.services.producto.ProductoService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;


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
    private final StockService stockService;
    private final HttpSession session;


    public void validar(String id, String email, Cliente cliente, EstadoOrdenCompra estadoOrdenCompra) {
        if (cliente == null) {
            throw new IllegalArgumentException("La orden de compra debe tener un cliente asociado.");
        }
        if (estadoOrdenCompra == null) {
            throw new IllegalArgumentException("El estado de la orden de compra no puede ser nulo.");
        }
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("El id de la orden de compra no puede ser nulo.");
        }
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("El email del usuario no puede ser nulo.");
        }
    }


    @Transactional
    public OrdenCompra crearOrdenCompra(String identificadorCompra,
                                        Date fecha,
                                        Cliente cliente,
                                        String emailUsuario,
                                        EstadoOrdenCompra estadoOrdenCompra) {
        validar(identificadorCompra, emailUsuario, cliente, estadoOrdenCompra);

        Date fechaOrden = (fecha != null) ? fecha : new Date();

        OrdenCompra orden = OrdenCompra.builder()
                .identificadorCompra(identificadorCompra)
                .fecha(fechaOrden)
                .total(0.0)
                .estadoOrdenCompra(estadoOrdenCompra)
                .cliente(cliente)
                .emailUsuario(emailUsuario)
                .eliminado(false)
                .detalles(new ArrayList<>())
                .build();

        return ordenCompraRepository.save(orden);
    }

    ///recalcula y asigna el total acumulado de una orden de compra sumando los subtotales
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

    ///
    public void recalcularSubtotal(DetalleCompra detalle) {
        if (detalle == null) return;
        detalle.setSubtotal(Math.round((detalle.getCantidad() * detalle.getPrecioUnitario()) * 100.0) / 100.0);
    }


    public List<DetalleCompra> obtenerItemsActivos(OrdenCompra ordenCompra) {
        if (ordenCompra == null || ordenCompra.getDetalles() == null) {
            return Collections.emptyList();
        }
        return ordenCompra.getDetalles().stream()
                .filter(d -> !d.isEliminado())
                .toList();
    }

    ///cuenta la cantidad total de unidades contenidas en la orden de compra
    public int contarItems(OrdenCompra ordenCompra) {
        if (ordenCompra == null || ordenCompra.getDetalles() == null) {
            return 0;
        }
        return ordenCompra.getDetalles().stream()
                .filter(d -> !d.isEliminado())
                .mapToInt(DetalleCompra::getCantidad)
                .sum();
    }



    @Transactional
    public Cliente obtenerOAsociarCliente(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("Usuario no autenticado");
        }
        if (usuario.getRol() != RolUsuario.CLIENTE) {
            throw new IllegalArgumentException("Solo los usuarios con rol CLIENTE pueden poseer o gestionar un carrito de compras.");
        }

        Usuario uPersistente = usuarioRepository.findById(usuario.getId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        if (uPersistente.getPersona() instanceof Cliente cliente) {
            return cliente;
        }
        throw new IllegalStateException("El usuario actual no tiene un perfil de cliente asociado para operar con el carrito.");
    }

    ///obtiene el carrito activo o crear
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

        ///ahora hay que asociar mail asi que lo sacamos de la sesion
        Usuario usuarioSession = (session != null) ? (Usuario) session.getAttribute("usuariosession") : null;
        String emailUser = (usuarioSession != null) ? usuarioSession.getNombreUsuario() : null;


        String cartId = "CART-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return crearOrdenCompra(cartId, new Date(), cliente, emailUser, EstadoOrdenCompra.PENDIENTE_COMPLETAR);
    }


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
            precioUnitario = 100.0;
        }

        OrdenCompra carrito = obtenerOCrearCarrito(cliente);

        int cantidadActualEnCarrito = 0;
        Optional<DetalleCompra> detalleExistente = carrito.getDetalles().stream()
                .filter(d -> !d.isEliminado() && d.getProducto() != null && d.getProducto().getId().equals(producto.getId()))
                .findFirst();

        if (detalleExistente.isPresent()) {
            cantidadActualEnCarrito = detalleExistente.get().getCantidad();
        }

        int cantidadTotalDeseada = cantidadActualEnCarrito + cantidad;
        if (stockService != null) {
            int stockDisponible = stockService.calcularStockActual(producto.getId());
            if (stockDisponible < cantidadTotalDeseada) {
                throw new IllegalArgumentException("Stock insuficiente para: " + producto.getNombre() + " (Stock disponible: " + stockDisponible + ")");
            }
        }

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

        if (stockService != null && detalle.getProducto() != null) {
            int stockDisponible = stockService.calcularStockActual(detalle.getProducto().getId());
            if (stockDisponible < nuevaCantidad) {
                throw new IllegalArgumentException("Stock insuficiente para: " + detalle.getProducto().getNombre() + " (Stock disponible: " + stockDisponible + ")");
            }
        }

        detalle.setCantidad(nuevaCantidad);
        recalcularSubtotal(detalle);
        detalleCompraRepository.save(detalle);

        recalcularTotal(carrito);
        return ordenCompraRepository.save(carrito);
    }


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