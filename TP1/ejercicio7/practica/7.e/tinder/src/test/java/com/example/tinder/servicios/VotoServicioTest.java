package com.example.tinder.servicios;

import com.example.tinder.entidades.Mascota;
import com.example.tinder.entidades.Usuario;
import com.example.tinder.entidades.Voto;
import com.example.tinder.errores.ErrorServicio;
import com.example.tinder.repositorios.MascotaRepositorio;
import com.example.tinder.repositorios.VotoRepositorio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VotoServicioTest {

    @Mock
    private VotoRepositorio votoRepositorio;

    @Mock
    private MascotaRepositorio mascotaRepositorio;

    @Mock
    private NotificacionServicio notificacionServicio;

    @InjectMocks
    private VotoServicio votoServicio;

    // --- TEST 1: REGLA DE NEGOCIO (AUTO-VOTO) ---
    @Test
    void votar_MismasMascotas_LanzaErrorServicio() {
        ErrorServicio excepcion = assertThrows(ErrorServicio.class, () -> {
            votoServicio.votar("USER-1", "PET-1", "PET-1");
        });

        assertEquals("No se puede votar a si mismo", excepcion.getMessage());
    }

    // --- TEST 2: SEGURIDAD (VOTAR CON MASCOTA AJENA) ---
    @Test
    void votar_UsuarioNoEsDuenio_LanzaErrorServicio() {
        Usuario duenioReal = new Usuario();
        duenioReal.setId("USER-DUENIO");

        Mascota mascota1 = new Mascota();
        mascota1.setUsuario(duenioReal);

        when(mascotaRepositorio.findById("PET-1")).thenReturn(Optional.of(mascota1));

        ErrorServicio excepcion = assertThrows(ErrorServicio.class, () -> {
            // El intruso intenta usar la PET-1 que no le pertenece
            votoServicio.votar("USER-INTRUSO", "PET-1", "PET-2");
        });

        assertEquals("No tiene permisos para realizar la operacion solicitada", excepcion.getMessage());
    }

    // --- TEST 3: SEGURIDAD (RESPONDER VOTO AJENO) ---
    @Test
    void responder_UsuarioNoEsDuenioDeMascota2_LanzaErrorServicio() {
        Usuario duenioReal = new Usuario();
        duenioReal.setId("USER-DUENIO");

        Mascota mascota2 = new Mascota();
        mascota2.setUsuario(duenioReal);

        Voto voto = new Voto();
        voto.setMascota2(mascota2); // La mascota que recibió el voto

        when(votoRepositorio.findById("VOTO-1")).thenReturn(Optional.of(voto));

        ErrorServicio excepcion = assertThrows(ErrorServicio.class, () -> {
            // Un intruso intenta responder el voto
            votoServicio.responder("USER-INTRUSO", "VOTO-1");
        });

        assertEquals("No tiene permisos para realizar esta accion", excepcion.getMessage());
    }

    // --- TEST 4: CAMINO FELIZ (RESPONDER CON ÉXITO) ---
    @Test
    void responder_DatosValidos_ActualizaVotoYNotifica() throws Exception {
        Usuario usuarioResponde = new Usuario();
        usuarioResponde.setId("USER-RESPONDE");

        Usuario usuarioOriginal = new Usuario();
        usuarioOriginal.setEmail("original@correo.com");

        Mascota mascota1 = new Mascota();
        mascota1.setUsuario(usuarioOriginal); // El que emitió el voto primero

        Mascota mascota2 = new Mascota();
        mascota2.setUsuario(usuarioResponde); // El que lo recibe y ahora responde

        Voto voto = new Voto();
        voto.setMascota1(mascota1);
        voto.setMascota2(mascota2);
        voto.setRespuesta(null);

        when(votoRepositorio.findById("VOTO-1")).thenReturn(Optional.of(voto));

        // Ejecutamos
        assertDoesNotThrow(() -> {
            votoServicio.responder("USER-RESPONDE", "VOTO-1");
        });

        // Validamos que se le asignó fecha de respuesta
        assertNotNull(voto.getRespuesta());

        // Validamos que se guardó en BD
        verify(votoRepositorio, times(1)).save(voto);

        // Validamos que se envió el correo
        verify(notificacionServicio, times(1)).enviar(anyString(), anyString(), eq("original@correo.com"));
    }

    // --- TEST 5: CAMINO FELIZ (VOTAR CON ÉXITO) ---
    @Test
    void votar_DatosValidos_GuardaVotoYNotifica() throws Exception {
        // 1. Arrange
        Usuario duenio1 = new Usuario();
        duenio1.setId("USER-1");

        Usuario duenio2 = new Usuario();
        duenio2.setEmail("duenio2@correo.com");

        Mascota mascota1 = new Mascota();
        mascota1.setUsuario(duenio1);

        Mascota mascota2 = new Mascota();
        mascota2.setUsuario(duenio2);

        when(mascotaRepositorio.findById("PET-1")).thenReturn(Optional.of(mascota1));
        when(mascotaRepositorio.findById("PET-2")).thenReturn(Optional.of(mascota2));

        // 2. Act
        assertDoesNotThrow(() -> {
            votoServicio.votar("USER-1", "PET-1", "PET-2");
        });

        // 3. Assert
        // Verificamos que se guardó el voto
        verify(votoRepositorio, times(1)).save(any(Voto.class));

        // Verificamos que se notificó al correo correcto (el dueño de la mascota 2)
        verify(notificacionServicio, times(1)).enviar(anyString(), anyString(), eq("duenio2@correo.com"));
    }

    // --- TEST 6: MASCOTA INEXISTENTE AL VOTAR ---
    @Test
    void votar_MascotaNoExiste_LanzaErrorServicio() {
        // Simulamos que la Mascota 1 no se encuentra en la base de datos
        when(mascotaRepositorio.findById("PET-INEXISTENTE")).thenReturn(Optional.empty());

        ErrorServicio excepcion = assertThrows(ErrorServicio.class, () -> {
            votoServicio.votar("USER-1", "PET-INEXISTENTE", "PET-2");
        });

        assertEquals("No existe la mascota", excepcion.getMessage());
    }

    // --- TEST 7: VOTO INEXISTENTE AL RESPONDER ---
    @Test
    void responder_VotoNoExiste_LanzaErrorServicio() {
        // Simulamos que el Voto no se encuentra
        when(votoRepositorio.findById("VOTO-INEXISTENTE")).thenReturn(Optional.empty());

        ErrorServicio excepcion = assertThrows(ErrorServicio.class, () -> {
            votoServicio.responder("USER-1", "VOTO-INEXISTENTE");
        });

        assertEquals("No existe el voto", excepcion.getMessage());
    }
}