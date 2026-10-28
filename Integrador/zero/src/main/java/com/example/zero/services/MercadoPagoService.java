package com.example.zero.services;

import com.example.zero.entidades.compra.Factura;
import com.example.zero.entidades.compraCliente.DetalleCompra;
import com.example.zero.entidades.compraCliente.OrdenCompra;
import com.example.zero.entidades.persona.Cliente;
import com.example.zero.entidades.persona.Usuario;
import com.example.zero.enums.EstadoOrdenCompra;
import com.example.zero.repositories.FacturaRepository;
import com.example.zero.repositories.OrdenCompraRepository;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.*;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class MercadoPagoService {

    public static final String DEFAULT_ACCESS_TOKEN = "APP_USR-6862216951145229-092415-dc100a089839820f98fa6d5aefabdff4-3693808838";

    @org.springframework.beans.factory.annotation.Value("${mercadopago.access-token:" + DEFAULT_ACCESS_TOKEN + "}")
    private String accessToken = DEFAULT_ACCESS_TOKEN;

    ///se inyecta el dominio que te da ngrok
    @org.springframework.beans.factory.annotation.Value("${mercadopago.base-url:}")
    private String configuredBaseUrl;

    private final OrdenCompraService ordenCompraService;
    private final OrdenCompraRepository ordenCompraRepository;
    private final VentaService ventaService;
    private final FacturaRepository facturaRepository;
    private final StockService stockService;
    private final HttpSession session;


    public void inicializarConfiguracion() {
        MercadoPagoConfig.setAccessToken(accessToken != null && !accessToken.isBlank() ? accessToken.trim() : DEFAULT_ACCESS_TOKEN);
    }

    ///crea una preferencia de pago en Mercado Pago para los productos activos del carrito.
    public String crearPreferenciaParaCarrito(OrdenCompra carrito, Cliente cliente, String baseUrl) throws MPException, MPApiException {
        inicializarConfiguracion();

        List<DetalleCompra> items = ordenCompraService.obtenerItemsActivos(carrito);
        if (items.isEmpty()) {
            throw new IllegalStateException("El carrito de compras no contiene productos para abonar.");
        }

        // validación de stock antes de generar preferencia de pago
        if (stockService != null) {
            for (DetalleCompra item : items) {
                if (item.getProducto() != null) {
                    int stockActual = stockService.calcularStockActual(item.getProducto().getId());
                    if (stockActual < item.getCantidad()) {
                        throw new IllegalArgumentException("Stock insuficiente para: " + item.getProducto().getNombre());
                    }
                }
            }
        }

        ///este tipo te lo da el SDK de mercado pago
        ///agrega a la preferencia cada uno de los items del carrito
        List<PreferenceItemRequest> mpItems = new ArrayList<>();
        for (DetalleCompra item : items) {
            String title = (item.getProducto() != null && item.getProducto().getNombre() != null)
                    ? item.getProducto().getNombre()
                    : "Producto Deportivo Zero";
            String id = (item.getProducto() != null && item.getProducto().getId() != null)
                    ? item.getProducto().getId()
                    : item.getId();

            double precio = item.getPrecioUnitario() > 0 ? item.getPrecioUnitario() : 1.0;
            BigDecimal unitPrice = BigDecimal.valueOf(precio).setScale(2, RoundingMode.HALF_UP);

            PreferenceItemRequest itemRequest = PreferenceItemRequest.builder()
                    .id(id)
                    .title(title)
                    .quantity(Math.max(1, item.getCantidad()))
                    .currencyId("ARS")
                    .unitPrice(unitPrice)
                    .build();

            mpItems.add(itemRequest);
        }

        ///
        String effectiveBaseUrl = (configuredBaseUrl != null && !configuredBaseUrl.isBlank())
                ? configuredBaseUrl.trim()
                : (baseUrl != null ? baseUrl.trim() : "http://localhost:8080");
        if (effectiveBaseUrl.endsWith("/")) {
            effectiveBaseUrl = effectiveBaseUrl.substring(0, effectiveBaseUrl.length() - 1);
        }

        String successUrl = effectiveBaseUrl + "/checkout/success";
        String failureUrl = effectiveBaseUrl + "/shop/cart";
        String pendingUrl = effectiveBaseUrl + "/shop/cart";

        PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                .success(successUrl)
                .failure(failureUrl)
                .pending(pendingUrl)
                .build();

        String payerName = (cliente != null && cliente.getNombre() != null) ? cliente.getNombre() : "Cliente";
        String payerSurname = (cliente != null && cliente.getApellido() != null) ? cliente.getApellido() : "Zero";
        String payerEmail = (carrito != null && carrito.getEmailUsuario() != null && !carrito.getEmailUsuario().isBlank())
                ? carrito.getEmailUsuario().trim()
                : "cliente@zero.com";

        PreferencePayerRequest payer = PreferencePayerRequest.builder()
                .name(payerName)
                .surname(payerSurname)
                .email(payerEmail)
                .build();

        PreferenceRequest.PreferenceRequestBuilder requestBuilder = PreferenceRequest.builder()
                .items(mpItems)
                .payer(payer)
                .backUrls(backUrls)
                .externalReference(carrito.getId());


        PreferenceClient client = new PreferenceClient();
        Preference preference = client.create(requestBuilder.build());

        String redirectUrl = preference.getInitPoint();
        if (redirectUrl == null || redirectUrl.isBlank()) {
            redirectUrl = preference.getSandboxInitPoint();
        }

        return redirectUrl;
    }

    ///procesa la confirmación de pago exitoso recibida desde Mercado Pago:
    ///registra la venta en el sistema, descuenta la orden de compra pasándola
    ///al estado pendiente_envio y genera la factura.
    @Transactional
    public Factura procesarPagoExitoso(String externalReference, String paymentId, Cliente cliente) {
        OrdenCompra orden = null;

        if (externalReference != null && !externalReference.isBlank()) {
            orden = ordenCompraRepository.findById(externalReference.trim()).orElse(null);
        }

        if (orden == null && cliente != null) {
            orden = ordenCompraRepository.findByClienteAndEstadoOrdenCompraAndEliminadoFalse(
                    cliente, EstadoOrdenCompra.PENDIENTE_COMPLETAR).orElse(null);
        }

        if (orden == null) {
            // Si la orden ya no se encuentra o ya fue procesada, buscar la última factura del cliente
            if (cliente != null && cliente.getNumeroDocumento() != null) {
                List<Factura> facturas = facturaRepository.findByEliminadoFalseOrderByFechaFacturaDesc();
                for (Factura f : facturas) {
                    if (f instanceof com.example.zero.entidades.compraCliente.FacturaCliente fc && fc.getOrdenCompra() != null && fc.getOrdenCompra().getCliente() != null) {
                        if (cliente.getNumeroDocumento().equals(fc.getOrdenCompra().getCliente().getNumeroDocumento())) {
                            return f;
                        }
                    }
                }
            }
            throw new IllegalStateException("No se encontró la orden de compra activa asociada al pago.");
        }

        // si la orden ya fue completada, no duplicar la factura
        if (orden.getEstadoOrdenCompra() != EstadoOrdenCompra.PENDIENTE_COMPLETAR) {
            List<Factura> facturas = facturaRepository.findByEliminadoFalseOrderByFechaFacturaDesc();
            if (orden.getCliente() != null && orden.getCliente().getNumeroDocumento() != null) {
                for (Factura f : facturas) {
                    if (f instanceof com.example.zero.entidades.compraCliente.FacturaCliente fc && fc.getOrdenCompra() != null && fc.getOrdenCompra().getCliente() != null) {
                        if (orden.getCliente().getNumeroDocumento().equals(fc.getOrdenCompra().getCliente().getNumeroDocumento())) {
                            return f;
                        }
                    }
                }
            }
            if (!facturas.isEmpty()) {
                return facturas.get(0);
            }
        }

        Cliente clienteOrden = orden.getCliente() != null ? orden.getCliente() : cliente;
        if (clienteOrden == null) {
            throw new IllegalStateException("La orden de compra no tiene un cliente asociado.");
        }

        List<DetalleCompra> items = ordenCompraService.obtenerItemsActivos(orden);
        if (items.isEmpty()) {
            throw new IllegalStateException("La orden de compra no contiene ítems activos.");
        }

        List<String> prodIds = new ArrayList<>();
        List<Integer> cantidades = new ArrayList<>();
        for (DetalleCompra d : items) {
            if (d.getProducto() != null && d.getCantidad() > 0) {
                prodIds.add(d.getProducto().getId());
                cantidades.add(d.getCantidad());
            }
        }

        String dni = (clienteOrden.getNumeroDocumento() != null && !clienteOrden.getNumeroDocumento().isBlank())
                ? clienteOrden.getNumeroDocumento() : "00000000";
        String nombre = (clienteOrden.getNombre() != null && !clienteOrden.getNombre().isBlank())
                ? clienteOrden.getNombre() : "Cliente";
        String apellido = (clienteOrden.getApellido() != null && !clienteOrden.getApellido().isBlank())
                ? clienteOrden.getApellido() : "Zero";
        String email = (orden.getEmailUsuario() != null && !orden.getEmailUsuario().isBlank())
                ? orden.getEmailUsuario().trim()
                : "";
        if (email.isBlank() && clienteOrden.getContactos() != null) {
            for (var c : clienteOrden.getContactos()) {
                if (c instanceof com.example.zero.entidades.empresa.ContactoCorreoElectronico ce && !ce.isEliminado() && ce.getEmail() != null && ce.getEmail().contains("@")) {
                    email = ce.getEmail().trim();
                    break;
                }
            }
        }

        // registra venta con medio de pago BILLETERA_VIRTUAL
        Factura factura = ventaService.registrarVenta(dni, nombre, apellido, email, "BILLETERA_VIRTUAL", prodIds, cantidades);

        // actualizar estado de la orden de compra a PENDIENTE_ENVIO
        orden.setEstadoOrdenCompra(EstadoOrdenCompra.PENDIENTE_ENVIO);
        ordenCompraRepository.save(orden);

        return factura;
    }
}
