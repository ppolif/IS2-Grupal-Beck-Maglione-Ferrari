package com.DulceMayor.backend.services.facturacion;

import com.DulceMayor.backend.config.AccesoDenegadoException;
import com.DulceMayor.backend.config.ConflictoException;
import com.DulceMayor.backend.config.RecursoNoEncontradoException;
import com.DulceMayor.backend.config.UsuarioPrincipal;
import com.DulceMayor.backend.config.ValidacionException;
import com.DulceMayor.backend.dtos.facturacion.DetalleFacturaRequest;
import com.DulceMayor.backend.dtos.facturacion.DetalleFacturaResponse;
import com.DulceMayor.backend.dtos.facturacion.FacturaMapper;
import com.DulceMayor.backend.dtos.facturacion.FacturaMiCuentaPage;
import com.DulceMayor.backend.dtos.facturacion.FacturaPage;
import com.DulceMayor.backend.dtos.facturacion.FacturaRequest;
import com.DulceMayor.backend.dtos.facturacion.FacturaResponse;
import com.DulceMayor.backend.dtos.producto.ProductoResponse;
import com.DulceMayor.backend.entities.RolUsuario;
import com.DulceMayor.backend.entities.facturacion.DetalleFactura;
import com.DulceMayor.backend.entities.facturacion.EstadoFactura;
import com.DulceMayor.backend.entities.facturacion.Factura;
import com.DulceMayor.backend.entities.pedido.DetallePedido;
import com.DulceMayor.backend.entities.pedido.Pedido;
import com.DulceMayor.backend.entities.persona.Minorista;
import com.DulceMayor.backend.entities.producto.Producto;
import com.DulceMayor.backend.entities.mayorista.Mayorista;
import com.DulceMayor.backend.entities.direccion.Direccion;
import com.DulceMayor.backend.repositories.facturacion.FacturaRepository;
import com.DulceMayor.backend.services.BaseService;
import com.DulceMayor.backend.services.persona.MinoristaService;
import com.DulceMayor.backend.services.mayorista.MayoristaService;
import com.DulceMayor.backend.services.producto.ProductoService;
import com.DulceMayor.backend.services.producto.StockService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import javax.imageio.ImageIO;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.DulceMayor.backend.dtos.facturacion.FacturaGeneradaEvent;
import com.DulceMayor.backend.services.mail.EmailService;
import com.DulceMayor.backend.services.mail.EmailTemplateHelper;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

/**
 * Servicio de negocio para la gestión de {@link Factura} y sus líneas {@link DetalleFactura}.
 *
 * <p><b>Patrón GoF aplicado: Template Method (Comportamiento)</b></p>
 * <ul>
 *   <li><b>Participante GoF:</b> ConcreteClass (Subclase Concreta) que extiende {@link BaseService}.</li>
 *   <li><b>Hooks redefinidos:</b> {@link #aResponse(Factura)} y {@link #buscarEntidadesActivas()}.</li>
 *   <li><b>Operaciones de negocio adicionales:</b> Generación transaccional de factura con descuento de stock
 *       por cada {@link DetalleFactura}, anulación de factura, actualización automática de estado según pagos,
 *       emisión de comprobante PDF y generación de código QR de pago.</li>
 * </ul>
 */
@Service
public class FacturaService extends BaseService<Factura, FacturaRequest, FacturaResponse> {

    private final FacturaRepository facturaRepository;
    private final FacturaLecturaService facturaLecturaService;
    private final ProductoService productoService;
    private final StockService stockService;
    private final MinoristaService minoristaService;
    private final MayoristaService mayoristaService;
    private final EmailService emailService;
    private final ApplicationEventPublisher eventPublisher;

