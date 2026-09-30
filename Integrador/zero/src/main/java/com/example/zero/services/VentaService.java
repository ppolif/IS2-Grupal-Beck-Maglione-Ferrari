package com.example.zero.services;


import com.example.zero.entidades.Imagen;
import com.example.zero.entidades.compra.Detalle;
import com.example.zero.entidades.compra.Factura;
import com.example.zero.entidades.compra.FormaDePago;
import com.example.zero.entidades.empresa.ContactoCorreoElectronico;
import com.example.zero.entidades.empresa.ContactoTelefonico;
import com.example.zero.entidades.persona.Cliente;
import com.example.zero.entidades.persona.Nacionalidad;
import com.example.zero.entidades.persona.Usuario;
import com.example.zero.entidades.producto.Producto;
import com.example.zero.enums.EstadoFactura;
import com.example.zero.enums.TipoDePago;
import com.example.zero.enums.TipoDocumento;
import com.example.zero.repositories.*;
import com.example.zero.services.persona.ClienteService;
import com.example.zero.services.producto.ProductoService;
import com.example.zero.services.mail.EmailService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.zero.entidades.compraCliente.FacturaCliente;
import com.example.zero.entidades.compraCliente.OrdenCompra;
import com.example.zero.entidades.compraProveedor.FacturaProveedor;
import com.example.zero.enums.EstadoOrdenCompra;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Servicio de negocio para la gestión y registro de ventas.
 */
@Service
public class VentaService {

    private static final Logger logger = LoggerFactory.getLogger(VentaService.class);

    private final FacturaRepository facturaRepository;
    private final DetalleRepository detalleRepository;
    private final FormaDePagoRepository formaDePagoRepository;
    private final ClienteRepository clienteRepository;
    private final ClienteService clienteService;
    private final NacionalidadRepository nacionalidadRepository;
    private final ProductoService productoService;
    private final ProductoRepository productoRepository;
    private final StockService stockService;
    private final UsuarioRepository usuarioRepository;
    private final EmailService emailService;
    private final OrdenCompraRepository ordenCompraRepository;
    private final ContactoService contactoService;
    private final HttpSession session;

    public VentaService(FacturaRepository facturaRepository,
                        DetalleRepository detalleRepository,
                        FormaDePagoRepository formaDePagoRepository,
                        ClienteRepository clienteRepository,
                        ClienteService clienteService,
                        NacionalidadRepository nacionalidadRepository,
                        ProductoService productoService,
                        UsuarioRepository usuarioRepository,
                        HttpSession session) {
        this(facturaRepository, detalleRepository, formaDePagoRepository, clienteRepository, clienteService,
                nacionalidadRepository, productoService, null, null, usuarioRepository, null, null, null, session);
    }

    public VentaService(FacturaRepository facturaRepository,
                        DetalleRepository detalleRepository,
                        FormaDePagoRepository formaDePagoRepository,
                        ClienteRepository clienteRepository,
                        ClienteService clienteService,
                        NacionalidadRepository nacionalidadRepository,
                        ProductoService productoService,
                        ProductoRepository productoRepository,
                        StockService stockService,
                        UsuarioRepository usuarioRepository,
                        HttpSession session) {
        this(facturaRepository, detalleRepository, formaDePagoRepository, clienteRepository, clienteService,
                nacionalidadRepository, productoService, productoRepository, stockService, usuarioRepository, null, null, null, session);
    }

