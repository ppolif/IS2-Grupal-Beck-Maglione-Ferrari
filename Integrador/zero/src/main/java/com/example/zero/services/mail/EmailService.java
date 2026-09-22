package com.example.zero.services.mail;

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

    @Value("${spring.mail.username}")
    private String remitente;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
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
}

