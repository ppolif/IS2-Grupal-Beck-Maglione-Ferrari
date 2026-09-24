package com.example.zero.services.mail;

import com.example.zero.entidades.persona.Usuario;
import com.example.zero.entidades.producto.Producto;
import com.example.zero.repositories.ProductoRepository;
import com.example.zero.repositories.UsuarioRepository;
import com.example.zero.services.producto.ProductoService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NewsletterService {

    private static final Logger logger = LoggerFactory.getLogger(NewsletterService.class);

    private final JavaMailSender mailSender;
    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;
    private final ProductoService productoService;

    @Value("${spring.mail.username:zeroshopclothes@gmail.com}")
    private String remitente;

    /**
     * Tarea programada cada 10 días a las 09:00 hs mediante notación cron.
     * Formato cron: segundo minuto hora día-del-mes mes día-de-la-semana
     */
    @Scheduled(cron = "${newsletter.cron:0 0 9 */10 * *}")
    public void enviarNewsletterProgramado() {
        logger.info("Iniciando tarea programada: Envío de newsletter de ofertas cada 10 días.");
        int enviados = enviarNewsletterOfertas();
        logger.info("Tarea de newsletter finalizada. Total de correos enviados: {}", enviados);
    }

    /**
     * Envía el newsletter con el catálogo HTML embebido de ofertas a todos los usuarios activos.
     *
     * @return Cantidad de correos despachados satisfactoriamente
     */
    public int enviarNewsletterOfertas() {
        List<Producto> ofertas = productoRepository.findByEnOfertaTrueAndEliminadoFalse();
        if (ofertas.isEmpty()) {
            logger.info("No hay productos en oferta actualmente. Se cancela el envío del newsletter.");
            return 0;
        }

        productoService.prepararParaVista(ofertas);

        List<Usuario> usuarios = usuarioRepository.findAll().stream()
                .filter(u -> !u.isEliminado() && u.getNombreUsuario() != null && u.getNombreUsuario().contains("@"))
                .toList();

        if (usuarios.isEmpty()) {
            logger.info("No hay usuarios destinatarios registrados para recibir el newsletter.");
            return 0;
        }

        String contenidoHtml = construirHtmlNewsletter(ofertas);
        int enviados = 0;

        for (Usuario usuario : usuarios) {
            try {
                enviarCorreoHtml(usuario.getNombreUsuario().trim(), "🔥 ¡Nuevas Ofertas Imperdibles en ZERO!", contenidoHtml);
                enviados++;
            } catch (Exception e) {
                logger.error("Error al enviar newsletter a {}: {}", usuario.getNombreUsuario(), e.getMessage());
            }
        }

        return enviados;
    }

    private void enviarCorreoHtml(String destinatario, String asunto, String htmlBody) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(remitente != null && !remitente.isBlank() ? remitente : "zeroshopclothes@gmail.com");
        helper.setTo(destinatario);
        helper.setSubject(asunto);
        helper.setText(htmlBody, true);

        mailSender.send(message);
    }

    public String construirHtmlNewsletter(List<Producto> ofertas) {
        StringBuilder tarjetas = new StringBuilder();

        for (Producto prod : ofertas) {
            String nombre = prod.getNombre() != null ? prod.getNombre() : "Producto Deportivo ZERO";
            String precio = String.format("$%.2f", prod.getPrecioActual() != null ? prod.getPrecioActual() : 0.0);
            String desc = prod.getDescripcion() != null ? prod.getDescripcion() : "Máxima calidad y rendimiento garantizado.";
            String img = "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=400";

            tarjetas.append(String.format("""
                <div style="background: #ffffff; border: 1px solid #eeeeee; border-radius: 8px; margin-bottom: 20px; padding: 16px; display: flex; align-items: center; box-shadow: 0 2px 4px rgba(0,0,0,0.05);">
                    <img src="%s" alt="%s" style="width: 100px; height: 100px; object-fit: cover; border-radius: 6px; margin-right: 18px;" />
                    <div style="flex-grow: 1;">
                        <span style="background-color: #ffba00; color: #141212; font-size: 11px; font-weight: bold; padding: 2px 8px; border-radius: 4px; text-transform: uppercase;">OFERTA DESTACADA</span>
                        <h3 style="margin: 6px 0 4px 0; color: #141212; font-size: 16px; font-weight: 700;">%s</h3>
                        <p style="margin: 0 0 8px 0; color: #666666; font-size: 13px; line-height: 1.4;">%s</p>
                        <span style="font-size: 18px; font-weight: 800; color: #d9534f;">%s</span>
                    </div>
                </div>
            """, img, nombre, nombre, desc, precio));
        }

        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
            </head>
            <body style="font-family: 'Helvetica Neue', Arial, sans-serif; background-color: #f7f7f7; margin: 0; padding: 20px;">
                <div style="max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 8px; overflow: hidden; border: 1px solid #e0e0e0;">
                    <!-- Cabecera -->
                    <div style="background-color: #141212; padding: 25px; text-align: center;">
                        <h1 style="color: #ffba00; margin: 0; font-size: 28px; font-weight: 800; letter-spacing: 2px; font-style: italic;">ZERO</h1>
                        <p style="color: #ffffff; margin: 5px 0 0 0; font-size: 14px;">Indumentaria y Calzado Deportivo de Alta Performance</p>
                    </div>

                    <!-- Mensaje Introductorio -->
                    <div style="padding: 25px 25px 10px 25px; text-align: center;">
                        <h2 style="color: #141212; margin-top: 0;">¡Promociones Exclusivas de la Semana!</h2>
                        <p style="color: #555555; font-size: 14px; line-height: 1.5;">
                            Te presentamos los productos en oferta que no te puedes perder. Equípate con lo mejor al mejor precio.
                        </p>
                    </div>

                    <!-- Lista de Productos en Oferta -->
                    <div style="padding: 0 25px 15px 25px;">
                        %s
                    </div>

                    <!-- Botón CTA -->
                    <div style="text-align: center; padding: 15px 25px 30px 25px;">
                        <a href="http://localhost:8080/offers" style="background-color: #ffba00; color: #141212; text-decoration: none; padding: 12px 28px; font-size: 15px; font-weight: bold; border-radius: 5px; display: inline-block;">
                            Explorar Catálogo de Ofertas
                        </a>
                    </div>

                    <!-- Footer -->
                    <div style="background-color: #f1f1f1; padding: 15px; text-align: center; font-size: 12px; color: #888888; border-top: 1px solid #eeeeee;">
                        &copy; 2026 ZERO Clothes. Todos los derechos reservados.<br>
                        Recibes este correo porque eres un usuario registrado en nuestra plataforma.
                    </div>
                </div>
            </body>
            </html>
        """, tarjetas.toString());
    }
}
