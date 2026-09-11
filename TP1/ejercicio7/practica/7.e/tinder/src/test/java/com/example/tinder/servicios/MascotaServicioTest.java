package com.example.tinder.servicios;

import com.example.tinder.entidades.Foto;
import com.example.tinder.entidades.Mascota;
import com.example.tinder.entidades.Usuario;
import com.example.tinder.enumeraciones.Sexo;
import com.example.tinder.enumeraciones.Tipo;
import com.example.tinder.errores.ErrorServicio;
import com.example.tinder.repositorios.MascotaRepositorio;
import com.example.tinder.repositorios.UsuarioRepositorio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MascotaServicioTest {

    @Mock
    private UsuarioRepositorio usuarioRepositorio;

    @Mock
    private MascotaRepositorio mascotaRepositorio;

    @Mock
    private FotoServicio fotoServicio;

    @Mock
    private MultipartFile archivoFalso;

    @InjectMocks
    private MascotaServicio mascotaServicio;

    // --- TEST 1: GUARDAR MASCOTA EXITOSAMENTE ---
    @Test
    void agregarMascota_DatosValidos_GuardaMascota() throws Exception {
        Usuario usuarioFalso = new Usuario();
        usuarioFalso.setId("USER-1");

        when(usuarioRepositorio.findById("USER-1")).thenReturn(Optional.of(usuarioFalso));
        when(fotoServicio.guardar(archivoFalso)).thenReturn(new Foto());

        assertDoesNotThrow(() -> {
            mascotaServicio.agregarMascota(archivoFalso, "USER-1", "Firulais", Sexo.MACHO, Tipo.PERRO);
        });

        // Verificamos que se haya intentado guardar la mascota en la base de datos
        verify(mascotaRepositorio, times(1)).save(any(Mascota.class));
    }

    // --- TEST 2: REGLA DE NEGOCIO (NOMBRE VACÍO) ---
    @Test
    void agregarMascota_NombreVacio_LanzaErrorServicio() {
        Usuario usuarioFalso = new Usuario();
        when(usuarioRepositorio.findById("USER-1")).thenReturn(Optional.of(usuarioFalso));

        ErrorServicio excepcion = assertThrows(ErrorServicio.class, () -> {
            mascotaServicio.agregarMascota(archivoFalso, "USER-1", "", Sexo.MACHO, Tipo.PERRO);
        });

        assertEquals("Ingrese el nombre de la mascota", excepcion.getMessage());
    }

    // --- TEST 3: SEGURIDAD (MODIFICAR MASCOTA AJENA) ---
    @Test
    void actualizar_UsuarioNoEsDuenio_LanzaErrorServicio() {
        // Preparamos al verdadero dueño
        Usuario duenioReal = new Usuario();
        duenioReal.setId("USER-DUENIO-REAL");

        // Preparamos la mascota existente asignada a su dueño real
        Mascota mascotaExistente = new Mascota();
        mascotaExistente.setUsuario(duenioReal);

        // Simulamos que la base de datos encuentra a la mascota
        when(mascotaRepositorio.findById("PET-1")).thenReturn(Optional.of(mascotaExistente));

        // Un intruso intenta modificarla usando un ID distinto
        String idIntruso = "USER-INTRUSO";

        ErrorServicio excepcion = assertThrows(ErrorServicio.class, () -> {
            mascotaServicio.actualizar(archivoFalso, idIntruso, "PET-1", "Nuevo Nombre", Sexo.MACHO, Tipo.PERRO);
        });

        assertEquals("El usuario debe ser el dueño de la mascota", excepcion.getMessage());
    }

    // --- TEST 4: ELIMINAR (DAR DE BAJA) CON ÉXITO ---
    @Test
    void eliminarMascota_UsuarioEsDuenio_AsignaFechaBaja() throws Exception {
        Usuario duenio = new Usuario();
        duenio.setId("USER-1");

        Mascota mascotaExistente = new Mascota();
        mascotaExistente.setUsuario(duenio); // Asignamos el dueño
        mascotaExistente.setBaja(null); // Iniciamos activa

        when(mascotaRepositorio.findById("PET-1")).thenReturn(Optional.of(mascotaExistente));

        assertDoesNotThrow(() -> {
            mascotaServicio.eliminarMascota("USER-1", "PET-1");
        });

        // Verificamos que se le asignó una fecha de baja
        assertNotNull(mascotaExistente.getBaja());
        verify(mascotaRepositorio, times(1)).save(mascotaExistente);
    }

    // --- TEST 5: VALIDAR REGLA DE NEGOCIO (SEXO NULO) ---
    @Test
    void agregarMascota_SexoNulo_LanzaErrorServicio() {
        // 1. Arrange
        Usuario usuarioFalso = new Usuario();
        usuarioFalso.setId("USER-1");

        // El servicio primero busca al usuario antes de validar los datos,
        // así que debemos simular que el usuario sí existe.
        when(usuarioRepositorio.findById("USER-1")).thenReturn(Optional.of(usuarioFalso));

        // 2 y 3. Act & Assert
        ErrorServicio excepcion = assertThrows(ErrorServicio.class, () -> {
            // Pasamos un nombre válido pero el enum Sexo como null
            mascotaServicio.agregarMascota(archivoFalso, "USER-1", "Firulais", null, Tipo.PERRO);
        });

        assertEquals("Ingrese el sexo de la mascota", excepcion.getMessage());
    }

    // --- TEST 6: BÚSQUEDA DE MASCOTA INEXISTENTE ---
    @Test
    void buscarPorId_MascotaNoExiste_LanzaErrorServicio() {
        // 1. Arrange
        String idInvalido = "PET-99999";

        // Simulamos que la base de datos devuelve un Optional vacío (no encontró nada)
        when(mascotaRepositorio.findById(idInvalido)).thenReturn(Optional.empty());

        // 2 y 3. Act & Assert
        ErrorServicio excepcion = assertThrows(ErrorServicio.class, () -> {
            mascotaServicio.buscarPorId(idInvalido);
        });

        // Validamos el mensaje exacto incluyendo el espacio extra al final que tiene tu código
        assertEquals("La mascota solicitada no existe. ", excepcion.getMessage());
    }
}