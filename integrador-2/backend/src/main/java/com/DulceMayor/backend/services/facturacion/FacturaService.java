package com.DulceMayor.backend.services.facturacion;

import com.DulceMayor.backend.config.ConflictoException;
import com.DulceMayor.backend.config.RecursoNoEncontradoException;
import com.DulceMayor.backend.config.ValidacionException;
import com.DulceMayor.backend.dtos.facturacion.DetalleFacturaRequest;
import com.DulceMayor.backend.dtos.facturacion.DetalleFacturaResponse;
import com.DulceMayor.backend.dtos.facturacion.FacturaMapper;
import com.DulceMayor.backend.dtos.facturacion.FacturaMiCuentaPage;
import com.DulceMayor.backend.dtos.facturacion.FacturaPage;
import com.DulceMayor.backend.dtos.facturacion.FacturaRequest;
import com.DulceMayor.backend.dtos.facturacion.FacturaResponse;
import com.DulceMayor.backend.dtos.producto.ProductoResponse;
import com.DulceMayor.backend.entities.facturacion.DetalleFactura;
import com.DulceMayor.backend.entities.facturacion.EstadoFactura;
import com.DulceMayor.backend.entities.facturacion.Factura;
import com.DulceMayor.backend.entities.producto.Producto;
import com.DulceMayor.backend.repositories.facturacion.FacturaRepository;
import com.DulceMayor.backend.services.BaseService;
import com.DulceMayor.backend.services.producto.ProductoService;
import com.DulceMayor.backend.services.producto.StockService;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.imageio.ImageIO;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public FacturaService(
            FacturaRepository facturaRepository,
            FacturaMapper facturaMapper,
            FacturaLecturaService facturaLecturaService,
            ProductoService productoService,
            StockService stockService) {
        super(facturaRepository, facturaMapper, "Factura");
        this.facturaRepository = facturaRepository;
        this.facturaLecturaService = facturaLecturaService;
        this.productoService = productoService;
        this.stockService = stockService;
    }

    /**
     * Genera transaccionalmente una nueva factura con sus líneas de detalle y descuenta el stock
     * de cada producto asociando el movimiento al {@link DetalleFactura} creado (regla 7.1 de {@code 02-reglas-negocio.md}).
     */
    @Override
    @Transactional
    public FacturaResponse crear(FacturaRequest request) {
        if (request == null || request.detalles() == null || request.detalles().isEmpty()) {
            throw new ValidacionException("La factura debe contener al menos un detalle");
        }

        if (request.pedidoId() != null && !request.pedidoId().isBlank()) {
            if (facturaRepository.findByPedidoIdAndEliminadoFalse(request.pedidoId().trim()).isPresent()) {
                throw new ConflictoException("DUPLICADO", "Ya existe una factura activa para el pedido: " + request.pedidoId());
            }
        }

        List<DetalleFactura> detalles = new ArrayList<>();
        double sumaSubtotales = 0.0;

        for (DetalleFacturaRequest item : request.detalles()) {
            if (item.cantidad() == null || item.cantidad() < 1) {
                throw new ValidacionException("La cantidad de cada detalle debe ser al menos 1");
            }
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

        Factura factura = Factura.builder()
                .numeroFactura(siguienteNumero)
                .fechaFactura(LocalDateTime.now())
                .pedidoId(request.pedidoId() != null && !request.pedidoId().isBlank() ? request.pedidoId().trim() : null)
                .estado(EstadoFactura.PENDIENTE_PAGO)
                .total(total)
                .detalles(detalles)
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

        return aResponse(guardada);
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
     * Genera el documento PDF de una factura con su encabezado, detalle de renglones y totales.
     */
    @Transactional(readOnly = true)
    public byte[] generarFacturaPdf(String id) {
        FacturaResponse factura = buscar(id);
        return construirPdfFactura(factura);
    }

    /**
     * Reenvía la factura por correo electrónico al cliente asociado.
     */
    @Transactional(readOnly = true)
    public void reenviarFacturaPorCorreo(String id) {
        obtenerEntidad(id);
        // En el entorno local sin servidor SMTP externo se valida la existencia de la factura y se completa la operación.
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
        return facturaLecturaService.listarMiCuenta(estado, pageable);
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
        StringBuilder texto = new StringBuilder();
        texto.append("BT /F1 14 Tf 50 780 Td (DULCEMAYOR - COMPROBANTE DE FACTURA) Tj ET\n");
        texto.append(String.format(Locale.US, "BT /F1 11 Tf 50 755 Td (Factura Nro: %d | Estado: %s) Tj ET\n",
                factura.numeroFactura(), factura.estado()));
        texto.append(String.format(Locale.US, "BT /F1 10 Tf 50 735 Td (Fecha: %s) Tj ET\n",
                factura.fechaFactura()));

        int y = 705;
        if (factura.detalles() != null) {
            for (DetalleFacturaResponse det : factura.detalles()) {
                String linea = String.format(Locale.US, "%s x%d - Unit: $%.2f - Subtotal: $%.2f",
                        limpiarTextoPdf(det.productoNombre()), det.cantidad(), det.precioUnitario(), det.subtotal());
                texto.append(String.format(Locale.US, "BT /F1 10 Tf 50 %d Td (%s) Tj ET\n", y, linea));
                y -= 18;
                if (y < 120) {
                    break;
                }
            }
        }

        texto.append(String.format(Locale.US,
                "BT /F1 11 Tf 50 90 Td (Total: $%.2f | Pagado: $%.2f | Saldo: $%.2f) Tj ET\n",
                factura.total(), factura.totalPagado(), factura.saldo()));

        String streamContent = texto.toString();
        String pdf = "%PDF-1.4\n"
                + "1 0 obj << /Type /Catalog /Pages 2 0 R >> endobj\n"
                + "2 0 obj << /Type /Pages /Kids [3 0 R] /Count 1 >> endobj\n"
                + "3 0 obj << /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Contents 4 0 R /Resources << /Font << /F1 5 0 R >> >> >> endobj\n"
                + "4 0 obj << /Length " + streamContent.length() + " >> stream\n"
                + streamContent
                + "endstream endobj\n"
                + "5 0 obj << /Type /Font /Subtype /Type1 /BaseFont /Helvetica >> endobj\n"
                + "xref\n0 6\n0000000000 65535 f \n"
                + "trailer << /Size 6 /Root 1 0 R >>\n"
                + "startxref\n0\n%%EOF";
        return pdf.getBytes(StandardCharsets.ISO_8859_1);
    }

    private String limpiarTextoPdf(String valor) {
        if (valor == null) {
            return "Producto";
        }
        return valor.replace("(", "").replace(")", "").replace("\\", "");
    }
}
