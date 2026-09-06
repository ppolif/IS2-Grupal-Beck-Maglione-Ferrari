package com.example.tinder.servicios;

import com.example.tinder.entidades.Foto;
import com.example.tinder.entidades.Usuario;
import com.example.tinder.entidades.Zona;
import com.example.tinder.errores.ErrorServicio;
import com.example.tinder.repositorios.UsuarioRepositorio;
import com.example.tinder.repositorios.ZonaRepositorio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServicioTest {

    // @InjectMocks crea una instancia real de UsuarioServicio
    // y le inyecta dependencias falsas (Mocks) si las necesitara.
    @Mock
    private UsuarioRepositorio usuarioRepositorio;

    @Mock
    private ZonaRepositorio zonaRepositorio;

    @Mock
    private FotoServicio fotoServicio;

    @Mock
    private MultipartFile archivoFalso;

    @InjectMocks
    private UsuarioServicio usuarioServicio;


    @Test
    void validar_DatosCorrectos_NoLanzaExcepcion() {
        // 1. Arrange (Preparar los datos de prueba)
        Zona zonaValida = new Zona();

        // 2 y 3. Act & Assert (Ejecutar y Validar)
        // assertDoesNotThrow verifica que el código "feliz" no rompa la aplicación
        assertDoesNotThrow(() -> {
            usuarioServicio.validar("Juan", "Perez", "correo@test.com", "1234567", "1234567", zonaValida);
        });
    }

    @Test
    void validar_NombreVacio_LanzaErrorServicio() {
        // 1. Arrange
        Zona zonaValida = new Zona();

        // 2 y 3. Act & Assert
        // assertThrows atrapa la excepción para que el test no falle por el error, sino que lo valide
        ErrorServicio excepcion = assertThrows(ErrorServicio.class, () -> {
            usuarioServicio.validar("", "Perez", "correo@test.com", "1234567", "1234567", zonaValida);
        });

        // Validamos que el mensaje de la excepción sea exactamente el que programaste
        assertEquals("Debe indicar un nombre.", excepcion.getMessage());
    }

    @Test
    void validar_ApellidoVacio_LanzaErrorServicio() {
        Zona zonaValida = new Zona();

        ErrorServicio excepcion = assertThrows(ErrorServicio.class, () -> {
            // Pasamos un apellido vacío ("")
            usuarioServicio.validar("Juan", "", "correo@test.com", "1234567", "1234567", zonaValida);
        });

        assertEquals("Debe indicar un apellido.", excepcion.getMessage());
    }

    @Test
    void validar_MailNulo_LanzaErrorServicio() {
        Zona zonaValida = new Zona();

        ErrorServicio excepcion = assertThrows(ErrorServicio.class, () -> {
            // Pasamos un mail nulo
            usuarioServicio.validar("Juan", "Perez", null, "1234567", "1234567", zonaValida);
        });

        assertEquals("Debe indicar un mail.", excepcion.getMessage());
    }

    @Test
    void validar_ClaveCorta_LanzaErrorServicio() {
        Zona zonaValida = new Zona();

        ErrorServicio excepcion = assertThrows(ErrorServicio.class, () -> {
            // Pasamos una clave de 6 caracteres (la regla dice que debe tener MAS de 6)
            usuarioServicio.validar("Juan", "Perez", "correo@test.com", "123456", "123456", zonaValida);
        });

        assertEquals("La clave no puede ser nula, debe tener mas de 6 caracteres.", excepcion.getMessage());
    }

    @Test
    void validar_ClavesNoCoinciden_LanzaErrorServicio() {
        Zona zonaValida = new Zona();

        ErrorServicio excepcion = assertThrows(ErrorServicio.class, () -> {
            // Pasamos clave "1234567" y repetirClave "7654321"
            usuarioServicio.validar("Juan", "Perez", "correo@test.com", "1234567", "7654321", zonaValida);
        });

        assertEquals("Las claves deben ser iguales", excepcion.getMessage());
    }

    @Test
    void validar_ZonaNula_LanzaErrorServicio() {
        ErrorServicio excepcion = assertThrows(ErrorServicio.class, () -> {
            // Pasamos zona como null
            usuarioServicio.validar("Juan", "Perez", "correo@test.com", "1234567", "1234567", null);
        });

        assertEquals("No se encontró la zona solicitada", excepcion.getMessage());
    }

    @Test
    void buscarPorId_UsuarioExiste_RetornaUsuario() throws ErrorServicio {
        // 1. Arrange (Preparar)
        String idPrueba = "ID-12345";
        Usuario usuarioSimulado = new Usuario();
        usuarioSimulado.setId(idPrueba);
        usuarioSimulado.setNombre("Fido");

        // MAGIA DE MOCKITO: Cuando el servicio llame a findById con "ID-12345",
        // le decimos al repositorio falso que devuelva nuestro usuarioSimulado envuelto en un Optional.
        when(usuarioRepositorio.findById(idPrueba)).thenReturn(Optional.of(usuarioSimulado));

        // 2. Act (Ejecutar)
        Usuario resultado = usuarioServicio.buscarPorId(idPrueba);

        // 3. Assert (Validar)
        // Verificamos que el servicio nos devolvió exactamente el mismo usuario que simulamos
        assertEquals(idPrueba, resultado.getId());
        assertEquals("Fido", resultado.getNombre());
    }

    @Test
    void buscarPorId_UsuarioNoExiste_LanzaErrorServicio() {
        // 1. Arrange
        String idFalso = "ID-99999";

        // Simulamos que la base de datos no encontró nada (devuelve Optional vacío)
        when(usuarioRepositorio.findById(idFalso)).thenReturn(Optional.empty());

        // 2 y 3. Act & Assert
        ErrorServicio excepcion = assertThrows(ErrorServicio.class, () -> {
            usuarioServicio.buscarPorId(idFalso);
        });

        // Validamos que el mensaje de error coincida con el de tu código
        assertEquals("No se encontró el usuario", excepcion.getMessage());
    }

    @Test
    void registrar_DatosValidos_GuardaUsuarioExitosamente() throws Exception {
        // 1. Arrange (Preparar el escenario)
        String idZona = "ID-ZONA-1";
        Zona zonaFalsa = new Zona();
        zonaFalsa.setId(idZona);

        Foto fotoFalsa = new Foto();
        fotoFalsa.setId("ID-FOTO-1");

        // Le enseñamos a los Mocks cómo deben comportarse cuando el servicio los llame
        when(zonaRepositorio.getOne(idZona)).thenReturn(zonaFalsa);
        when(fotoServicio.guardar(archivoFalso)).thenReturn(fotoFalsa);

        // 2. Act (Ejecutar)
        // Usamos assertDoesNotThrow porque si todo sale bien, no debería saltar ningún ErrorServicio
        assertDoesNotThrow(() -> {
            usuarioServicio.registrar(
                    archivoFalso,
                    "Juan",
                    "Perez",
                    "juan@correo.com",
                    "1234567",
                    "1234567",
                    idZona
            );
        });

        // 3. Assert (Validar con 'verify')
        // Comprobamos que el repositorio "save" haya sido ejecutado exactamente 1 vez
        // "any(Usuario.class)" significa que no importa los datos exactos del usuario,
        // solo nos importa que se haya intentado guardar un objeto de tipo Usuario.
        verify(usuarioRepositorio, times(1)).save(any(Usuario.class));

        // También podemos verificar que se haya intentado guardar la foto
        verify(fotoServicio, times(1)).guardar(archivoFalso);
    }
}