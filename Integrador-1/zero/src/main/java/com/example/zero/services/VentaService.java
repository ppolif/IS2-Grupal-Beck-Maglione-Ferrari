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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.zero.entidades.compraCliente.FacturaCliente;
import com.example.zero.entidades.compraCliente.OrdenCompra;
import com.example.zero.entidades.compraProveedor.FacturaProveedor;
import com.example.zero.enums.EstadoOrdenCompra;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;


@Service
public class VentaService {

    private static final Logger logger = LoggerFactory.getLogger(VentaService.class); //clase para visualizar los errores mas facil por consola

    private final FacturaRepository facturaRepository;
    private final FormaDePagoRepository formaDePagoRepository;
    private final ClienteRepository clienteRepository;
    private final ClienteService clienteService;
    private final ProductoService productoService;
    private final StockService stockService;
    private final UsuarioRepository usuarioRepository;
    private final EmailService emailService;
    private final OrdenCompraRepository ordenCompraRepository;
    private final OrdenCompraService ordenCompraService;

    @Autowired
    public VentaService(FacturaRepository facturaRepository,
                        FormaDePagoRepository formaDePagoRepository,
                        ClienteRepository clienteRepository,
                        ClienteService clienteService,
                        ProductoService productoService,
                        StockService stockService,
                        UsuarioRepository usuarioRepository,
                        EmailService emailService,
                        OrdenCompraRepository ordenCompraRepository,
                        OrdenCompraService ordenCompraService) {
        this.facturaRepository = facturaRepository;
        this.formaDePagoRepository = formaDePagoRepository;
        this.clienteRepository = clienteRepository;
        this.clienteService = clienteService;
        this.productoService = productoService;
        this.stockService = stockService;
        this.usuarioRepository = usuarioRepository;
        this.emailService = emailService;
        this.ordenCompraRepository = ordenCompraRepository;
        this.ordenCompraService = ordenCompraService;
    }

