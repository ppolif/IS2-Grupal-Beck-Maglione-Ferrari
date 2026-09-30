package com.example.zero.services.mail;

import com.example.zero.services.CompraProveedorService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    private final CompraProveedorService compraProveedorService;

    @Value("${spring.mail.username}")
    private String remitente;

    public EmailService(JavaMailSender mailSender, CompraProveedorService compraProveedorService) {
        this.mailSender = mailSender;
        this.compraProveedorService = compraProveedorService;
    }

    /**     
     * Envía un correo electrónico con el código de activación de 6 dígitos con formato HTML institucional ZERO.
     *
     * @param destinatario Dirección de correo del cliente receptor
     * @param codigo       Código de 6 dígitos numéricos
     */
    public void enviarCodigoConfirmacion(String destinatario, String codigo) {
        if (destinatario == null || destinatario.trim().isEmpty()) {
            throw new IllegalArgumentException("La dirección de correo destinataria no puede estar vacía");
        }
        if (codigo == null || codigo.trim().isEmpty()) {
            throw new IllegalArgumentException("El código de confirmación no puede ser nulo o vacío");
        }

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(remitente != null && !remitente.isEmpty() ? remitente : "zeroshopclothes@gmail.com");
            helper.setTo(destinatario.trim());
            helper.setSubject("ZERO - Código de confirmación de tu cuenta");

            String contenidoHtml = String.format("""
                <div style="font-family: 'Helvetica Neue', Arial, sans-serif; max-width: 500px; margin: 0 auto; border: 1px solid #e0e0e0; border-radius: 8px; overflow: hidden; background-color: #ffffff;">
                    <div style="background-color: #141212; padding: 24px; text-align: center;">
                        <h1 style="color: #ffba00; margin: 0; font-size: 26px; font-weight: 800; letter-spacing: 2px; font-style: italic;">ZERO</h1>
                        <p style="color: #cccccc; margin: 6px 0 0 0; font-size: 13px;">Ropa Deportiva Unisex</p>
                    </div>
                    <div style="padding: 28px 24px; color: #222222; line-height: 1.6;">
                        <h2 style="font-size: 18px; color: #141212; margin-top: 0; font-weight: 700;">¡Bienvenido/a a ZERO!</h2>
                        <p style="font-size: 14px; margin-bottom: 20px;">
                            Gracias por registrarte en nuestra tienda. Para completar la activación de tu cuenta de cliente y comenzar a comprar, ingresa el siguiente código de confirmación en la plataforma:
                        </p>
                        <div style="text-align: center; margin: 28px 0;">
                            <span style="display: inline-block; font-size: 32px; font-weight: 800; letter-spacing: 8px; color: #141212; background-color: #f7f7f7; padding: 14px 28px; border-radius: 8px; border: 2px dashed #ffba00;">
                                %s
                            </span>
                        </div>
                        <p style="font-size: 13px; color: #666666; margin-top: 24px;">
                            Este código es de un solo uso y expirará en <strong>15 minutos</strong>. Si tú no realizaste este registro, por favor desestima este mensaje.
                        </p>
                    </div>
                    <div style="background-color: #f9f9f9; padding: 14px; text-align: center; font-size: 12px; color: #888888; border-top: 1px solid #eeeeee;">
                        &copy; 2026 ZERO Ropa Deportiva. Todos los derechos reservados.
                    </div>
                </div>
                """, codigo.trim());

            helper.setText(contenidoHtml, true);

            mailSender.send(mimeMessage);
            logger.info("Correo de confirmación enviado exitosamente a {}", destinatario);

        } catch (MessagingException | MailException e) {
            logger.error("Fallo al enviar correo de confirmación a {}: {}", destinatario, e.getMessage(), e);
            throw new IllegalArgumentException("No se pudo enviar el correo de confirmación a " + destinatario +
                    ". Por favor, verifica tu conexión o las credenciales SMTP del sistema: " + e.getMessage(), e);
        }   
    }

    /**
     * Envía un correo electrónico con el detalle y comprobante de compra con diseño HTML embebido institucional ZERO.
     *
     * @param factura      Entidad Factura con el detalle de la venta realizada
     * @param destinatario Dirección de correo del cliente receptor
     */
    @org.springframework.scheduling.annotation.Async
    public void enviarComprobanteCompra(com.example.zero.entidades.compra.Factura factura, String destinatario) {
        if (factura == null) {
            throw new IllegalArgumentException("La factura de compra no puede ser nula");
        }
        if (destinatario == null || destinatario.trim().isEmpty() || !destinatario.contains("@")) {
            throw new IllegalArgumentException("La dirección de correo destinataria no es válida: " + destinatario);
        }

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(remitente != null && !remitente.isEmpty() ? remitente : "zeroshopclothes@gmail.com");
            helper.setTo(destinatario.trim());
            helper.setSubject("ZERO - Confirmación de tu compra " + (factura.getOrderNumber() != null ? factura.getOrderNumber() : ""));

            String contenidoHtml = construirHtmlComprobante(factura);
            helper.setText(contenidoHtml, true);

            mailSender.send(mimeMessage);
            logger.info("Comprobante de compra enviado exitosamente a {} para la orden {}", destinatario, factura.getOrderNumber());

        } catch (MessagingException | MailException e) {
            logger.error("Fallo al enviar comprobante de compra a {}: {}", destinatario, e.getMessage(), e);
            throw new IllegalArgumentException("No se pudo enviar el comprobante de compra a " + destinatario +
                    ". Error: " + e.getMessage(), e);
        }
    }

    /**
     * Construye la plantilla HTML embebida con los detalles de la venta, productos, totales y diseño ZERO.
     *
     * @param factura Factura a representar
     * @return Código HTML con estilos CSS inline
     */
    public String construirHtmlComprobante(com.example.zero.entidades.compra.Factura factura) {
        if (factura == null) {
            return "";
        }

        String orderNumber = factura.getOrderNumber() != null ? factura.getOrderNumber() : "#ORD-S/N";
        String customerName = factura.getCustomerName() != null ? factura.getCustomerName() : "Cliente Estimado/a";
        String paymentMethod = factura.getPaymentMethod() != null ? factura.getPaymentMethod() : "Medio de Pago Electrónico";
        String status = factura.getStatus() != null ? factura.getStatus() : "PAGADA";
        
        java.time.LocalDateTime fechaHora = factura.getFechaFactura() != null ? factura.getFechaFactura() : java.time.LocalDateTime.now();
        String fechaStr = fechaHora.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        String shippingAddress = factura.getShippingAddress();
        String shippingCity = factura.getShippingCity();
        String shippingZip = factura.getShippingZip();
        StringBuilder addressBuilder = new StringBuilder();
        if (shippingAddress != null && !shippingAddress.isBlank()) {
            addressBuilder.append(shippingAddress.trim());
            if (shippingCity != null && !shippingCity.isBlank()) {
                addressBuilder.append(", ").append(shippingCity.trim());
            }
            if (shippingZip != null && !shippingZip.isBlank()) {
                addressBuilder.append(" (CP: ").append(shippingZip.trim()).append(")");
            }
        }
        String direccionEnvio = addressBuilder.toString();

        StringBuilder filasProductos = new StringBuilder();
        if (factura.getDetalles() != null && !factura.getDetalles().isEmpty()) {
            for (com.example.zero.entidades.compra.Detalle detalle : factura.getDetalles()) {
                if (detalle != null && !detalle.isEliminado()) {
                    String nombreProd = (detalle.getProducto() != null && detalle.getProducto().getNombre() != null)
                            ? detalle.getProducto().getNombre()
                            : "Producto Deportivo";
                    int cantidad = detalle.getCantidad();
                    double precioUnitario = compraProveedorService.calcularCostoUnitario(detalle);
                    double subtotalItem = detalle.getSubtotal();

                    filasProductos.append(String.format(java.util.Locale.US, """
                        <tr style="border-bottom: 1px solid #eeeeee;">
                            <td style="padding: 12px 14px; font-weight: 600; color: #141212;">%s</td>
                            <td style="padding: 12px 14px; text-align: center; color: #555555; font-weight: bold;">x %d</td>
                            <td style="padding: 12px 14px; text-align: right; color: #555555;">$%.2f</td>
                            <td style="padding: 12px 14px; text-align: right; font-weight: 700; color: #141212;">$%.2f</td>
                        </tr>
                    """, escapeHtml(nombreProd), cantidad, precioUnitario, subtotalItem));
                }
            }
        } else {
            filasProductos.append("""
                <tr>
                    <td colspan="4" style="padding: 16px; text-align: center; color: #888888; font-style: italic;">
                        Sin desglose de productos registrado.
                    </td>
                </tr>
            """);
        }

        String subtotalStr = String.format(java.util.Locale.US, "$%.2f", factura.getSubtotal());
        String totalStr = String.format(java.util.Locale.US, "$%.2f", factura.getTotalAmount());

        String bloqueDireccion = "";
        if (!direccionEnvio.isBlank()) {
            bloqueDireccion = String.format("""
                <div style="margin-top: 10px; font-size: 13px; color: #555555;">
                    <strong style="color: #141212;">Dirección de Entrega:</strong> %s
                </div>
            """, escapeHtml(direccionEnvio));
        }

        return String.format(java.util.Locale.US, """
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
            </head>
            <body style="font-family: 'Helvetica Neue', Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 24px 10px;">
                <div style="max-width: 650px; margin: 0 auto; background-color: #ffffff; border-radius: 8px; overflow: hidden; border: 1px solid #e0e0e0; box-shadow: 0 4px 12px rgba(0,0,0,0.06);">
                    <!-- Cabecera Institucional ZERO -->
                    <div style="background-color: #141212; padding: 26px 20px; text-align: center;">
                        <h1 style="color: #ffba00; margin: 0; font-size: 28px; font-weight: 800; letter-spacing: 2px; font-style: italic;">ZERO</h1>
                        <p style="color: #cccccc; margin: 6px 0 0 0; font-size: 13px;">Ropa Deportiva Unisex</p>
                    </div>

                    <!-- Franja de Éxito -->
                    <div style="background-color: #28a745; color: #ffffff; padding: 12px 20px; text-align: center; font-size: 14px; font-weight: 700; letter-spacing: 0.5px;">
                        ✓ ¡Tu compra ha sido procesada y confirmada con éxito!
                    </div>

                    <div style="padding: 28px 24px; color: #222222; line-height: 1.6;">
                        <!-- Saludo -->
                        <h2 style="font-size: 20px; color: #141212; margin-top: 0; font-weight: 700;">¡Hola, %s!</h2>
                        <p style="font-size: 14px; color: #555555; margin-bottom: 22px;">
                            Muchas gracias por tu compra en <strong>ZERO</strong>. A continuación te presentamos el resumen y comprobante detallado de tu orden:
                        </p>

                        <!-- Tarjeta de Metadatos de la Orden -->
                        <div style="background-color: #f9f9f9; border: 1px solid #e9ecef; border-radius: 8px; padding: 18px 20px; margin-bottom: 24px;">
                            <table style="width: 100%%; border-collapse: collapse; font-size: 13px;">
                                <tr>
                                    <td style="padding: 4px 0; color: #666666; width: 40%%;"><strong>Número de Orden:</strong></td>
                                    <td style="padding: 4px 0; color: #141212; font-weight: 700; font-size: 14px;">%s</td>
                                </tr>
                                <tr>
                                    <td style="padding: 4px 0; color: #666666;"><strong>Fecha y Hora:</strong></td>
                                    <td style="padding: 4px 0; color: #333333;">%s hs</td>
                                </tr>
                                <tr>
                                    <td style="padding: 4px 0; color: #666666;"><strong>Medio de Pago:</strong></td>
                                    <td style="padding: 4px 0; color: #333333;">%s</td>
                                </tr>
                                <tr>
                                    <td style="padding: 4px 0; color: #666666;"><strong>Estado del Pago:</strong></td>
                                    <td style="padding: 4px 0;">
                                        <span style="background-color: #d4edda; color: #155724; padding: 2px 8px; border-radius: 4px; font-size: 11px; font-weight: 800; text-transform: uppercase;">
                                            %s
                                        </span>
                                    </td>
                                </tr>
                            </table>
                            %s
                        </div>

                        <!-- Tabla Detallada de Productos -->
                        <h3 style="font-size: 16px; color: #141212; margin: 0 0 12px 0; font-weight: 700; border-bottom: 2px solid #ffba00; padding-bottom: 6px;">
                            Detalle de los Productos
                        </h3>

                        <table style="width: 100%%; border-collapse: collapse; font-size: 14px; margin-bottom: 24px; border: 1px solid #eeeeee;">
                            <thead>
                                <tr style="background-color: #141212; color: #ffffff;">
                                    <th style="padding: 10px 12px; text-align: left; font-size: 13px;">Producto</th>
                                    <th style="padding: 10px 12px; text-align: center; font-size: 13px;">Cant.</th>
                                    <th style="padding: 10px 12px; text-align: right; font-size: 13px;">Precio Unitario</th>
                                    <th style="padding: 10px 12px; text-align: right; font-size: 13px;">Subtotal</th>
                                </tr>
                            </thead>
                            <tbody>
                                %s
                                <!-- Subtotal y Total -->
                                <tr style="background-color: #fafafa; border-top: 2px solid #e0e0e0;">
                                    <td colspan="3" style="padding: 10px 12px; text-align: right; font-weight: 600; color: #666666;">Subtotal:</td>
                                    <td style="padding: 10px 12px; text-align: right; font-weight: 600; color: #333333;">%s</td>
                                </tr>
                                <tr style="background-color: #141212; color: #ffffff;">
                                    <td colspan="3" style="padding: 12px 14px; text-align: right; font-size: 15px; font-weight: 800; letter-spacing: 0.5px;">TOTAL ABONADO:</td>
                                    <td style="padding: 12px 14px; text-align: right; font-size: 18px; font-weight: 800; color: #ffba00;">%s</td>
                                </tr>
                            </tbody>
                        </table>

                        <!-- Botón CTA -->
                        <div style="text-align: center; margin: 30px 0 16px 0;">
                            <a href="http://localhost:8080/shop" style="background-color: #ffba00; color: #141212; text-decoration: none; padding: 12px 30px; font-size: 14px; font-weight: 800; border-radius: 6px; display: inline-block; letter-spacing: 1px;">
                                VISITAR TIENDA ZERO
                            </a>
                        </div>

                        <p style="font-size: 12px; color: #777777; text-align: center; margin-top: 18px; line-height: 1.5;">
                            ¿Tienes alguna duda o consulta sobre tu compra? Escríbenos a 
                            <a href="mailto:zeroshopclothes@gmail.com" style="color: #ffba00; text-decoration: underline; font-weight: bold;">zeroshopclothes@gmail.com</a>.
                        </p>
                    </div>

                    <!-- Pie Institucional -->
                    <div style="background-color: #f1f1f1; padding: 16px; text-align: center; font-size: 12px; color: #888888; border-top: 1px solid #eeeeee;">
                        &copy; 2026 ZERO Ropa Deportiva. Todos los derechos reservados.<br>
                        Este es un correo automático de confirmación de transacción comercial.
                    </div>
                </div>
            </body>
            </html>
        """, escapeHtml(customerName), escapeHtml(orderNumber), fechaStr, escapeHtml(paymentMethod),
             escapeHtml(status), bloqueDireccion, filasProductos.toString(), subtotalStr, totalStr);
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
}

