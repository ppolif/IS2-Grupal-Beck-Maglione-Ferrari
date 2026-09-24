package com.example.zero.services;

import com.example.zero.services.mail.EmailService;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
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

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    private EmailService emailService;

    @BeforeEach
    void setUp() {
        emailService = new EmailService(mailSender);
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
}