    public void validarVenta(String clienteDni, String clienteNombre, String clienteApellido, String clienteEmail,
                             List<String> productoIds, String formaDePagoStr, List<Integer> cantidades) {
        if (clienteDni == null || clienteDni.trim().isEmpty()) {
            throw new IllegalArgumentException("El DNI del cliente no puede estar vacío");
        }
        if (clienteNombre == null || clienteNombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del cliente no puede estar vacío");
        }
        if (clienteApellido == null || clienteApellido.trim().isEmpty()) {
            throw new IllegalArgumentException("El apellido del cliente no puede estar vacío");
        }
        if (clienteEmail == null || clienteEmail.isEmpty()) {
            throw new IllegalArgumentException("El correo del cliente no puede estar vacío");
        }
        if (productoIds == null || productoIds.isEmpty()) {
            throw new IllegalArgumentException("Debe agregar al menos un producto a la venta");
        }
        if (formaDePagoStr == null || formaDePagoStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Debe especificar una forma de pago.");
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


        if (stockService != null) {
            Map<String, Integer> cantidadesPorProducto = new LinkedHashMap<>(); //mapea la cantidad de cada producto con su id para evitar peticiones maliciosas y falsos chequeos de stock
            for (int i = 0; i < productoIds.size(); i++) {
                String pId = productoIds.get(i);
                int c = cantidades.get(i);
                cantidadesPorProducto.put(pId, cantidadesPorProducto.getOrDefault(pId, 0) + c);
            }

            for (Map.Entry<String, Integer> entry : cantidadesPorProducto.entrySet()) {
                String prodId = entry.getKey(); //agarramos el id del producto
                int cantidadSolicitada = entry.getValue();//agarramos la cantidad total calculada en el primer for
                Producto producto = productoService.buscarPorId(prodId); //buscamos el producto
                int stockActual = stockService.calcularStockActual(prodId);//buscamos el sotck

                if (stockActual < cantidadSolicitada) {
                    throw new IllegalArgumentException("Stock insuficiente para: " + producto.getNombre()); //si no hay stock lanza error
                }
            }
        }
    }

    @Transactional
    public Factura registrarVenta(String clienteDni, String clienteNombre, String clienteApellido,
                                  String clienteEmail, String formaDePagoStr,
                                  List<String> productoIds, List<Integer> cantidades) {
        validarVenta(clienteDni, clienteNombre, clienteApellido, clienteEmail, productoIds, formaDePagoStr, cantidades);

        String dniLimpio = clienteDni.trim();

        // obtener o crear cliente
        Cliente cliente = clienteRepository.findByNumeroDocumentoAndEliminadoFalse(dniLimpio)
                .orElseGet(() -> {
                    //se esta registrando una venta de un cliente que compra por primera vez

                    return clienteService.crearCliente(
                            dniLimpio,
                            clienteNombre,
                            clienteApellido,
                            null,
                            TipoDocumento.DNI,
                            null
                    );
                });

        // asociar el cliente con un usuario, si es que existe
        Optional<Usuario> usuarioCliente = usuarioRepository.findByNombreUsuarioAndEliminadoFalse(clienteEmail);
        if (usuarioCliente.isPresent()) {
            clienteService.asociarClienteUsuario(cliente.getNumeroDocumento(), usuarioCliente.get());
        }
        
        clienteRepository.save(cliente);


        TipoDePago tipoPago;
        try {
            //verificacion del tipo de pago, por ejemplo si alguien cambia los parametros de la url esto chequea
            tipoPago = TipoDePago.valueOf(formaDePagoStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("La forma de pago ingresada ('" + formaDePagoStr + "') no es válida.");
        }

        FormaDePago formaDePago = formaDePagoRepository.findByTipoPagoAndEliminadoFalse(tipoPago)
                .orElseThrow(() -> new IllegalArgumentException(
                        "El método de pago '" + tipoPago + "'no está habilitado en la base de datos."
                ));

        // genera numero de factura
        Long numeroFactura = facturaRepository.findTopByOrderByNumeroFacturaDesc()
                .map(f -> f.getNumeroFactura() + 1)
                .orElse(1001L);

        // instanciar OrdenCompra con el email del comprador
        String ordenId = "ORD-" + numeroFactura;
        OrdenCompra orden = ordenCompraService.crearOrdenCompra(
                ordenId,
                new Date(),
                cliente,
                clienteEmail,
                EstadoOrdenCompra.PENDIENTE_ENVIO
        );

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

        // detalles
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

            //redondeo a dos decimales
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
            orden.setTotal(factura.getTotalPagado()); //aca se le asigna el valor total de la compra
            if (ordenCompraRepository != null) {
                ordenCompraRepository.save(orden);
            }
        }

        Factura facturaGuardada = facturaRepository.save(factura);

        // cambio en el stock!!
        if (facturaGuardada.getDetalles() != null) {
            for (Detalle detalle : facturaGuardada.getDetalles()) {
                Producto prod = detalle.getProducto();
                if (prod != null) {
                    int stockActual = stockService.calcularStockActual(prod.getId());
                    int nuevoBalance = stockService.disminuirStock(stockActual, detalle.getCantidad());
                    stockService.crearStock(detalle, nuevoBalance, "Egreso por Venta - Factura N° " + facturaGuardada.getNumeroFactura());
                }
            }
        }

        //envio de comprobante
        String orderNumStr = facturaGuardada.getNumeroFactura() != null ? "#ORD-" + facturaGuardada.getNumeroFactura() : "#ORD-" + facturaGuardada.getId();

        try {
            emailService.enviarComprobanteCompra(facturaGuardada, clienteEmail);
        } catch (Exception e) {
            logger.error("No se pudo enviar el correo de confirmación de compra para factura {}: {}",
                    orderNumStr, e.getMessage());
        }

        return facturaGuardada;
    }

    @Transactional(readOnly = true)
    public List<Factura> listarVentas() {
        List<Factura> facturas = facturaRepository.findByEliminadoFalseOrderByFechaFacturaDesc();

        return facturas;
    }

    @Transactional(readOnly = true)
    public List<Factura> listarComprasCliente(Cliente cliente) {
        if (cliente == null) {
            return Collections.emptyList();
        }
        List<Factura> compras = facturaRepository.findByClienteOrderByFechaFacturaDesc(cliente);
        return compras;
    }

    @Transactional(readOnly = true)
    public Factura buscarPorId(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID de la factura no puede ser nulo o vacío");
        }
        Factura f = facturaRepository.findActive(id)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la factura activa con ID: " + id));
        return f;
    }

    @Transactional(readOnly = true)
    public Factura buscarPorNumeroFactura(Long numeroFactura) {
        if (numeroFactura == null) {
            throw new IllegalArgumentException("El número de factura no puede ser nulo");
        }
        Factura f = facturaRepository.findByNumeroFacturaAndEliminadoFalse(numeroFactura)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la factura número: " + numeroFactura));
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
        return f;
    }

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
            // si es factura cliente intentar primero resolver foto por el emailUsuario de la OrdenCompra
            if (f instanceof FacturaCliente fc && fc.getOrdenCompra() != null) {
                String email = fc.getOrdenCompra().getEmailUsuario();
                if (email != null && !email.isBlank() && usuarioRepository != null) {
                    Optional<Usuario> uOpt = usuarioRepository.findByNombreUsuarioAndEliminadoFalse(email.trim().toLowerCase());
                    if (uOpt.isPresent() && uOpt.get().getFoto() != null && !uOpt.get().getFoto().isBlank()) {
                        return uOpt.get().getFoto().trim();
                    }
                }
            }

            // sino buscar por el Cliente de la factura
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
