package com.DulceMayor.backend.services.mail;

import com.DulceMayor.backend.entities.contacto.Contacto;
import com.DulceMayor.backend.entities.contacto.ContactoCorreoElectronico;
import com.DulceMayor.backend.entities.mayorista.ConfiguracionCorreoMayorista;
import com.DulceMayor.backend.entities.persona.Minorista;
import com.DulceMayor.backend.repositories.mayorista.ConfiguracionCorreoMayoristaRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Servicio centralizado para el despacho de correos electrónicos.
 *
 * <p><b>Características principales:</b></p>
 * <ul>
 *   <li><b>Resolución dinámica de transporte:</b> Intenta obtener la configuración SMTP
 *       desde la base de datos ({@link ConfiguracionCorreoMayorista}). Si no existe, recurre
 *       al {@link JavaMailSender} por defecto configurado con las propiedades de entorno/properties.</li>
 *   <li><b>Soporte de HTML y archivos adjuntos:</b> Permite enviar correos simples o enriquecidos con PDFs
 *       adjuntos (como comprobantes de facturación).</li>
 *   <li><b>Tolerancia a fallos:</b> Captura errores de red/autenticación y registra advertencias sin interrumpir
 *       el flujo de las transacciones operativas.</li>
 * </ul>
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender defaultMailSender;
    private final ConfiguracionCorreoMayoristaRepository configRepository;

    @Value("${spring.mail.username:}")
    private String defaultFromEmail;

    public EmailService(
            JavaMailSender defaultMailSender,
            ConfiguracionCorreoMayoristaRepository configRepository) {
        this.defaultMailSender = defaultMailSender;
        this.configRepository = configRepository;
    }

    /**
     * Resuelve la dirección de correo electrónico preferida de un minorista según la regla de prioridad del sistema:
     * <ol>
     *   <li><b>Prioridad 1:</b> Correo de su cuenta de usuario (login).</li>
     *   <li><b>Prioridad 2 (Respaldo):</b> Primer correo registrado en su lista de contactos secundarios.</li>
     * </ol>
     *
     * @param minorista Entidad del cliente.
     * @return Dirección de correo electrónico válida o {@code null} si no posee ninguna.
     */
    public String resolverEmailMinorista(Minorista minorista) {
        if (minorista == null) {
            return null;
        }

        // 1. Prioridad: correo de la cuenta de usuario
        if (minorista.getUsuario() != null && minorista.getUsuario().getNombreUsuario() != null) {
            String username = minorista.getUsuario().getNombreUsuario().trim();
            if (username.contains("@")) {
                return username;
            }
        }

        // 2. Respaldo: contactos secundarios cargados
        if (minorista.getContactos() != null) {
            for (Contacto c : minorista.getContactos()) {
                if (!c.isEliminado() && c instanceof ContactoCorreoElectronico correo && correo.getEmail() != null) {
                    String mail = correo.getEmail().trim();
                    if (!mail.isBlank()) {
                        return mail;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Envía un correo electrónico simple con cuerpo en formato HTML.
     *
     * @param destinatario Correo electrónico de destino.
     * @param asunto Asunto del correo.
     * @param cuerpoHtml Contenido en formato HTML.
     */
    public void enviarCorreoSimple(String destinatario, String asunto, String cuerpoHtml) {
        enviarCorreo(destinatario, asunto, cuerpoHtml, null, null, null);
    }

    /**
     * Envía un correo electrónico con un archivo adjunto (por ejemplo, factura PDF en memoria).
     *
     * @param destinatario Correo electrónico de destino.
     * @param asunto Asunto del correo.
     * @param cuerpoHtml Contenido en formato HTML.
     * @param adjunto Bytes del archivo a adjuntar.
     * @param nombreArchivo Nombre que tendrá el archivo adjunto (ej. factura-123.pdf).
     * @param mimeType Tipo MIME del adjunto (ej. application/pdf).
     */
    public void enviarCorreoConAdjunto(
            String destinatario,
            String asunto,
            String cuerpoHtml,
            byte[] adjunto,
            String nombreArchivo,
            String mimeType) {
        enviarCorreo(destinatario, asunto, cuerpoHtml, adjunto, nombreArchivo, mimeType);
    }

    /**
     * Orquesta la preparación del mensaje MIME, el remitente y el despacho efectivo.
     */
    private void enviarCorreo(
            String destinatario,
            String asunto,
            String cuerpoHtml,
            byte[] adjunto,
            String nombreArchivo,
            String mimeType) {

        if (destinatario == null || destinatario.isBlank()) {
            log.warn("Omisión de envío de correo: El destinatario es nulo o vacío (Asunto: '{}')", asunto);
            return;
        }

        try {
            JavaMailSender mailSender = resolverMailSender();
            String remitente = resolverRemitente();

            MimeMessage message = mailSender.createMimeMessage();
            boolean tieneAdjunto = adjunto != null && adjunto.length > 0;
            MimeMessageHelper helper = new MimeMessageHelper(message, tieneAdjunto, "UTF-8");

            helper.setTo(destinatario.trim());
            helper.setSubject(asunto);
            helper.setText(cuerpoHtml, true); // true indica contenido HTML

            if (remitente != null && !remitente.isBlank()) {
                helper.setFrom(remitente.trim(), "Dulce Mayor");
            }

            if (tieneAdjunto) {
                String contentType = (mimeType != null && !mimeType.isBlank()) ? mimeType : "application/octet-stream";
                String archivo = (nombreArchivo != null && !nombreArchivo.isBlank()) ? nombreArchivo : "adjunto";
                helper.addAttachment(archivo, new ByteArrayResource(adjunto), contentType);
            }

            mailSender.send(message);
            log.info("Correo enviado exitosamente a '{}' con asunto: '{}'", destinatario, asunto);

        } catch (MessagingException e) {
            log.error("Error al construir/enviar correo a '{}' (Asunto: '{}'): {}", destinatario, asunto, e.getMessage());
        } catch (Exception e) {
            log.error("Fallo inesperado durante el envío de correo a '{}': {}", destinatario, e.getMessage(), e);
        }
    }

    /**
     * Resuelve dinámicamente el {@link JavaMailSender}. Si en la base de datos existe una configuración
     * personalizada para la empresa mayorista, la instancia dinámicamente; si no, utiliza el bean inyectado.
     */
    private JavaMailSender resolverMailSender() {
        return configRepository.findAll().stream()
                .filter(c -> !c.isEliminado() && c.getCorreo() != null && !c.getCorreo().isBlank())
                .findFirst()
                .map(this::construirMailSenderPersonalizado)
                .orElse(defaultMailSender);
    }

    /**
     * Construye una instancia de {@link JavaMailSenderImpl} en base a la entidad almacenada en la base de datos.
     */
    private JavaMailSender construirMailSenderPersonalizado(ConfiguracionCorreoMayorista config) {
        JavaMailSenderImpl impl = new JavaMailSenderImpl();
        impl.setHost(config.getSmtp());

        try {
            impl.setPort(Integer.parseInt(config.getPuerto()));
        } catch (NumberFormatException e) {
            impl.setPort(587); // Puerto por defecto seguro
        }

        impl.setUsername(config.getCorreo());
        impl.setPassword(config.getClave());

        Properties props = impl.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", String.valueOf(config.isTls()));
        props.put("mail.smtp.connectiontimeout", "5000");
        props.put("mail.smtp.timeout", "5000");
        props.put("mail.smtp.writetimeout", "5000");

        return impl;
    }

    /**
     * Retorna la dirección de correo origen del remitente.
     */
    private String resolverRemitente() {
        return configRepository.findAll().stream()
                .filter(c -> !c.isEliminado() && c.getCorreo() != null && !c.getCorreo().isBlank())
                .findFirst()
                .map(ConfiguracionCorreoMayorista::getCorreo)
                .orElse(defaultFromEmail);
    }
}