    public FacturaService(
            FacturaRepository facturaRepository,
            FacturaMapper facturaMapper,
            FacturaLecturaService facturaLecturaService,
            ProductoService productoService,
            StockService stockService,
            MinoristaService minoristaService,
            MayoristaService mayoristaService,
            EmailService emailService,
            ApplicationEventPublisher eventPublisher) {
        super(facturaRepository, facturaMapper, "Factura");
        this.facturaRepository = facturaRepository;
        this.facturaLecturaService = facturaLecturaService;
        this.productoService = productoService;
        this.stockService = stockService;
        this.minoristaService = minoristaService;
        this.mayoristaService = mayoristaService;
        this.emailService = emailService;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Valida las reglas de negocio para el alta de factura según el Template Method de {@link BaseService}.
     */
    /// actualmente estos metodos no se llaman nunca porque no se llama el crear, pero sirven por si implementamos
    /// que los jefes puedan hacer algo con las facturas no sé
    @Override
    protected void validarCrear(FacturaRequest request) {
        if (request == null || request.detalles() == null || request.detalles().isEmpty()) {
            throw new ValidacionException("La factura debe contener al menos un detalle");
        }

        if (request.pedidoId() != null && !request.pedidoId().isBlank()) {
            if (facturaRepository.findByPedidoIdAndEliminadoFalse(request.pedidoId().trim()).isPresent()) {
                throw new ConflictoException("DUPLICADO", "Ya existe una factura activa para el pedido: " + request.pedidoId());
            }
        }

        for (DetalleFacturaRequest item : request.detalles()) {
            if (item.cantidad() == null || item.cantidad() < 1) {
                throw new ValidacionException("La cantidad de cada detalle debe ser al menos 1");
            }
            if (item.productoId() == null || item.productoId().isBlank()) {
                throw new ValidacionException("El id del producto es obligatorio en cada detalle");
            }
            productoService.obtenerEntidad(item.productoId());
        }
    }

    /**
     * Hook del Template Method previo a persistir la factura.
     * Resuelve los renglones {@link DetalleFactura}, calcula subtotales, asigna número de factura correlativo,
     * fecha/hora actual y estado inicial {@link EstadoFactura#PENDIENTE_PAGO}.
     */
    @Override
    protected void antesDePersistir(Factura entidad, FacturaRequest request) {
        List<DetalleFactura> detalles = new ArrayList<>();
        double sumaSubtotales = 0.0;

        for (DetalleFacturaRequest item : request.detalles()) {
            Producto producto = productoService.obtenerEntidad(item.productoId());

            Double precioUnitario = item.precioUnitario();
            if (precioUnitario == null || precioUnitario <= 0) {
                ProductoResponse prodDto = productoService.buscar(item.productoId());
                precioUnitario = prodDto.precioVigente();
            }
            if (precioUnitario == null || precioUnitario <= 0) {
                throw new ValidacionException("El producto no posee un precio unitario vigente válido: " + producto.getNombre());
            }

            double subtotal = item.cantidad() * precioUnitario;
            sumaSubtotales += subtotal;

            DetalleFactura detalle = DetalleFactura.builder()
                    .producto(producto)
                    .cantidad(item.cantidad())
                    .precioUnitario(precioUnitario)
                    .subtotal(subtotal)
                    .eliminado(false)
                    .build();
            detalles.add(detalle);
        }

        double descuento = (request.descuento() != null && request.descuento() > 0) ? request.descuento() : 0.0;
        double total = Math.max(0.0, sumaSubtotales - descuento);

        Long siguienteNumero = facturaRepository.findFirstByOrderByNumeroFacturaDesc()
                .map(f -> f.getNumeroFactura() + 1L)
                .orElse(1L);

        entidad.setNumeroFactura(siguienteNumero);
        entidad.setFechaFactura(LocalDateTime.now());
        entidad.setEstado(EstadoFactura.PENDIENTE_PAGO);
        entidad.setTotal(total);
        entidad.setDetalles(detalles);
        entidad.setPedido(null);
    }

    /**
     * Hook del Template Method posterior a la creación y persistencia de la factura.
     * Descuenta atómicamente el stock de cada producto vinculando el movimiento al {@link DetalleFactura}.
     */
    @Override
    protected void despuesDeCrear(Factura guardada, FacturaRequest request) {
        if (guardada.getDetalles() != null) {
            for (DetalleFactura detalleGuardado : guardada.getDetalles()) {
                stockService.descontar(
                        detalleGuardado.getProducto().getId(),
                        detalleGuardado.getCantidad(),
                        detalleGuardado
                );
            }
        }
    }

    /**
     * Genera transaccionalmente la {@link Factura} asociada a un {@link Pedido} durante su procesamiento,
     * clonando sus detalles, heredando su total y descontando el stock de cada producto vinculándolo
     * al {@link DetalleFactura} creado (sección 5.1 de {@code 02-reglas-negocio.md}).
     */
    @Transactional
    public FacturaResponse generarDesdePedido(Pedido pedido) {
        if (pedido == null || pedido.getDetalles() == null || pedido.getDetalles().isEmpty()) {
            throw new ValidacionException("El pedido a facturar debe contener al menos un detalle");
        }
        if (pedido.getId() != null
                && facturaRepository.findByPedidoIdAndEliminadoFalse(pedido.getId()).isPresent()) {
            throw new ConflictoException(
                    "DUPLICADO",
                    "Ya existe una factura activa para el pedido: " + pedido.getId()
            );
        }

        List<DetalleFactura> detallesFactura = new ArrayList<>();
        for (DetallePedido detPedido : pedido.getDetalles()) {
            DetalleFactura detalle = DetalleFactura.builder()
                    .producto(detPedido.getProducto())
                    .cantidad(detPedido.getCantidad())
                    .precioUnitario(detPedido.getPrecioUnitario())
                    .subtotal(detPedido.getSubtotal())
                    .eliminado(false)
                    .build();
            detallesFactura.add(detalle);
        }

        Long siguienteNumero = facturaRepository.findFirstByOrderByNumeroFacturaDesc()
                .map(f -> f.getNumeroFactura() + 1L)
                .orElse(1L);

        String caeSimulado = String.valueOf(74000000000000L + (long) (Math.random() * 9999999999999L));
        LocalDate vtoCae = LocalDate.now().plusDays(10);
        String cuitReceptor = "30-71234567-8";
        if (pedido.getPuntoDeVenta() != null && pedido.getPuntoDeVenta().getMinorista() != null) {
            cuitReceptor = pedido.getPuntoDeVenta().getMinorista().getNumeroDocumento();
        }

        Factura factura = Factura.builder()
                .numeroFactura(siguienteNumero)
                .fechaFactura(LocalDateTime.now())
                .pedido(pedido)
                .estado(EstadoFactura.PENDIENTE_PAGO)
                .total(pedido.getTotal())
                .caeSimulado(caeSimulado)
                .fechaVencimientoCae(vtoCae)
                .cuitReceptor(cuitReceptor)
                .condicionIvaReceptor("Responsable Inscripto")
                .detalles(detallesFactura)
                .eliminado(false)
                .build();

        Factura guardada = facturaRepository.save(factura);

        for (DetalleFactura detalleGuardado : guardada.getDetalles()) {
            stockService.descontar(
                    detalleGuardado.getProducto().getId(),
                    detalleGuardado.getCantidad(),
                    detalleGuardado
            );
        }

        // Publicar evento asíncrono para que los observadores (listeners de correo) envíen la factura en PDF
        String emailMinorista = null;
        if (pedido.getPuntoDeVenta() != null && pedido.getPuntoDeVenta().getMinorista() != null) {
            emailMinorista = emailService.resolverEmailMinorista(pedido.getPuntoDeVenta().getMinorista());
        }
        eventPublisher.publishEvent(new FacturaGeneradaEvent(
                guardada.getId(),
                guardada.getNumeroFactura(),
                pedido.getNumeroPedido(),
                emailMinorista,
                guardada.getTotal()
        ));

        return aResponse(guardada);
    }

    /**
     * Emite transaccionalmente la factura electrónica oficial en estado {@code PAGADA} una vez que el cobro
     * del cliente ha sido acreditado o verificado (regla clave de facturación contra pago).
     */
    @Transactional
    public Factura emitirFacturaConPagoAprobado(Pedido pedido) {
        if (pedido == null || pedido.getDetalles() == null || pedido.getDetalles().isEmpty()) {
            throw new ValidacionException("El pedido a facturar debe contener al menos un detalle");
        }

        // Si ya existe factura para este pedido, actualizarla o retornarla
        java.util.Optional<Factura> existente = facturaRepository.findByPedidoIdAndEliminadoFalse(pedido.getId());
        if (existente.isPresent()) {
            Factura f = existente.get();
            f.setEstado(EstadoFactura.PAGADA);
            return facturaRepository.save(f);
        }

        List<DetalleFactura> detallesFactura = new ArrayList<>();
        for (DetallePedido detPedido : pedido.getDetalles()) {
            DetalleFactura detalle = DetalleFactura.builder()
                    .producto(detPedido.getProducto())
                    .cantidad(detPedido.getCantidad())
                    .precioUnitario(detPedido.getPrecioUnitario())
                    .subtotal(detPedido.getSubtotal())
                    .eliminado(false)
                    .build();
            detallesFactura.add(detalle);
        }

        Long siguienteNumero = facturaRepository.findFirstByOrderByNumeroFacturaDesc()
                .map(f -> f.getNumeroFactura() + 1L)
                .orElse(1L);

        String caeSimulado = String.valueOf(74000000000000L + (long) (Math.random() * 9999999999999L));
        LocalDate vtoCae = LocalDate.now().plusDays(10);
        String cuitReceptor = "30-71234567-8";
        if (pedido.getPuntoDeVenta() != null && pedido.getPuntoDeVenta().getMinorista() != null) {
            cuitReceptor = pedido.getPuntoDeVenta().getMinorista().getNumeroDocumento();
        }

        Factura factura = Factura.builder()
                .numeroFactura(siguienteNumero)
                .fechaFactura(LocalDateTime.now())
                .pedido(pedido)
                .estado(EstadoFactura.PAGADA)
                .total(pedido.getTotal())
                .caeSimulado(caeSimulado)
                .fechaVencimientoCae(vtoCae)
                .cuitReceptor(cuitReceptor)
                .condicionIvaReceptor("Responsable Inscripto")
                .detalles(detallesFactura)
                .eliminado(false)
                .build();

        Factura guardada = facturaRepository.save(factura);

        for (DetalleFactura detalleGuardado : guardada.getDetalles()) {
            stockService.descontar(
                    detalleGuardado.getProducto().getId(),
                    detalleGuardado.getCantidad(),
                    detalleGuardado
            );
        }

        String emailMinorista = null;
        if (pedido.getPuntoDeVenta() != null && pedido.getPuntoDeVenta().getMinorista() != null) {
            emailMinorista = emailService.resolverEmailMinorista(pedido.getPuntoDeVenta().getMinorista());
        }
        eventPublisher.publishEvent(new FacturaGeneradaEvent(
                guardada.getId(),
                guardada.getNumeroFactura(),
                pedido.getNumeroPedido(),
                emailMinorista,
                guardada.getTotal()
        ));

        return guardada;
    }

    @Override
    protected FacturaResponse aResponse(Factura entidad) {
        return facturaLecturaService.enriquecerResponse(entidad);
    }

    @Override
    protected List<Factura> buscarEntidadesActivas() {
        return facturaRepository.findAllByEliminadoFalseOrderByFechaFacturaDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<FacturaResponse> listar() {
        List<Factura> entidades = buscarEntidadesActivas();
        List<FacturaResponse> resultado = new ArrayList<>();
        for (Factura f : entidades) {
            resultado.add(aResponse(f));
        }
        return resultado;
    }

    /**
     * Busca la factura activa asociada a un pedido específico retornando un {@link Optional}.
     *
     * @param pedidoId Identificador único del pedido.
     * @return {@link Optional} con el {@link FacturaResponse} si existe factura activa asociada, o vacío en caso contrario.
     */
    @Transactional(readOnly = true)
    public java.util.Optional<FacturaResponse> buscarPorPedidoId(String pedidoId) {
        if (pedidoId == null || pedidoId.isBlank()) {
            return java.util.Optional.empty();
        }
        return facturaRepository.findByPedidoIdAndEliminadoFalse(pedidoId.trim())
                .map(this::aResponse);
    }

    /**
     * Busca la factura activa asociada a un pedido específico.
     */
    @Transactional(readOnly = true)
    public FacturaResponse buscarFacturaPorPedido(String pedidoId) {
        Factura factura = facturaRepository.findByPedidoIdAndEliminadoFalse(pedidoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Factura no encontrada para el pedido con id: " + pedidoId));
        return aResponse(factura);
    }

    /**
     * Anula una factura existente siempre que no se encuentre ya anulada ni posea pagos activos registrados
     * (regla 7.3 de {@code 02-reglas-negocio.md}).
     */
    @Transactional
    public FacturaResponse anularFactura(String id) {
        Factura factura = obtenerEntidad(id);
        if (factura.getEstado() == EstadoFactura.ANULADA) {
            throw new ConflictoException("TRANSICION_INVALIDA", "La factura ya se encuentra anulada");
        }
        if (facturaLecturaService.tienePagosActivos(id)) {
            throw new ConflictoException("CONFLICTO", "No se puede anular una factura que posee pagos registrados");
        }
        factura.setEstado(EstadoFactura.ANULADA);
        Factura guardada = facturaRepository.save(factura);
        return aResponse(guardada);
    }

    /**
     * Actualiza el estado de la factura ({@code PENDIENTE_PAGO} o {@code PAGADA}) en función del total pagado acumulado
     * (regla 7.2 de {@code 02-reglas-negocio.md}).
     */
    @Transactional
    public void actualizarEstadoSegunPagos(Factura factura, double totalPagado) {
        if (factura.getEstado() == EstadoFactura.ANULADA) {
            return;
        }
        if (totalPagado >= factura.getTotal() - 0.001) {
            factura.setEstado(EstadoFactura.PAGADA);
        } else {
            factura.setEstado(EstadoFactura.PENDIENTE_PAGO);
        }
        facturaRepository.save(factura);
    }

    /**
     * Genera la imagen PNG del código QR de pago para una factura en estado {@code PENDIENTE_PAGO}
     * (regla 7.4 de {@code 02-reglas-negocio.md}).
     */
    @Transactional(readOnly = true)
    public byte[] generarQrPago(String id) {
        Factura factura = obtenerEntidad(id);
        if (factura.getEstado() != EstadoFactura.PENDIENTE_PAGO) {
            throw new ConflictoException(
                    "CONFLICTO",
                    "Solo se puede generar el código QR para facturas en estado PENDIENTE_PAGO"
            );
        }

        double totalPagado = facturaLecturaService.obtenerTotalPagado(id);
        double saldo = Math.max(0.0, factura.getTotal() - totalPagado);
        String payload = String.format(Locale.US, "DULCEMAYOR|FACTURA:%s|NRO:%d|MONTO:%.2f",
                factura.getId(), factura.getNumeroFactura(), saldo);

        return renderizarMatrizQrPng(payload);
    }

    /**
     * Genera el documento PDF de una factura con su encabezado oficial de AFIP, logo institucional,
     * detalle de renglones, totales y código QR fiscal de validación.
     */
    @Transactional(readOnly = true)
    public byte[] generarFacturaPdf(String id) {
        Factura factura = obtenerEntidad(id);
        verificarAccesoMinorista(factura);
        FacturaResponse response = aResponse(factura);
        return construirPdfFactura(factura, response);
    }

    /**
     * Reenvía la factura por correo electrónico al cliente asociado con su PDF adjunto.
     */
    @Transactional(readOnly = true)
    public void reenviarFacturaPorCorreo(String id) {
        Factura factura = obtenerEntidad(id);
        verificarAccesoMinorista(factura);

        String emailDestino = null;
        String numeroPedido = "-";
        if (factura.getPedido() != null) {
            numeroPedido = String.valueOf(factura.getPedido().getNumeroPedido());
            if (factura.getPedido().getPuntoDeVenta() != null && factura.getPedido().getPuntoDeVenta().getMinorista() != null) {
                emailDestino = emailService.resolverEmailMinorista(factura.getPedido().getPuntoDeVenta().getMinorista());
            }
        }

        if (emailDestino == null || emailDestino.isBlank()) {
            throw new ValidacionException("El cliente asociado a la factura no posee un correo electrónico registrado");
        }

        byte[] pdf = generarFacturaPdf(id);
        String asunto = "Factura #" + factura.getNumeroFactura() + " - Dulce Mayor";
        String html = EmailTemplateHelper.generarHtmlFactura(
                numeroPedido,
                String.valueOf(factura.getNumeroFactura()),
                String.format(Locale.US, "%.2f", factura.getTotal())
        );

        emailService.enviarCorreoConAdjunto(
                emailDestino,
                asunto,
                html,
                pdf,
                "factura-" + factura.getNumeroFactura() + ".pdf",
                "application/pdf"
        );
    }

    @Override
    @Transactional(readOnly = true)
    public FacturaResponse buscar(String id) {
        Factura factura = obtenerEntidad(id);
        verificarAccesoMinorista(factura);
        return aResponse(factura);
    }

    /**
     * Consulta paginada de facturas con filtros opcionales.
     */
    @Transactional(readOnly = true)
    public FacturaPage listarPaginado(
            String estadoStr,
            String minoristaId,
            LocalDate desde,
            LocalDate hasta,
            Pageable pageable) {
        EstadoFactura estado = parsearEstado(estadoStr);
        return facturaLecturaService.listarPaginado(estado, minoristaId, desde, hasta, pageable);
    }

    /**
     * Consulta paginada de las facturas del cliente minorista autenticado ("Mi cuenta").
     */
    @Transactional(readOnly = true)
    public FacturaMiCuentaPage listarMiCuenta(String estadoStr, Pageable pageable) {
        EstadoFactura estado = parsearEstado(estadoStr);
        UsuarioPrincipal principal = obtenerPrincipalActual();
        String minoristaId = null;
        if (principal != null && principal.getRol() == RolUsuario.MINORISTA) {
            minoristaId = minoristaService.buscarPorUsuarioId(principal.getId())
                    .map(Minorista::getId)
                    .orElse(null);
        }
        return facturaLecturaService.listarMiCuenta(estado, minoristaId, pageable);
    }

    private void verificarAccesoMinorista(Factura factura) {
        UsuarioPrincipal principal = obtenerPrincipalActual();
        if (principal != null && principal.getRol() == RolUsuario.MINORISTA) {
            java.util.Optional<Minorista> minoristaOpt = minoristaService.buscarPorUsuarioId(principal.getId());
            if (minoristaOpt.isPresent()) {
                Minorista minorista = minoristaOpt.get();
                if (factura.getPedido() != null
                        && factura.getPedido().getPuntoDeVenta() != null
                        && factura.getPedido().getPuntoDeVenta().getMinorista() != null
                        && !factura.getPedido().getPuntoDeVenta().getMinorista().getId().equals(minorista.getId())) {
                    throw new AccesoDenegadoException("No tiene permisos para acceder a una factura de otro cliente");
                }
            }
        }
    }

    private UsuarioPrincipal obtenerPrincipalActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UsuarioPrincipal up) {
            return up;
        }
        return null;
    }

    private EstadoFactura parsearEstado(String estadoStr) {
        if (estadoStr == null || estadoStr.isBlank()) {
            return null;
        }
        try {
            return EstadoFactura.valueOf(estadoStr.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new ValidacionException("Estado de factura inválido: " + estadoStr);
        }
    }

    private byte[] renderizarMatrizQrPng(String payload) {
        int size = 240;
        int modules = 25;
        int moduleSize = size / modules;
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        try {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, size, size);
            g.setColor(Color.BLACK);

            dibujarPatronPosicionQr(g, 1, 1, moduleSize);
            dibujarPatronPosicionQr(g, modules - 8, 1, moduleSize);
            dibujarPatronPosicionQr(g, 1, modules - 8, moduleSize);

            byte[] bytes = payload.getBytes(StandardCharsets.UTF_8);
            int bitIndex = 0;
            for (int y = 9; y < modules - 1; y++) {
                for (int x = 9; x < modules - 1; x++) {
                    byte b = bytes[(bitIndex / 8) % bytes.length];
                    boolean dark = ((b >> (bitIndex % 8)) & 1) == 1;
                    if (dark) {
                        g.fillRect(x * moduleSize, y * moduleSize, moduleSize, moduleSize);
                    }
                    bitIndex++;
                }
            }
        } finally {
            g.dispose();
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo generar la imagen PNG del QR de pago", e);
        }
    }

    private void dibujarPatronPosicionQr(Graphics2D g, int startX, int startY, int moduleSize) {
        g.fillRect(startX * moduleSize, startY * moduleSize, 7 * moduleSize, 7 * moduleSize);
        g.setColor(Color.WHITE);
        g.fillRect((startX + 1) * moduleSize, (startY + 1) * moduleSize, 5 * moduleSize, 5 * moduleSize);
        g.setColor(Color.BLACK);
        g.fillRect((startX + 2) * moduleSize, (startY + 2) * moduleSize, 3 * moduleSize, 3 * moduleSize);
    }

    private byte[] construirPdfFactura(FacturaResponse factura) {
        return construirPdfFactura(null, factura);
    }

    private byte[] construirPdfFactura(Factura factura, FacturaResponse response) {
        if (response == null) {
            throw new ValidacionException("No se pueden generar comprobantes PDF sin datos de factura");
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 25f, 25f, 25f, 25f);
            PdfWriter.getInstance(document, baos);
            document.open();

            // Paleta de colores acorde a la identidad institucional de Dulce Mayor y AFIP
            Color colorBorde = new Color(195, 198, 209);
            Color colorFondoGris = new Color(246, 247, 251);
            Color colorFondoCabecera = new Color(237, 240, 247);
            Color colorTextoOscuro = new Color(43, 43, 58);
            Color colorTextoSecundario = new Color(107, 107, 128);

            // Tipografías
            Font fEmpresaNombre = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12f, colorTextoOscuro);
            Font fEmpresaSub = FontFactory.getFont(FontFactory.HELVETICA, 7.5f, colorTextoSecundario);
            Font fRegular = FontFactory.getFont(FontFactory.HELVETICA, 8f, colorTextoOscuro);
            Font fBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8f, colorTextoOscuro);
            Font fTituloComp = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14f, colorTextoOscuro);
            Font fLetra = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22f, colorTextoOscuro);
            Font fLetraCod = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 6.5f, colorTextoSecundario);
            Font fTh = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8f, colorTextoOscuro);
            Font fTb = FontFactory.getFont(FontFactory.HELVETICA, 8f, colorTextoOscuro);
            Font fTotalTit = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9.5f, colorTextoOscuro);
            Font fTotalVal = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11.5f, new Color(20, 120, 60));
            Font fAviso = FontFactory.getFont(FontFactory.HELVETICA, 7f, colorTextoSecundario);

            // Determinación de tipo de comprobante AFIP (Letra "A" para RI, Letra "B" para Monotributo / Consumidor Final)
            String condicionIvaRec = (factura != null && factura.getCondicionIvaReceptor() != null)
                    ? factura.getCondicionIvaReceptor()
                    : "Responsable Inscripto";
            boolean esFacturaA = "Responsable Inscripto".equalsIgnoreCase(condicionIvaRec);
            String letraComp = esFacturaA ? "A" : "B";
            String codComp = esFacturaA ? "COD. 01" : "COD. 06";

            DateTimeFormatter fmtFechaHora = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            DateTimeFormatter fmtFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            LocalDateTime fechaEmisionLdt = response.fechaFactura() != null
                    ? response.fechaFactura()
                    : (factura != null && factura.getFechaFactura() != null ? factura.getFechaFactura() : LocalDateTime.now());
            String fechaEmisionStr = fmtFechaHora.format(fechaEmisionLdt);
            String fechaSoloStr = fmtFecha.format(fechaEmisionLdt);

            LocalDate fechaVtoCaeLd = (factura != null && factura.getFechaVencimientoCae() != null)
                    ? factura.getFechaVencimientoCae()
                    : fechaEmisionLdt.toLocalDate().plusDays(10);
            String fechaVtoCaeStr = fmtFecha.format(fechaVtoCaeLd);

            long numeroFactura = response.numeroFactura() != null
                    ? response.numeroFactura()
                    : (factura != null ? factura.getNumeroFactura() : 1L);

            // 0. DATOS OFICIALES DEL EMISOR (Obtenidos dinámicamente desde la BD de Sede Central)
            Mayorista sedeCentral = null;
            try {
                if (mayoristaService != null) {
                    sedeCentral = mayoristaService.obtenerSedeCentral();
                }
            } catch (Exception ignored) {
                // Si la base de datos o servicio no responde, utiliza valores seguros de respaldo
            }

            String razonSocialEmisor = (sedeCentral != null && sedeCentral.getRazonSocial() != null && !sedeCentral.getRazonSocial().isBlank())
                    ? sedeCentral.getRazonSocial()
                    : "Dulce Mayor S.A.";
            String cuitEmisor = (sedeCentral != null && sedeCentral.getCuit() != null && !sedeCentral.getCuit().isBlank())
                    ? sedeCentral.getCuit()
                    : "30-71234567-9";

            String domicilioEmisor = "Av. San Martín 1020, Ciudad de Mendoza";
            if (sedeCentral != null && sedeCentral.getDirecciones() != null && !sedeCentral.getDirecciones().isEmpty()) {
                Direccion dirCentral = sedeCentral.getDirecciones().get(0);
                String c = dirCentral.getCalle() != null ? dirCentral.getCalle() : "";
                String n = dirCentral.getNumeracion() != null ? dirCentral.getNumeracion() : "";
                String loc = (dirCentral.getLocalidad() != null && dirCentral.getLocalidad().getNombre() != null)
                        ? dirCentral.getLocalidad().getNombre()
                        : "";
                domicilioEmisor = String.format("%s %s - %s", c, n, loc).trim();
            }

            // 1. ENCABEZADO OFICIAL AFIP (3 columnas: Emisor con Logo | Letra Central | Comprobante)
            PdfPTable tblEncabezado = new PdfPTable(new float[]{45f, 10f, 45f});
            tblEncabezado.setWidthPercentage(100f);

            // Celda Izquierda: Logo y Datos del Emisor
            PdfPCell celdaEmisor = new PdfPCell();
            celdaEmisor.setBorder(Rectangle.BOX);
            celdaEmisor.setBorderColor(colorBorde);
            celdaEmisor.setPadding(6f);

            try {
                ClassPathResource logoRes = new ClassPathResource("static/images/logo.png");
                if (logoRes.exists()) {
                    try (InputStream is = logoRes.getInputStream()) {
                        byte[] logoBytes = is.readAllBytes();
                        Image logo = Image.getInstance(logoBytes);
                        logo.scaleToFit(65f, 65f);
                        logo.setAlignment(Element.ALIGN_LEFT);
                        celdaEmisor.addElement(logo);
                    }
                }
            } catch (Exception ignored) {
                // Si la imagen no está disponible, el documento continúa sin interrumpirse
            }

            celdaEmisor.addElement(new Paragraph(razonSocialEmisor, fEmpresaNombre));
            celdaEmisor.addElement(new Paragraph("Distribuidora Mayorista de Golosinas y Alimentos", fEmpresaSub));
            celdaEmisor.addElement(new Paragraph("Domicilio Comercial: " + domicilioEmisor, fRegular));
            celdaEmisor.addElement(new Paragraph("Condición frente al IVA: IVA Responsable Inscripto", fBold));
            tblEncabezado.addCell(celdaEmisor);

            // Celda Central: Recuadro de Letra del Comprobante
            PdfPCell celdaLetra = new PdfPCell();
            celdaLetra.setBorder(Rectangle.BOX);
            celdaLetra.setBorderColor(colorBorde);
            celdaLetra.setBackgroundColor(colorFondoGris);
            celdaLetra.setHorizontalAlignment(Element.ALIGN_CENTER);
            celdaLetra.setVerticalAlignment(Element.ALIGN_MIDDLE);
            celdaLetra.setPadding(4f);

            Paragraph pLetra = new Paragraph(letraComp, fLetra);
            pLetra.setAlignment(Element.ALIGN_CENTER);
            celdaLetra.addElement(pLetra);

            Paragraph pCod = new Paragraph(codComp, fLetraCod);
            pCod.setAlignment(Element.ALIGN_CENTER);
            celdaLetra.addElement(pCod);
            tblEncabezado.addCell(celdaLetra);

            // Celda Derecha: Datos del Comprobante Fiscal
            PdfPCell celdaComp = new PdfPCell();
            celdaComp.setBorder(Rectangle.BOX);
            celdaComp.setBorderColor(colorBorde);
            celdaComp.setPadding(6f);

            Paragraph pTitComp = new Paragraph("FACTURA", fTituloComp);
            pTitComp.setAlignment(Element.ALIGN_LEFT);
            celdaComp.addElement(pTitComp);

            celdaComp.addElement(new Paragraph(String.format(Locale.US, "Punto de Venta: 0001    Comp. Nro: %08d", numeroFactura), fBold));
            celdaComp.addElement(new Paragraph("Fecha de Emisión: " + fechaEmisionStr, fRegular));
            celdaComp.addElement(new Paragraph("CUIT: " + cuitEmisor, fRegular));
            celdaComp.addElement(new Paragraph("Ingresos Brutos: " + cuitEmisor, fRegular));
            celdaComp.addElement(new Paragraph("Fecha de Inicio de Actividades: 01/03/2020", fRegular));
            tblEncabezado.addCell(celdaComp);

            document.add(tblEncabezado);

            // 2. BARRA DE PERÍODO FACTURADO
            PdfPTable tblPeriodo = new PdfPTable(new float[]{33.3f, 33.3f, 33.4f});
            tblPeriodo.setWidthPercentage(100f);
            tblPeriodo.setSpacingBefore(3f);

            tblPeriodo.addCell(crearCeldaPeriodo("Período Facturado Desde: " + fechaSoloStr, fRegular, colorBorde, colorFondoGris));
            tblPeriodo.addCell(crearCeldaPeriodo("Hasta: " + fechaSoloStr, fRegular, colorBorde, colorFondoGris));
            tblPeriodo.addCell(crearCeldaPeriodo("Fecha de Vto. para el pago: " + fechaVtoCaeStr, fBold, colorBorde, colorFondoGris));
            document.add(tblPeriodo);

            // 3. DATOS DEL CLIENTE / RECEPTOR
            String cuitRec = (factura != null && factura.getCuitReceptor() != null && !factura.getCuitReceptor().isBlank())
                    ? factura.getCuitReceptor()
                    : "Consumidor Final";

            String razonSocialRec = "Cliente Minorista";
            String domicilioRec = "-";
            String formaPagoRec = "Cuenta Corriente / A convenir";
            String numeroPedidoStr = (response.pedidoId() != null) ? response.pedidoId() : "-";

            if (factura != null && factura.getPedido() != null) {
                Pedido ped = factura.getPedido();
                numeroPedidoStr = String.valueOf(ped.getNumeroPedido());
                if (ped.getFormaDePago() != null && ped.getFormaDePago().getTipoPago() != null) {
                    formaPagoRec = ped.getFormaDePago().getTipoPago().name();
                }
                if (ped.getPuntoDeVenta() != null) {
                    if (ped.getPuntoDeVenta().getNombreComercio() != null) {
                        razonSocialRec = ped.getPuntoDeVenta().getNombreComercio();
                    }
                    if (ped.getPuntoDeVenta().getDireccion() != null) {
                        var dir = ped.getPuntoDeVenta().getDireccion();
                        String locNombre = (dir.getLocalidad() != null && dir.getLocalidad().getNombre() != null)
                                ? dir.getLocalidad().getNombre()
                                : "";
                        domicilioRec = String.format("%s %s, %s",
                                dir.getCalle() != null ? dir.getCalle() : "",
                                dir.getNumeracion() != null ? dir.getNumeracion() : "",
                                locNombre).trim();
                    }
                    if (ped.getPuntoDeVenta().getMinorista() != null) {
                        var min = ped.getPuntoDeVenta().getMinorista();
                        if (razonSocialRec.equals("Cliente Minorista") && min.getNombre() != null) {
                            razonSocialRec = min.getNombre() + " " + (min.getApellido() != null ? min.getApellido() : "");
                        }
                    }
                }
            }

            PdfPTable tblCliente = new PdfPTable(new float[]{50f, 50f});
            tblCliente.setWidthPercentage(100f);
            tblCliente.setSpacingBefore(3f);

            tblCliente.addCell(crearCeldaDato("CUIT / Identificación:", cuitRec, fBold, fRegular, colorBorde));
            tblCliente.addCell(crearCeldaDato("Apellido y Nombre / Razón Social:", razonSocialRec, fBold, fRegular, colorBorde));
            tblCliente.addCell(crearCeldaDato("Condición frente al IVA:", condicionIvaRec, fBold, fRegular, colorBorde));
            tblCliente.addCell(crearCeldaDato("Domicilio Comercial:", domicilioRec, fBold, fRegular, colorBorde));
            tblCliente.addCell(crearCeldaDato("Condición de Venta:", formaPagoRec, fBold, fRegular, colorBorde));
            tblCliente.addCell(crearCeldaDato("Pedido Dulce Mayor:", "#" + numeroPedidoStr, fBold, fRegular, colorBorde));

            document.add(tblCliente);

            // 4. TABLA DE PRODUCTOS / ÍTEMS FACTURADOS
            PdfPTable tblItems = new PdfPTable(new float[]{12f, 48f, 10f, 15f, 15f});
            tblItems.setWidthPercentage(100f);
            tblItems.setSpacingBefore(5f);

            tblItems.addCell(crearCeldaTh("Código", Element.ALIGN_CENTER, fTh, colorFondoCabecera, colorBorde));
            tblItems.addCell(crearCeldaTh("Descripción / Producto", Element.ALIGN_LEFT, fTh, colorFondoCabecera, colorBorde));
            tblItems.addCell(crearCeldaTh("Cantidad", Element.ALIGN_CENTER, fTh, colorFondoCabecera, colorBorde));
            tblItems.addCell(crearCeldaTh("Precio Unit.", Element.ALIGN_RIGHT, fTh, colorFondoCabecera, colorBorde));
            tblItems.addCell(crearCeldaTh("Subtotal", Element.ALIGN_RIGHT, fTh, colorFondoCabecera, colorBorde));

            List<DetalleFacturaResponse> detalles = response.detalles() != null ? response.detalles() : List.of();
            if (detalles.isEmpty()) {
                PdfPCell celdaVacia = new PdfPCell(new Phrase("Sin renglones registrados", fTb));
                celdaVacia.setColspan(5);
                celdaVacia.setPadding(8f);
                celdaVacia.setHorizontalAlignment(Element.ALIGN_CENTER);
                celdaVacia.setBorderColor(colorBorde);
                tblItems.addCell(celdaVacia);
            } else {
                int filaIdx = 0;
                for (DetalleFacturaResponse det : detalles) {
                    Color bgFila = (filaIdx % 2 == 1) ? colorFondoGris : Color.WHITE;
                    String cod = String.format("ART-%03d", filaIdx + 1);
                    String prod = det.productoNombre() != null ? det.productoNombre() : "Producto";
                    int cant = det.cantidad() != null ? det.cantidad() : 1;
                    double pu = det.precioUnitario() != null ? det.precioUnitario() : 0.0;
                    double sub = det.subtotal() != null ? det.subtotal() : (cant * pu);

                    tblItems.addCell(crearCeldaTb(cod, Element.ALIGN_CENTER, fTb, bgFila, colorBorde));
                    tblItems.addCell(crearCeldaTb(prod, Element.ALIGN_LEFT, fTb, bgFila, colorBorde));
                    tblItems.addCell(crearCeldaTb(String.valueOf(cant), Element.ALIGN_CENTER, fTb, bgFila, colorBorde));
                    tblItems.addCell(crearCeldaTb(String.format(Locale.GERMANY, "$ %,.2f", pu), Element.ALIGN_RIGHT, fTb, bgFila, colorBorde));
                    tblItems.addCell(crearCeldaTb(String.format(Locale.GERMANY, "$ %,.2f", sub), Element.ALIGN_RIGHT, fTb, bgFila, colorBorde));
                    filaIdx++;
                }
            }
            document.add(tblItems);

            // 5. RESUMEN FINANCIERO Y TOTALES
            double totalFactura = response.total() != null ? response.total() : (factura != null ? factura.getTotal() : 0.0);
            double totalPagado = response.totalPagado() != null ? response.totalPagado() : 0.0;
            double saldoPendiente = response.saldo() != null ? response.saldo() : Math.max(0.0, totalFactura - totalPagado);
            EstadoFactura estado = response.estado() != null ? response.estado() : (factura != null ? factura.getEstado() : EstadoFactura.PENDIENTE_PAGO);

            PdfPTable tblTotales = new PdfPTable(new float[]{55f, 45f});
            tblTotales.setWidthPercentage(100f);
            tblTotales.setSpacingBefore(4f);

            // Columna Izquierda: Estado del pago y saldo
            PdfPCell celdaEstado = new PdfPCell();
            celdaEstado.setBorder(Rectangle.BOX);
            celdaEstado.setBorderColor(colorBorde);
            celdaEstado.setPadding(6f);

            String estadoBadge = (estado == EstadoFactura.PAGADA) ? "COMPROBANTE PAGADO" :
                    (estado == EstadoFactura.ANULADA ? "COMPROBANTE ANULADO" : "PENDIENTE DE PAGO");
            Color colorBadge = (estado == EstadoFactura.PAGADA) ? new Color(20, 140, 60) :
                    (estado == EstadoFactura.ANULADA ? new Color(200, 40, 40) : new Color(180, 110, 10));

            Font fBadge = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9f, colorBadge);
            celdaEstado.addElement(new Paragraph("Estado Fiscal: " + estadoBadge, fBadge));
            celdaEstado.addElement(new Paragraph(String.format(Locale.GERMANY, "Total Cobrado: $ %,.2f", totalPagado), fRegular));
            celdaEstado.addElement(new Paragraph(String.format(Locale.GERMANY, "Saldo Pendiente: $ %,.2f", saldoPendiente), fBold));
            tblTotales.addCell(celdaEstado);

            // Columna Derecha: Desglose Neto / IVA / Total
            PdfPCell celdaMontos = new PdfPCell();
            celdaMontos.setBorder(Rectangle.BOX);
            celdaMontos.setBorderColor(colorBorde);
            celdaMontos.setPadding(6f);

            if (esFacturaA) {
                double neto = totalFactura / 1.21;
                double iva = totalFactura - neto;
                celdaMontos.addElement(crearFilaImporte("Importe Neto Gravado:", String.format(Locale.GERMANY, "$ %,.2f", neto), fRegular));
                celdaMontos.addElement(crearFilaImporte("IVA (21.00%):", String.format(Locale.GERMANY, "$ %,.2f", iva), fRegular));
            } else {
                celdaMontos.addElement(crearFilaImporte("Subtotal:", String.format(Locale.GERMANY, "$ %,.2f", totalFactura), fRegular));
            }

            PdfPTable tblTotalDestacado = new PdfPTable(new float[]{50f, 50f});
            tblTotalDestacado.setWidthPercentage(100f);
            PdfPCell cTotLab = new PdfPCell(new Phrase("Importe Total:", fTotalTit));
            cTotLab.setBorder(Rectangle.NO_BORDER);
            cTotLab.setBackgroundColor(colorFondoCabecera);
            cTotLab.setPadding(4f);
            PdfPCell cTotVal = new PdfPCell(new Phrase(String.format(Locale.GERMANY, "$ %,.2f", totalFactura), fTotalVal));
            cTotVal.setBorder(Rectangle.NO_BORDER);
            cTotVal.setBackgroundColor(colorFondoCabecera);
            cTotVal.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cTotVal.setPadding(4f);
            tblTotalDestacado.addCell(cTotLab);
            tblTotalDestacado.addCell(cTotVal);
            celdaMontos.addElement(tblTotalDestacado);

            tblTotales.addCell(celdaMontos);
            document.add(tblTotales);

            // 6. PIE LEGAL FISCAL (QR AFIP + CAE)
            String cae = (factura != null && factura.getCaeSimulado() != null) ? factura.getCaeSimulado() : "74219845120394";

            PdfPTable tblAfip = new PdfPTable(new float[]{20f, 80f});
            tblAfip.setWidthPercentage(100f);
            tblAfip.setSpacingBefore(6f);

            // Celda Izquierda: QR Fiscal de AFIP
            PdfPCell celdaQr = new PdfPCell();
            celdaQr.setBorder(Rectangle.BOX);
            celdaQr.setBorderColor(colorBorde);
            celdaQr.setHorizontalAlignment(Element.ALIGN_CENTER);
            celdaQr.setVerticalAlignment(Element.ALIGN_MIDDLE);
            celdaQr.setPadding(4f);

            byte[] qrBytes = generarQrAfipBytes(factura, response, numeroFactura, totalFactura, cae, fechaEmisionLdt.toLocalDate(), cuitRec, cuitEmisor);
            if (qrBytes != null) {
                Image qrImg = Image.getInstance(qrBytes);
                qrImg.scaleToFit(65f, 65f);
                qrImg.setAlignment(Element.ALIGN_CENTER);
                celdaQr.addElement(qrImg);
            }
            tblAfip.addCell(celdaQr);

            // Celda Derecha: Datos de CAE y leyendas reglamentarias
            PdfPCell celdaCae = new PdfPCell();
            celdaCae.setBorder(Rectangle.BOX);
            celdaCae.setBorderColor(colorBorde);
            celdaCae.setPadding(6f);

            Paragraph pCae = new Paragraph();
            pCae.add(new Phrase("CAE N°: ", fBold));
            pCae.add(new Phrase(cae, fTituloComp));
            celdaCae.addElement(pCae);

            Paragraph pVto = new Paragraph();
            pVto.add(new Phrase("Fecha de Vto. de CAE: ", fBold));
            pVto.add(new Phrase(fechaVtoCaeStr, fBold));
            celdaCae.addElement(pVto);

            celdaCae.addElement(new Paragraph(
                    "Esta Administración Federal no se responsabiliza por los datos ingresados en el comprobante (Resolución General AFIP N° 4291/2018).",
                    fAviso
            ));
            celdaCae.addElement(new Paragraph(
                    "Comprobante fiscal electrónico oficial emitido por Dulce Mayor S.A.",
                    fAviso
            ));
            tblAfip.addCell(celdaCae);

            document.add(tblAfip);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Error al construir el comprobante PDF de la factura: " + e.getMessage(), e);
        }
    }

    private PdfPCell crearCeldaPeriodo(String texto, Font font, Color colorBorde, Color colorFondo) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, font));
        celda.setBackgroundColor(colorFondo);
        celda.setBorder(Rectangle.BOX);
        celda.setBorderColor(colorBorde);
        celda.setPadding(4f);
        return celda;
    }

    private PdfPCell crearCeldaDato(String etiqueta, String valor, Font fontEtiqueta, Font fontValor, Color colorBorde) {
        Paragraph p = new Paragraph();
        p.add(new Phrase(etiqueta + " ", fontEtiqueta));
        p.add(new Phrase(valor != null ? valor : "-", fontValor));
        PdfPCell celda = new PdfPCell(p);
        celda.setBorder(Rectangle.BOX);
        celda.setBorderColor(colorBorde);
        celda.setPadding(3.5f);
        return celda;
    }

    private PdfPCell crearCeldaTh(String texto, int alineacion, Font font, Color colorFondo, Color colorBorde) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, font));
        celda.setBackgroundColor(colorFondo);
        celda.setBorder(Rectangle.BOX);
        celda.setBorderColor(colorBorde);
        celda.setHorizontalAlignment(alineacion);
        celda.setPadding(5f);
        return celda;
    }

    private PdfPCell crearCeldaTb(String texto, int alineacion, Font font, Color colorFondo, Color colorBorde) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, font));
        celda.setBackgroundColor(colorFondo);
        celda.setBorder(Rectangle.BOX);
        celda.setBorderColor(colorBorde);
        celda.setHorizontalAlignment(alineacion);
        celda.setPadding(4f);
        return celda;
    }

    private Paragraph crearFilaImporte(String etiqueta, String valor, Font font) {
        Paragraph p = new Paragraph();
        p.add(new Phrase(etiqueta + " ", font));
        p.add(new Phrase(valor, font));
        p.setAlignment(Element.ALIGN_RIGHT);
        return p;
    }

    private byte[] generarQrAfipBytes(
            Factura factura,
            FacturaResponse response,
            long numeroFactura,
            double total,
            String cae,
            LocalDate fechaEmision,
            String cuitReceptor,
            String cuitEmisor) {
        try {
            int tipoCmp = (factura != null && "Responsable Inscripto".equalsIgnoreCase(factura.getCondicionIvaReceptor())) ? 1 : 6;
            String fechaStr = fechaEmision != null ? fechaEmision.toString() : LocalDate.now().toString();
            String cuitLimpio = limpiarSoloNumeros(cuitReceptor, "30712345678");
            String cuitEmisorLimpio = limpiarSoloNumeros(cuitEmisor, "30712345679");

            String jsonAfip = String.format(Locale.US,
                    "{\"ver\":1,\"fecha\":\"%s\",\"cuit\":%s,\"ptoVta\":1,\"tipoCmp\":%d,\"nroCmp\":%d,\"importe\":%.2f,\"moneda\":\"PES\",\"ctz\":1,\"tipoDocRec\":80,\"nroDocRec\":%s,\"tipoCodAut\":\"E\",\"codAut\":%s}",
                    fechaStr,
                    cuitEmisorLimpio,
                    tipoCmp,
                    numeroFactura,
                    total,
                    cuitLimpio,
                    cae != null ? cae : "74219845120394"
            );
            String urlAfip = "https://www.afip.gob.ar/fe/qr/?p=" + Base64.getEncoder().encodeToString(jsonAfip.getBytes(StandardCharsets.UTF_8));

            QRCodeWriter qrWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrWriter.encode(urlAfip, BarcodeFormat.QR_CODE, 140, 140);
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                MatrixToImageWriter.writeToStream(bitMatrix, "PNG", baos);
                return baos.toByteArray();
            }
        } catch (Exception e) {
            return null;
        }
    }

    private String limpiarSoloNumeros(String valor, String valorDefecto) {
        if (valor == null || valor.isBlank()) {
            return valorDefecto;
        }
        String limpio = valor.replaceAll("[^0-9]", "");
        return limpio.isBlank() ? valorDefecto : limpio;
    }
}
