package com.example.zero.services;

import com.example.zero.services.mail.EmailService;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Disabled("Ignorado según requerimiento 8.4")
@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    private EmailService emailService;

    @BeforeEach
    void setUp() {
        emailService = new EmailService(mailSender, null);
    }

    @Test
    void enviarCodigoConfirmacion_conDatosValidos_enviaMimeMessage() {
        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(any(MimeMessage.class));

        emailService.enviarCodigoConfirmacion("cliente@test.com", "123456");

        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    void enviarCodigoConfirmacion_conDestinatarioVacio_lanzaExcepcion() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> emailService.enviarCodigoConfirmacion("", "123456"));

        assertTrue(ex.getMessage().contains("La dirección de correo destinataria no puede estar vacía"));
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void enviarCodigoConfirmacion_conCodigoVacio_lanzaExcepcion() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> emailService.enviarCodigoConfirmacion("cliente@test.com", ""));

        assertTrue(ex.getMessage().contains("El código de confirmación no puede ser nulo o vacío"));
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void enviarCodigoConfirmacion_conErrorSmtp_lanzaIllegalArgumentException() {
        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new MailSendException("Fallo de conexión SMTP")).when(mailSender).send(any(MimeMessage.class));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> emailService.enviarCodigoConfirmacion("cliente@test.com", "123456"));

        assertTrue(ex.getMessage().contains("No se pudo enviar el correo de confirmación"));
    }

    @Test
    void enviarComprobanteCompra_conFacturaValida_enviaMimeMessage() {
        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(any(MimeMessage.class));

        com.example.zero.entidades.producto.Producto prod = com.example.zero.entidades.producto.Producto.builder()
                .id("prod-1")
                .nombre("Remera Running Pro")
                .build();

        com.example.zero.entidades.compra.Detalle detalle = com.example.zero.entidades.compra.Detalle.builder()
                .id("det-1")
                .producto(prod)
                .cantidad(2)
                .subtotal(5000.0)
                .eliminado(false)
                .build();

        com.example.zero.entidades.compra.Factura factura = com.example.zero.entidades.compra.Factura.builder()
                .id("fac-1")
                .numeroFactura(1050L)
                .fechaFactura(java.time.LocalDateTime.of(2026, 9, 29, 15, 30))
                .totalPagado(5000.0)
                .estado(com.example.zero.enums.EstadoFactura.PAGADA)
                .detalles(new java.util.HashSet<>(java.util.List.of(detalle)))
                .build();

        emailService.enviarComprobanteCompra(factura, "comprador@test.com");

        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    void enviarComprobanteCompra_conFacturaNula_lanzaExcepcion() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> emailService.enviarComprobanteCompra(null, "comprador@test.com"));

        assertTrue(ex.getMessage().contains("La factura de compra no puede ser nula"));
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void enviarComprobanteCompra_conDestinatarioInvalido_lanzaExcepcion() {
        com.example.zero.entidades.compra.Factura factura = com.example.zero.entidades.compra.Factura.builder()
                .numeroFactura(1001L)
                .build();

        assertThrows(IllegalArgumentException.class,
                () -> emailService.enviarComprobanteCompra(factura, ""));
        assertThrows(IllegalArgumentException.class,
                () -> emailService.enviarComprobanteCompra(factura, null));
        assertThrows(IllegalArgumentException.class,
                () -> emailService.enviarComprobanteCompra(factura, "correo-sin-arroba"));

        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void enviarComprobanteCompra_conErrorSmtp_lanzaIllegalArgumentException() {
        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new MailSendException("Fallo SMTP")).when(mailSender).send(any(MimeMessage.class));

        com.example.zero.entidades.compra.Factura factura = com.example.zero.entidades.compra.Factura.builder()
                .numeroFactura(1001L)
                .build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> emailService.enviarComprobanteCompra(factura, "cliente@test.com"));

        assertTrue(ex.getMessage().contains("No se pudo enviar el comprobante de compra"));
    }

    @Test
    void construirHtmlComprobante_generaEstructuraHtmlCompleta() {
        com.example.zero.entidades.persona.Cliente cliente = com.example.zero.entidades.persona.Cliente.builder()
                .nombre("Lionel")
                .apellido("Messi")
                .build();

        com.example.zero.entidades.producto.Producto prod = com.example.zero.entidades.producto.Producto.builder()
                .id("p10")
                .nombre("Camiseta Albiceleste 10")
                .build();

        com.example.zero.entidades.compra.Detalle det = com.example.zero.entidades.compra.Detalle.builder()
                .producto(prod)
                .cantidad(1)
                .subtotal(18500.0)
                .eliminado(false)
                .build();

        com.example.zero.entidades.compra.FormaDePago fdp = com.example.zero.entidades.compra.FormaDePago.builder()
                .tipoPago(com.example.zero.enums.TipoDePago.BILLETERA_VIRTUAL)
                .build();

        com.example.zero.entidades.compraCliente.FacturaCliente factura = com.example.zero.entidades.compraCliente.FacturaCliente.builder()
                .numeroFactura(2026L)
                .fechaFactura(java.time.LocalDateTime.of(2026, 9, 29, 14, 0))
                .ordenCompra(com.example.zero.entidades.compraCliente.OrdenCompra.builder().cliente(cliente).build())
                .formaDePago(fdp)
                .estado(com.example.zero.enums.EstadoFactura.PAGADA)
                .totalPagado(18500.0)
                .detalles(new java.util.HashSet<>(java.util.List.of(det)))
                .build();

        String html = emailService.construirHtmlComprobante(factura);

        assertTrue(html.contains("ZERO"));
        assertTrue(html.contains("#ORD-2026"));
        assertTrue(html.contains("Lionel Messi"));
        assertTrue(html.contains("Camiseta Albiceleste 10"));
        assertTrue(html.contains("$18500.00"));
        assertTrue(html.contains("PAGADA"));
        assertTrue(html.contains("zeroshopclothes@gmail.com"));
    }

    @Test
    void construirHtmlComprobante_conFacturaNula_retornaVacio() {
        String html = emailService.construirHtmlComprobante(null);
        assertTrue(html.isEmpty());
    }
}

