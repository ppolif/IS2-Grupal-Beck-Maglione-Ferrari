package com.example.persona.services;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:biblioteca.facultad@gmail.com}")
    private String remitente;

    @Value("${app.facultad.url:https://ingenieria.uncuyo.edu.ar/}")
    private String urlFacultad;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void enviarAvisoVencimientoLibro(String destinatario, String nombrePersona, String tituloLibro, LocalDate fechaVencimiento) {
        if (destinatario == null || destinatario.isBlank()) {
            log.warn("No se puede enviar aviso de vencimiento: destinatario nulo o vacío para {}", nombrePersona);
            return;
        }

        String asunto = "Recordatorio: Mañana vence la devolución de tu libro - Sistema de Biblioteca";
        String fechaFormateada = fechaVencimiento != null ? fechaVencimiento.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "Mañana";
        String contenidoHtml = construirHtmlVencimiento(nombrePersona, tituloLibro, fechaFormateada);

        enviarCorreoHtml(destinatario, asunto, contenidoHtml);
    }

    @Override
    public void enviarSaludoCumpleanios(String destinatario, String nombrePersona) {
        if (destinatario == null || destinatario.isBlank()) {
            log.warn("No se puede enviar saludo de cumpleaños: destinatario nulo o vacío para {}", nombrePersona);
            return;
        }

        String asunto = "¡Feliz Cumpleaños " + nombrePersona + "! - Facultad de Ingeniería UNCUYO";
        String contenidoHtml = construirHtmlCumpleanios(nombrePersona);

        enviarCorreoHtml(destinatario, asunto, contenidoHtml);
    }

    private void enviarCorreoHtml(String destinatario, String asunto, String cuerpoHtml) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(remitente);
            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setText(cuerpoHtml, true);

            mailSender.send(mimeMessage);
            log.info("Correo electrónico enviado con éxito a [{}] | Asunto: '{}'", destinatario, asunto);
        } catch (Exception e) {
            log.error("Fallo al enviar correo a [{}] | Asunto: '{}' | Motivo: {}", destinatario, asunto, e.getMessage());
        }
    }

    private String construirHtmlVencimiento(String nombrePersona, String tituloLibro, String fechaVencimiento) {
        String plantilla = """
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f8f9fa; margin: 0; padding: 20px; color: #333; }
                    .card { max-width: 600px; margin: 0 auto; background: #ffffff; border-radius: 10px; overflow: hidden; border: 1px solid #e2e8f0; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1); }
                    .header { background: linear-gradient(135deg, #1e3a8a 0%, #3b82f6 100%); color: #ffffff; padding: 24px; text-align: center; }
                    .header h1 { margin: 0; font-size: 22px; font-weight: 700; letter-spacing: 0.5px; }
                    .content { padding: 30px 24px; }
                    .alert-box { background-color: #fffbeb; border-left: 4px solid #f59e0b; padding: 16px; margin: 20px 0; border-radius: 4px; }
                    .book-card { background-color: #f1f5f9; border-radius: 8px; padding: 16px 20px; margin: 20px 0; }
                    .footer { background-color: #f8fafc; padding: 20px; text-align: center; font-size: 13px; color: #64748b; border-top: 1px solid #e2e8f0; }
                </style>
            </head>
            <body>
                <div class="card">
                    <div class="header">
                        <h1>Sistema de Gestión de Biblioteca</h1>
                        <p style="margin: 5px 0 0 0; font-size: 14px; opacity: 0.9;">Aviso Automático de Préstamo</p>
                    </div>
                    <div class="content">
                        <h2 style="color: #1e293b; font-size: 20px; margin-top: 0;">Estimado/a {{NOMBRE}},</h2>
                        <div class="alert-box">
                            <strong style="color: #b45309;">Recordatorio importante:</strong>
                            <p style="margin: 6px 0 0 0; color: #92400e;">Mañana vence el plazo establecido para la devolución del siguiente libro prestado.</p>
                        </div>
                        <div class="book-card">
                            <p style="margin: 0 0 8px 0; font-size: 13px; text-transform: uppercase; letter-spacing: 1px; color: #64748b; font-weight: bold;">Detalles del Libro</p>
                            <h3 style="margin: 0 0 8px 0; color: #0f172a; font-size: 18px;">{{TITULO}}</h3>
                            <p style="margin: 0; color: #334155; font-size: 14px;"><strong>Fecha Límite de Devolución:</strong> <span style="color: #dc2626; font-weight: bold;">{{FECHA}}</span></p>
                        </div>
                        <p style="color: #475569; font-size: 14px; line-height: 1.6;">
                            Te solicitamos acercarte a las instalaciones de la biblioteca para realizar la entrega del ejemplar o gestionar la renovación correspondiente en caso de estar disponible.
                        </p>
                    </div>
                    <div class="footer">
                        Facultad de Ingeniería &bull; Universidad Nacional de Cuyo<br>
                        Sistema Automatizado de Notificaciones
                    </div>
                </div>
            </body>
            </html>
            """;

        return plantilla
                .replace("{{NOMBRE}}", escape(nombrePersona))
                .replace("{{TITULO}}", escape(tituloLibro))
                .replace("{{FECHA}}", escape(fechaVencimiento));
    }

    private String construirHtmlCumpleanios(String nombrePersona) {
        String plantilla = """
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f1f5f9; margin: 0; padding: 25px 10px; color: #1e293b; }
                    .card { max-width: 600px; margin: 0 auto; background: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.1); border: 1px solid #e2e8f0; }
                    .header { background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 50%, #db2777 100%); color: #ffffff; padding: 36px 20px; text-align: center; }
                    .header h1 { margin: 0; font-size: 28px; font-weight: 800; }
                    .header p { margin: 8px 0 0 0; font-size: 15px; opacity: 0.95; }
                    .content { padding: 32px 28px; text-align: center; }
                    .greeting-text { font-size: 16px; line-height: 1.7; color: #334155; margin-bottom: 28px; }
                    .btn-facultad { background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%); color: #ffffff !important; text-decoration: none; padding: 14px 32px; font-size: 16px; font-weight: 700; border-radius: 8px; display: inline-block; box-shadow: 0 4px 12px rgba(37, 99, 235, 0.35); }
                    .footer { background-color: #f8fafc; padding: 20px; text-align: center; font-size: 13px; color: #94a3b8; border-top: 1px solid #e2e8f0; }
                </style>
            </head>
            <body>
                <div class="card">
                    <div class="header">
                        <h1>&#127881; &iexcl;Muy Feliz Cumplea&ntilde;os! &#127874;</h1>
                        <p>Facultad de Ingenier&iacute;a &bull; UNCUYO</p>
                    </div>
                    <div class="content">
                        <h2 style="color: #0f172a; font-size: 22px; margin-top: 0;">&iexcl;Querido/a {{NOMBRE}}!</h2>
                        <p class="greeting-text">
                            En este d&iacute;a tan especial, toda la comunidad acad&eacute;mica de la Facultad de Ingenier&iacute;a te env&iacute;a sus m&aacute;s c&aacute;lidas felicitaciones y mejores deseos de felicidad, salud y &eacute;xitos en cada uno de tus proyectos personales y acad&eacute;micos.
                        </p>
                        <div style="margin: 30px 0;">
                            <a href="{{URL_FACULTAD}}" target="_blank" class="btn-facultad">
                                Visitar la P&aacute;gina de la Facultad
                            </a>
                        </div>
                        <p style="font-size: 13px; color: #64748b; margin-top: 25px;">
                            &iexcl;Esperamos que disfrutes de un d&iacute;a extraordinario rodeado/a de tus seres queridos!
                        </p>
                    </div>
                    <div class="footer">
                        Facultad de Ingenier&iacute;a &bull; Universidad Nacional de Cuyo<br>
                        Mendoza, Argentina
                    </div>
                </div>
            </body>
            </html>
            """;

        return plantilla
                .replace("{{NOMBRE}}", escape(nombrePersona))
                .replace("{{URL_FACULTAD}}", escape(urlFacultad));
    }

    private String escape(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#39;");
    }
}