    @org.springframework.beans.factory.annotation.Autowired
    public VentaService(FacturaRepository facturaRepository,
                        DetalleRepository detalleRepository,
                        FormaDePagoRepository formaDePagoRepository,
                        ClienteRepository clienteRepository,
                        ClienteService clienteService,
                        NacionalidadRepository nacionalidadRepository,
                        ProductoService productoService,
                        ProductoRepository productoRepository,
                        StockService stockService,
                        UsuarioRepository usuarioRepository,
                        @org.springframework.beans.factory.annotation.Autowired(required = false) EmailService emailService,
                        @org.springframework.beans.factory.annotation.Autowired(required = false) OrdenCompraRepository ordenCompraRepository,
                        @org.springframework.beans.factory.annotation.Autowired(required = false) ContactoService contactoService,
                        HttpSession session) {
        this.facturaRepository = facturaRepository;
        this.detalleRepository = detalleRepository;
        this.formaDePagoRepository = formaDePagoRepository;
        this.clienteRepository = clienteRepository;
        this.clienteService = clienteService;
        this.nacionalidadRepository = nacionalidadRepository;
        this.productoService = productoService;
        this.productoRepository = productoRepository;
        this.stockService = stockService;
        this.usuarioRepository = usuarioRepository;
        this.emailService = emailService;
        this.ordenCompraRepository = ordenCompraRepository;
        this.contactoService = contactoService;
        this.session = session;
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

        // Fase 1: Verificación de Stock Actual previo a la venta
        if (stockService != null) {
            Map<String, Integer> cantidadesPorProducto = new LinkedHashMap<>();
            for (int i = 0; i < productoIds.size(); i++) {
                String pId = productoIds.get(i);
                int c = cantidades.get(i);
                cantidadesPorProducto.put(pId, cantidadesPorProducto.getOrDefault(pId, 0) + c);
            }

            for (Map.Entry<String, Integer> entry : cantidadesPorProducto.entrySet()) {
                String prodId = entry.getKey();
                int cantidadSolicitada = entry.getValue();
                Producto producto = productoService.buscarPorId(prodId);
                int stockActual = stockService.calcularStockActual(prodId);

                if (stockActual < cantidadSolicitada) {
                    throw new IllegalArgumentException("Stock insuficiente para: " + producto.getNombre());
                }
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

        // Asociar la Persona al Usuario actual (Usuario -> Persona)
        Usuario usuarioSession = (session != null) ? (Usuario) session.getAttribute("usuariosession") : null;
        if (usuarioSession != null && usuarioRepository != null) {
            usuarioSession.setPersona(cliente);
            usuarioRepository.save(usuarioSession);
        } else if (clienteEmail != null && !clienteEmail.trim().isEmpty() && usuarioRepository != null) {
            usuarioRepository.findByNombreUsuarioAndEliminadoFalse(clienteEmail.trim().toLowerCase())
                    .ifPresent(u -> {
                        u.setPersona(cliente);
                        usuarioRepository.save(u);
                    });
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

        // 4. Instanciar OrdenCompra con el email del comprador
        OrdenCompra orden = OrdenCompra.builder()
                .identificadorCompra("ORD-" + numeroFactura)
                .fecha(new Date())
                .cliente(cliente)
                .emailUsuario(clienteEmail != null && !clienteEmail.isBlank() ? clienteEmail.trim().toLowerCase() : null)
                .total(0.0)
                .estadoOrdenCompra(EstadoOrdenCompra.PENDIENTE_ENVIO)
                .eliminado(false)
                .build();
        if (ordenCompraRepository != null) {
            orden = ordenCompraRepository.save(orden);
        }

        FacturaCliente factura = FacturaCliente.builder()
                .numeroFactura(numeroFactura)
                .fechaFactura(LocalDateTime.now())
                .estado(EstadoFactura.PAGADA)
                .formaDePago(formaDePago)
                .ordenCompra(orden)
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
        if (orden != null) {
            orden.setTotal(factura.getTotalPagado());
            if (ordenCompraRepository != null) {
                ordenCompraRepository.save(orden);
            }
        }

        Factura facturaGuardada = facturaRepository.save(factura);

        // Fase 2: Descuento de stock y creación de registro trazable en Stock
        if (stockService != null && facturaGuardada.getDetalles() != null) {
            for (Detalle detalle : facturaGuardada.getDetalles()) {
                Producto prod = detalle.getProducto();
                if (prod != null) {
                    int stockActual = stockService.calcularStockActual(prod.getId());
                    int nuevoBalance = stockService.disminuirStock(stockActual, detalle.getCantidad());
                    stockService.crearStock(detalle, nuevoBalance, "Egreso por Venta - Factura N° " + facturaGuardada.getNumeroFactura());
                }
            }
        }

        // Fase 3: Despacho de comprobante y detalle de compra por correo electrónico
        if (emailService != null) {
            String emailDestino = (clienteEmail != null && !clienteEmail.trim().isEmpty() && clienteEmail.contains("@"))
                    ? clienteEmail.trim()
                    : (contactoService != null ? contactoService.obtenerEmailPrincipal(cliente).orElse(null) : null);

            if (emailDestino == null && session != null) {
                //usuarioSession = (Usuario) session.getAttribute("usuariosession");
                if (usuarioSession != null && usuarioSession.getNombreUsuario() != null && usuarioSession.getNombreUsuario().contains("@")) {
                    emailDestino = usuarioSession.getNombreUsuario().trim();
                }
            }

            String orderNumStr = facturaGuardada.getNumeroFactura() != null ? "#ORD-" + facturaGuardada.getNumeroFactura() : "#ORD-" + facturaGuardada.getId();

            if (emailDestino != null && !emailDestino.trim().isEmpty() && emailDestino.contains("@")) {
                try {
                    emailService.enviarComprobanteCompra(facturaGuardada, emailDestino.trim());
                } catch (Exception e) {
                    logger.error("No se pudo enviar el correo de confirmación de compra para factura {}: {}",
                            orderNumStr, e.getMessage());
                }
            } else {
                logger.info("No se encontró una dirección de correo válida para notificar la compra de la factura {}",
                        orderNumStr);
            }
        }

        return facturaGuardada;
    }

    @Transactional(readOnly = true)
    public List<Factura> listarVentas() {
        List<Factura> facturas = facturaRepository.findByEliminadoFalseOrderByFechaFacturaDesc();
        for (Factura f : facturas) {
            //enriquecerFactura(f);
        }
        return facturas;
    }

    @Transactional(readOnly = true)
    public List<Factura> listarComprasCliente(Cliente cliente) {
        if (cliente == null) {
            return Collections.emptyList();
        }
        List<Factura> compras = facturaRepository.findByClienteOrderByFechaFacturaDesc(cliente);
        for (Factura f : compras) {
            //enriquecerFactura(f);
        }
        return compras;
    }

    @Transactional(readOnly = true)
    public Factura buscarPorId(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID de la factura no puede ser nulo o vacío");
        }
        Factura f = facturaRepository.findActive(id)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la factura activa con ID: " + id));
        //enriquecerFactura(f);
        return f;
    }

    @Transactional(readOnly = true)
    public Factura buscarPorNumeroFactura(Long numeroFactura) {
        if (numeroFactura == null) {
            throw new IllegalArgumentException("El número de factura no puede ser nulo");
        }
        Factura f = facturaRepository.findByNumeroFacturaAndEliminadoFalse(numeroFactura)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la factura número: " + numeroFactura));
        //enriquecerFactura(f);
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
        String clean = orderNumberOrId.trim()
                .replace("#ORD-", "")
                .replace("ORD-", "")
                .replace("#FAC-", "")
                .replace("FAC-", "")
                .replace("#", "")
                .trim();
        try {
            Long num = Long.parseLong(clean);
            Optional<Factura> facturaOpt = facturaRepository.findByNumeroFacturaAndEliminadoFalse(num);
            if (facturaOpt.isPresent()) {
                Factura f = facturaOpt.get();
                //enriquecerFactura(f);
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
        //enriquecerFactura(f);
        return f;
    }

//    public void enriquecerFactura(Factura f) {
//        if (f == null) return;
//
//        Cliente cliente = obtenerClienteDeFactura(f);
//        if (cliente == null) return;
//
//        // 2. Verificar si el cliente ya tiene un email o teléfono cargado en sus contactos
//        boolean tieneEmail = cliente.getContactos().stream()
//                .anyMatch(c -> c instanceof ContactoCorreoElectronico ce && ce.getEmail() != null && !ce.getEmail().isBlank());
//
//        boolean tieneTelefono = cliente.getContactos().stream()
//                .anyMatch(c -> c instanceof ContactoTelefonico ct && ct.getTelefono() != null && !ct.getTelefono().isBlank());
//
//        // 3. Si no tiene email en sus contactos, pero hay un usuario en sesión, enriquecemos con el email de la sesión
//        if (!tieneEmail && session != null) {
//            Usuario usuarioSession = (Usuario) session.getAttribute("usuariosession");
//            if (usuarioSession != null && usuarioSession.getNombreUsuario() != null && usuarioSession.getNombreUsuario().contains("@")) {
//                ContactoCorreoElectronico contactoEmail = ContactoCorreoElectronico.builder()
//                        .email(usuarioSession.getNombreUsuario().trim())
//                        .observacion("Asociado desde sesión activa")
//                        .eliminado(false)
//                        .build();
//                cliente.getContactos().add(contactoEmail);
//            }
//        }
//    }

    public Cliente obtenerClienteDeFactura(Factura f) {
        if (f instanceof FacturaCliente fc && fc.getOrdenCompra() != null) {
            return fc.getOrdenCompra().getCliente();
        }
        return null;
    }

    public String obtenerFotoComprobante(Factura f) {
        if (f == null) {
            return "/admin/assets/images/avatar.png";
        }
        try {
            // 1. Si es FacturaCliente, intentar primero resolver foto por el emailUsuario de la OrdenCompra
            if (f instanceof FacturaCliente fc && fc.getOrdenCompra() != null) {
                String email = fc.getOrdenCompra().getEmailUsuario();
                if (email != null && !email.isBlank() && usuarioRepository != null) {
                    Optional<Usuario> uOpt = usuarioRepository.findByNombreUsuarioAndEliminadoFalse(email.trim().toLowerCase());
                    if (uOpt.isPresent() && uOpt.get().getFoto() != null && !uOpt.get().getFoto().isBlank()) {
                        return uOpt.get().getFoto().trim();
                    }
                }
            }

            // 2. Si no, buscar por el Cliente de la factura
            Cliente c = obtenerClienteDeFactura(f);
            if (c != null) {
                if (clienteService != null) {
                    String foto = clienteService.obtenerFotoPerfilCliente(c);
                    if (foto != null && !foto.isBlank() && !foto.equals("/admin/assets/images/avatar.png")) {
                        return foto;
                    }
                }
                if (c.getNumeroDocumento() != null && usuarioRepository != null) {
                    List<Usuario> usuarios = usuarioRepository.findByPersonaDocumentoAndEliminadoFalse(c.getNumeroDocumento().trim());
                    if (usuarios != null) {
                        for (Usuario u : usuarios) {
                            if (u != null && u.getFoto() != null && !u.getFoto().isBlank()) {
                                return u.getFoto().trim();
                            }
                        }
                    }
                }
                if (c.getImagen() != null && !c.getImagen().isEmpty()) {
                    for (com.example.zero.entidades.Imagen img : c.getImagen()) {
                        if (img != null && !img.isEliminado() && img.getId() != null) {
                            return "/imagen/" + img.getId();
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Error resolviendo foto de comprobante para factura {}: {}", f.getId(), e.getMessage());
        }
        return "/admin/assets/images/avatar.png";
    }

    public String obtenerNombreComprobante(Factura f) {
        if (f instanceof FacturaProveedor fp && fp.getProveedor() != null) {
            String razon = fp.getProveedor().getRazonSocial();
            return (razon != null && !razon.isBlank()) ? razon.trim() : "Proveedor";
        }
        if (f instanceof FacturaCliente fc && fc.getOrdenCompra() != null && fc.getOrdenCompra().getCliente() != null) {
            Cliente c = fc.getOrdenCompra().getCliente();
            String nom = c.getNombre() != null ? c.getNombre().trim() : "";
            String ape = c.getApellido() != null ? c.getApellido().trim() : "";
            String completo = (nom + " " + ape).trim();
            return !completo.isEmpty() ? completo : "Cliente Final";
        }
        return "Cliente Final";
    }

    public String obtenerEmailComprobante(Factura f) {
        if (f instanceof FacturaProveedor fp && fp.getProveedor() != null) {
            return "CUIT: " + fp.getProveedor().getCuit();
        }
        if (f instanceof FacturaCliente fc && fc.getOrdenCompra() != null) {
            String email = fc.getOrdenCompra().getEmailUsuario();
            return (email != null && !email.isBlank()) ? email.trim() : "N/A";
        }
        return "N/A";
    }

    public String obtenerResumenProductos(Factura f) {
        if (f == null || f.getDetalles() == null || f.getDetalles().isEmpty()) {
            return "Venta General";
        }
        StringBuilder sb = new StringBuilder();
        for (Detalle d : f.getDetalles()) {
            if (!d.isEliminado() && d.getProducto() != null) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(d.getProducto().getNombre()).append(" (x").append(d.getCantidad()).append(")");
            }
        }
        return sb.length() > 0 ? sb.toString() : "Venta General";
    }

    public boolean esCompraProveedor(Factura f) {
        return f instanceof FacturaProveedor;
    }
}
