package com.DulceMayor.backend.services.notificacion;

import com.DulceMayor.backend.dtos.notificacion.NewsletterEnvioResponse;
import com.DulceMayor.backend.entities.Usuario;
import com.DulceMayor.backend.entities.contacto.ContactoCorreoElectronico;
import com.DulceMayor.backend.entities.persona.Minorista;
import com.DulceMayor.backend.entities.producto.Producto;
import com.DulceMayor.backend.repositories.persona.MinoristaRepository;
import com.DulceMayor.backend.repositories.producto.ProductoRepository;
import com.DulceMayor.backend.services.mail.EmailService;
import com.DulceMayor.backend.services.producto.VigenciaPrecioService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NewsletterServiceTest {

    @Mock
    private MinoristaRepository minoristaRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private VigenciaPrecioService vigenciaPrecioService;

    @Mock
    private EmailService emailService;

    private NewsletterService newsletterService;

    @BeforeEach
    void setUp() {
        newsletterService = new NewsletterService(
                minoristaRepository,
                productoRepository,
                vigenciaPrecioService,
                emailService
        );
    }

    @Test
    @DisplayName("enviarNewsletter: envía correo a minoristas con email con los productos en oferta")
    void enviarNewsletterExitoso() {
        Producto prodOferta = Producto.builder()
                .id("prod-oferta-1")
                .codigo("OFER-01")
                .nombre("Alfajor Triple Oferta")
                .enOferta(true)
                .eliminado(false)
                .build();

        when(productoRepository.findAllByEnOfertaTrueAndEliminadoFalseOrderByNombreAsc())
                .thenReturn(List.of(prodOferta));
        when(vigenciaPrecioService.obtenerPrecioVigente("prod-oferta-1")).thenReturn(1500.0);

        Usuario usuario = Usuario.builder()
                .nombreUsuario("kiosco@test.com")
                .eliminado(false)
                .build();

        Minorista minorista = Minorista.builder()
                .id("min-1")
                .nombre("Juan")
                .apellido("Perez")
                .usuario(usuario)
                .eliminado(false)
                .build();

        when(minoristaRepository.findAllActivosConUsuarios()).thenReturn(List.of(minorista));

        NewsletterEnvioResponse res = newsletterService.enviarNewsletter();

        assertEquals(1, res.destinatarios());
        assertEquals(1, res.productosEnOferta());
        verify(emailService, times(1)).enviarCorreoSimple(
                eq("kiosco@test.com"),
                contains("Ofertas Especiales"),
                contains("Alfajor Triple Oferta")
        );
    }
}
