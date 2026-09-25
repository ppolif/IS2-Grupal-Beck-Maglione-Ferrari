package com.example.zero.services;

import com.example.zero.entidades.compra.Detalle;
import com.example.zero.entidades.compra.Factura;
import com.example.zero.entidades.compra.FormaDePago;
import com.example.zero.entidades.persona.Cliente;
import com.example.zero.entidades.persona.Nacionalidad;
import com.example.zero.entidades.producto.Producto;
import com.example.zero.enums.EstadoFactura;
import com.example.zero.enums.TipoDePago;
import com.example.zero.enums.TipoDocumento;
import com.example.zero.repositories.*;
import com.example.zero.services.persona.ClienteService;
import com.example.zero.services.producto.ProductoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Servicio de negocio para la gestión y registro de ventas.
 */
@Service
public class VentaService {

    private final FacturaRepository facturaRepository;
    private final DetalleRepository detalleRepository;
    private final FormaDePagoRepository formaDePagoRepository;
    private final ClienteRepository clienteRepository;
    private final ClienteService clienteService;
    private final NacionalidadRepository nacionalidadRepository;
    private final ProductoService productoService;
    private final UsuarioRepository usuarioRepository;

    public VentaService(FacturaRepository facturaRepository,
                        DetalleRepository detalleRepository,
                        FormaDePagoRepository formaDePagoRepository,
                        ClienteRepository clienteRepository,
                        ClienteService clienteService,
                        NacionalidadRepository nacionalidadRepository,
                        ProductoService productoService,
                        UsuarioRepository usuarioRepository) {
        this.facturaRepository = facturaRepository;
        this.detalleRepository = detalleRepository;
        this.formaDePagoRepository = formaDePagoRepository;
        this.clienteRepository = clienteRepository;
        this.clienteService = clienteService;
        this.nacionalidadRepository = nacionalidadRepository;
        this.productoService = productoService;
        this.usuarioRepository = usuarioRepository;
    }

    public void validarVenta(String clienteDni, String clienteNombre, String clienteApellido,
                             List<String> productoIds, List<Integer> cantidades) {
        if (clienteDni == null || clienteDni.trim().isEmpty()) {
            throw new IllegalArgumentException("El DNI del cliente no puede estar vacío");
        }
        if (clienteNombre == null || clienteNombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del cliente no puede estar vacío");
        }
        if (clienteApellido == null || clienteApellido.trim().isEmpty()) {
            throw new IllegalArgumentException("El apellido del cliente no puede estar vacío");
        }
        if (productoIds == null || productoIds.isEmpty()) {
            throw new IllegalArgumentException("Debe agregar al menos un producto a la venta");
        }
        if (cantidades == null || cantidades.size() != productoIds.size()) {
            throw new IllegalArgumentException("La cantidad de productos e ítems no coincide");
        }
        for (int i = 0; i < cantidades.size(); i++) {
            Integer cant = cantidades.get(i);
            if (cant == null || cant <= 0) {
                throw new IllegalArgumentException("La cantidad de cada producto debe ser mayor a cero");
            }
        }
    }

    @Transactional
    public Factura registrarVenta(String clienteDni, String clienteNombre, String clienteApellido,
                                  String clienteEmail, String formaDePagoStr,
                                  List<String> productoIds, List<Integer> cantidades) {
        validarVenta(clienteDni, clienteNombre, clienteApellido, productoIds, cantidades);

        String dniLimpio = clienteDni.trim();

        // 1. Obtener o crear Cliente
        Cliente cliente = clienteRepository.findByNumeroDocumentoAndEliminadoFalse(dniLimpio)
                .orElseGet(() -> {
                    Nacionalidad nac = nacionalidadRepository.findByNombreAndEliminadoFalse("Argentina")
                            .orElseGet(() -> nacionalidadRepository.save(
                                    Nacionalidad.builder().nombre("Argentina").eliminado(false).build()
                            ));
                    return clienteService.crearCliente(
                            dniLimpio,
                            clienteNombre.trim(),
                            clienteApellido.trim(),
                            LocalDate.of(2000, 1, 1),
                            TipoDocumento.DNI,
                            nac
                    );
                });

        // Actualizar nombre o apellido si vino modificado
        if (clienteNombre != null && !clienteNombre.trim().isEmpty()) {
            cliente.setNombre(clienteNombre.trim());
        }
        if (clienteApellido != null && !clienteApellido.trim().isEmpty()) {
            cliente.setApellido(clienteApellido.trim());
        }
        // Asociar usuario al cliente si aún no está vinculado
        if (cliente.getUsuario() == null && usuarioRepository != null) {
            if (clienteEmail != null && !clienteEmail.trim().isEmpty()) {
                usuarioRepository.findByNombreUsuarioAndEliminadoFalse(clienteEmail.trim().toLowerCase())
                        .ifPresent(u -> {
                            cliente.setUsuario(u);
                            u.setPersona(cliente);
                            usuarioRepository.save(u);
                        });
            }
            if (cliente.getUsuario() == null) {
                usuarioRepository.findByPersonaDocumentoAndEliminadoFalse(dniLimpio)
                        .ifPresent(cliente::setUsuario);
            }
        }
        clienteRepository.save(cliente);

        // 2. Resolver Forma de Pago
        TipoDePago tipoPago;
        try {
            tipoPago = (formaDePagoStr != null && !formaDePagoStr.trim().isEmpty())
                    ? TipoDePago.valueOf(formaDePagoStr.trim().toUpperCase())
                    : TipoDePago.EFECTIVO;
        } catch (IllegalArgumentException e) {
            tipoPago = TipoDePago.EFECTIVO;
        }

        final TipoDePago finalTipoPago = tipoPago;
        FormaDePago formaDePago = formaDePagoRepository.findByTipoPagoAndEliminadoFalse(finalTipoPago)
                .orElseGet(() -> formaDePagoRepository.save(
                        FormaDePago.builder()
                                .tipoPago(finalTipoPago)
                                .observacion("Registrado automáticamente en venta")
                                .eliminado(false)
                                .build()
                ));

        // 3. Generar número correlativo de factura
        Long numeroFactura = facturaRepository.findTopByOrderByNumeroFacturaDesc()
                .map(f -> f.getNumeroFactura() + 1)
                .orElse(1001L);

        // 4. Instanciar Factura
        Factura factura = Factura.builder()
                .numeroFactura(numeroFactura)
                .fechaFactura(LocalDateTime.now())
                .estado(EstadoFactura.PAGADA)
                .cliente(cliente)
                .formaDePago(formaDePago)
                .totalPagado(0.0)
                .eliminado(false)
                .detalles(new HashSet<>())
                .build();

        // 5. Procesar cada detalle de producto
        double total = 0.0;
        for (int i = 0; i < productoIds.size(); i++) {
            String prodId = productoIds.get(i);
            int cantidad = cantidades.get(i);

            Producto producto = productoService.buscarPorId(prodId);
            double precioUnitario = 0.0;
            try {
                precioUnitario = productoService.obtenerPrecioActual(prodId);
            } catch (Exception ignored) {
            }

            double subtotal = Math.round(precioUnitario * cantidad * 100.0) / 100.0;
            total += subtotal;

            Detalle detalle = Detalle.builder()
                    .factura(factura)
                    .producto(producto)
                    .cantidad(cantidad)
                    .subtotal(subtotal)
                    .eliminado(false)
                    .build();

            factura.getDetalles().add(detalle);
        }

        factura.setTotalPagado(Math.round(total * 100.0) / 100.0);

        return facturaRepository.save(factura);
    }

