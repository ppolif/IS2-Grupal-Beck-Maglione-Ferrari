package com.DulceMayor.backend.services.mail;

import com.DulceMayor.backend.repositories.mayorista.ConfiguracionCorreoMayoristaRepository;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender defaultMailSender;

    @Mock
    private ConfiguracionCorreoMayoristaRepository configRepository;

    private EmailService emailService;

    @BeforeEach
    void setUp() {
        emailService = new EmailService(defaultMailSender, configRepository);
    }

    @Test
    @DisplayName("enviarCorreoSimple con destinatario nulo o en blanco no invoca send")
    void enviarCorreoDestinatarioVacioNoEnvia() {
        emailService.enviarCorreoSimple("", "Asunto", "<p>Hola</p>");
        emailService.enviarCorreoSimple(null, "Asunto", "<p>Hola</p>");

        verify(defaultMailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("enviarCorreoSimple invoca send exitosamente usando defaultMailSender si no hay config en BD")
    void enviarCorreoSimpleExitoso() {
        when(configRepository.findAll()).thenReturn(List.of());
        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(defaultMailSender.createMimeMessage()).thenReturn(mimeMessage);

        assertDoesNotThrow(() ->
                emailService.enviarCorreoSimple("cliente@correo.com", "Asunto Test", "<h1>Prueba</h1>")
        );

        verify(defaultMailSender, times(1)).send(mimeMessage);
    }

    @Test
    @DisplayName("enviarCorreoConAdjunto adjunta el archivo binario y despacha el mail")
    void enviarCorreoConAdjuntoExitoso() {
        when(configRepository.findAll()).thenReturn(List.of());
        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(defaultMailSender.createMimeMessage()).thenReturn(mimeMessage);

        byte[] pdfFicticio = "%PDF-1.4...".getBytes();

        assertDoesNotThrow(() ->
                emailService.enviarCorreoConAdjunto(
                        "cliente@correo.com",
                        "Factura",
                        "<p>Detalle adjunto</p>",
                        pdfFicticio,
                        "factura-1.pdf",
                        "application/pdf"
                )
        );

        verify(defaultMailSender, times(1)).send(mimeMessage);
    }

    @Test
    @DisplayName("resolverEmailMinorista prioriza usuario y luego contactos secundarios")
    void resolverEmailMinoristaPrioridades() {
        com.DulceMayor.backend.entities.Usuario usuario = com.DulceMayor.backend.entities.Usuario.builder()
                .nombreUsuario("usuario@login.com")
                .build();

        com.DulceMayor.backend.entities.contacto.ContactoCorreoElectronico contactoMail =
                com.DulceMayor.backend.entities.contacto.ContactoCorreoElectronico.builder()
                        .email("contacto@secundario.com")
                        .build();

        com.DulceMayor.backend.entities.persona.Minorista conUsuarioYContacto = com.DulceMayor.backend.entities.persona.Minorista.builder()
                .usuario(usuario)
                .contactos(List.of(contactoMail))
                .build();

        // 1. Debe priorizar el usuario
        org.junit.jupiter.api.Assertions.assertEquals("usuario@login.com", emailService.resolverEmailMinorista(conUsuarioYContacto));

        // 2. Sin usuario, debe tomar el contacto
        com.DulceMayor.backend.entities.persona.Minorista soloContacto = com.DulceMayor.backend.entities.persona.Minorista.builder()
                .usuario(null)
                .contactos(List.of(contactoMail))
                .build();
        org.junit.jupiter.api.Assertions.assertEquals("contacto@secundario.com", emailService.resolverEmailMinorista(soloContacto));

        // 3. Nulo o sin emails debe dar null
        org.junit.jupiter.api.Assertions.assertNull(emailService.resolverEmailMinorista(null));
    }
}
