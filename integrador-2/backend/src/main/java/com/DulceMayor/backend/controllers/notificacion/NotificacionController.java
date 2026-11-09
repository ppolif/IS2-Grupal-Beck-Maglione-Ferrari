package com.DulceMayor.backend.controllers.notificacion;

import com.DulceMayor.backend.dtos.notificacion.NewsletterEnvioResponse;
import com.DulceMayor.backend.services.notificacion.NewsletterService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para el módulo de notificaciones y newsletters.
 *
 * <p>Expone el endpoint documentado en {@code docs/04-openapi.yaml} bajo el tag <i>Notificaciones</i>.</p>
 */
@RestController
@RequestMapping
public class NotificacionController {

    private final NewsletterService newsletterService;

    public NotificacionController(NewsletterService newsletterService) {
        this.newsletterService = newsletterService;
    }

    /**
     * Dispara manualmente el newsletter de ofertas para todos los clientes minoristas.
     *
     * <p>Permitido para roles administrativos / jefes de logística.</p>
     *
     * @return {@link NewsletterEnvioResponse} con el total de destinatarios y productos incluidos.
     */
    @PreAuthorize("hasAnyRole('JEFE_LOGISTICA', 'JEFE_VENTAS', 'ADMIN')")
    @PostMapping({"/notificaciones/newsletter/enviar", "/api/v1/notificaciones/newsletter/enviar"})
    public ResponseEntity<NewsletterEnvioResponse> enviarNewsletter() {
        NewsletterEnvioResponse response = newsletterService.enviarNewsletter();
        return ResponseEntity.ok(response);
    }
}
