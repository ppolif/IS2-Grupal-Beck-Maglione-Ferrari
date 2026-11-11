package com.DulceMayor.backend.services.notificacion;

import com.DulceMayor.backend.dtos.notificacion.NewsletterEnvioResponse;
import com.DulceMayor.backend.entities.persona.Minorista;
import com.DulceMayor.backend.entities.producto.Producto;
import com.DulceMayor.backend.repositories.persona.MinoristaRepository;
import com.DulceMayor.backend.repositories.producto.ProductoRepository;
import com.DulceMayor.backend.services.mail.EmailService;
import com.DulceMayor.backend.services.mail.EmailTemplateHelper;
import com.DulceMayor.backend.services.mail.EmailTemplateHelper.ItemOfertaNewsletter;
import com.DulceMayor.backend.services.producto.VigenciaPrecioService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio encargado de la gestión y distribución del Newsletter periódico de ofertas para clientes minoristas.
 *
 * <p><b>Regla Crítica de Negocio:</b> {@code 02-reglas-negocio.md} (§8)</p>
 * <ul>
 *   <li><b>Newsletter Mensual:</b> Se ejecuta de manera programada con la anotación
 *       {@code @Scheduled(cron = "0 0 8 5 * ?")} (Día 5 de cada mes a las 08:00 AM).</li>
 *   <li><b>Contenido:</b> Itera sobre todos los minoristas activos con dirección de email registrada y envía
 *       un listado HTML con los productos donde {@code enOferta == true}.</li>
 * </ul>
 */
@Service
public class NewsletterService {

    private static final Logger log = LoggerFactory.getLogger(NewsletterService.class);
    private static final String ASUNTO_NEWSLETTER = "Ofertas Especiales del Mes - Dulce Mayor";

    private final MinoristaRepository minoristaRepository;
    private final ProductoRepository productoRepository;
    private final VigenciaPrecioService vigenciaPrecioService;
    private final EmailService emailService;

    public NewsletterService(
            MinoristaRepository minoristaRepository,
            ProductoRepository productoRepository,
            VigenciaPrecioService vigenciaPrecioService,
            EmailService emailService) {
        this.minoristaRepository = minoristaRepository;
        this.productoRepository = productoRepository;
        this.vigenciaPrecioService = vigenciaPrecioService;
        this.emailService = emailService;
    }

    /**
     * Tarea programada que se ejecuta automáticamente el día 5 de cada mes a las 08:00 AM.
     */
    @Transactional
    @Scheduled(cron = "${app.newsletter.cron:0 0 8 5 * ?}")
    public void ejecutarNewsletterMensualProgramado() {
        log.info("Iniciando despacho automático programado del Newsletter mensual...");
        NewsletterEnvioResponse resultado = enviarNewsletter();
        log.info("Newsletter mensual finalizado: {} productos en oferta enviados a {} clientes.",
                resultado.productosEnOferta(), resultado.destinatarios());
    }

    /**
     * Envía el newsletter de ofertas a todos los clientes minoristas activos que tengan email.
     * Puede invocarse de manera programada o manualmente a través del endpoint REST.
     *
     * @return {@link NewsletterEnvioResponse} con el balance de correos enviados y productos incluidos.
     */
    @Transactional
    public NewsletterEnvioResponse enviarNewsletter() {
        // 1. Obtener productos activos en oferta y preparar ítems con precio vigente
        List<Producto> productosEnOferta = productoRepository
                .findAllByEnOfertaTrueAndEliminadoFalseOrderByNombreAsc();

        List<ItemOfertaNewsletter> items = new ArrayList<>();
        for (Producto p : productosEnOferta) {
            Double precioVigente = vigenciaPrecioService.obtenerPrecioVigente(p.getId());
            String precioStr = precioVigente != null
                    ? String.format(Locale.US, "%.2f", precioVigente)
                    : "Consultar";

            items.add(new ItemOfertaNewsletter(
                    p.getNombre(),
                    p.getDescripcion(),
                    precioStr
            ));
        }

        // 2. Obtener clientes minoristas y resolver correos de sus usuarios registrados
        List<Minorista> minoristas = minoristaRepository.findAllActivosConUsuarios();
        Set<String> correosDestinatarios = new HashSet<>();

        for (Minorista m : minoristas) {
            if (m.getUsuario() != null && m.getUsuario().getNombreUsuario() != null) {
                String username = m.getUsuario().getNombreUsuario().trim();
                if (username.contains("@")) {
                    correosDestinatarios.add(username.toLowerCase());
                }
            }
        }

        log.info("Preparando envío de newsletter para {} destinatarios con {} productos en oferta.",
                correosDestinatarios.size(), items.size());

        // 3. Generar cuerpo HTML con el helper de plantillas
        String cuerpoHtml = EmailTemplateHelper.generarHtmlNewsletter(items);

        // 4. Despachar a cada cliente minorista
        int enviados = 0;
        for (String destinatario : correosDestinatarios) {
            try {
                emailService.enviarCorreoSimple(destinatario, ASUNTO_NEWSLETTER, cuerpoHtml);
                enviados++;
            } catch (Exception ex) {
                log.warn("No se pudo enviar newsletter a '{}': {}", destinatario, ex.getMessage());
            }
        }

        return new NewsletterEnvioResponse(enviados, items.size());
    }
}