    @Transactional(readOnly = true)
    public List<Factura> listarVentas() {
        List<Factura> facturas = facturaRepository.findByEliminadoFalseOrderByFechaFacturaDesc();
        for (Factura f : facturas) {
            enriquecerFactura(f);
        }
        return facturas;
    }

    @Transactional(readOnly = true)
    public Factura buscarPorId(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID de la factura no puede ser nulo o vacío");
        }
        Factura f = facturaRepository.findActive(id)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la factura activa con ID: " + id));
        enriquecerFactura(f);
        return f;
    }

    @Transactional(readOnly = true)
    public Factura buscarPorNumeroFactura(Long numeroFactura) {
        if (numeroFactura == null) {
            throw new IllegalArgumentException("El número de factura no puede ser nulo");
        }
        Factura f = facturaRepository.findByNumeroFacturaAndEliminadoFalse(numeroFactura)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la factura número: " + numeroFactura));
        enriquecerFactura(f);
        return f;
    }

    @Transactional
    public void eliminarVenta(String id) {
        Factura factura = buscarPorId(id);
        factura.setEliminado(true);
        if (factura.getDetalles() != null) {
            for (Detalle d : factura.getDetalles()) {
                d.setEliminado(true);
            }
        }
        facturaRepository.save(factura);
    }

    @Transactional(readOnly = true)
    public Factura buscarFacturaPorIdentificador(String orderNumberOrId) {
        if (orderNumberOrId == null || orderNumberOrId.trim().isEmpty()) {
            return null;
        }
        String clean = orderNumberOrId.trim().replace("#ORD-", "").replace("ORD-", "").replace("#", "").trim();
        try {
            Long num = Long.parseLong(clean);
            Optional<Factura> facturaOpt = facturaRepository.findByNumeroFacturaAndEliminadoFalse(num);
            if (facturaOpt.isPresent()) {
                Factura f = facturaOpt.get();
                enriquecerFactura(f);
                return f;
            }
        } catch (NumberFormatException ignored) {
        }

        // Búsqueda alternativa por ID primario
        Optional<Factura> facturaById = facturaRepository.findActive(clean);
        if (facturaById.isEmpty() && !clean.equals(orderNumberOrId.trim())) {
            facturaById = facturaRepository.findActive(orderNumberOrId.trim());
        }
        Factura f = facturaById.orElse(null);
        enriquecerFactura(f);
        return f;
    }

    public void enriquecerFactura(Factura f) {
        if (f == null || f.getCliente() == null) return;
        Cliente c = f.getCliente();
        if (c.getUsuario() == null && usuarioRepository != null) {
            if (c.getNumeroDocumento() != null) {
                usuarioRepository.findByPersonaDocumentoAndEliminadoFalse(c.getNumeroDocumento())
                        .ifPresent(c::setUsuario);
            }
        }
        if (c.getUsuario() == null && c.getContactos() != null && usuarioRepository != null) {
            for (var cont : c.getContactos()) {
                if (cont instanceof com.example.zero.entidades.empresa.ContactoCorreoElectronico ce && ce.getEmail() != null) {
                    usuarioRepository.findByNombreUsuarioAndEliminadoFalse(ce.getEmail().trim().toLowerCase())
                            .ifPresent(c::setUsuario);
                    if (c.getUsuario() != null) break;
                }
            }
        }
    }
}
