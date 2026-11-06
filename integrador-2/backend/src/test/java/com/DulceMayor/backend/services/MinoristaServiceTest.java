package com.DulceMayor.backend.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.DulceMayor.backend.config.ConflictoException;
import com.DulceMayor.backend.dtos.AuthResponse;
import com.DulceMayor.backend.dtos.UsuarioResponse;
import com.DulceMayor.backend.dtos.persona.ActualizarPerfilMinoristaRequest;
import com.DulceMayor.backend.dtos.persona.MinoristaMapper;
import com.DulceMayor.backend.dtos.persona.RegistroMinoristaRequest;
import com.DulceMayor.backend.entities.ProveedorAuth;
import com.DulceMayor.backend.entities.RolUsuario;
import com.DulceMayor.backend.entities.Usuario;
import com.DulceMayor.backend.entities.persona.Minorista;
import com.DulceMayor.backend.entities.persona.TipoDocumento;
import com.DulceMayor.backend.repositories.persona.MinoristaRepository;
import com.DulceMayor.backend.services.direccion.DireccionService;
import com.DulceMayor.backend.services.persona.MinoristaService;
import com.DulceMayor.backend.services.persona.PersonaService;
import java.util.Optional;
import org.springframework.context.ApplicationEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests unitarios para {@link MinoristaService} con Mockito.
 */
@ExtendWith(MockitoExtension.class)
class MinoristaServiceTest {

    @Mock
    private MinoristaRepository minoristaRepository;

    @Mock
    private MinoristaMapper minoristaMapper;

    @Mock
    private PersonaService personaService;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private DireccionService direccionService;

    @Mock
    private ContactoService contactoService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private MinoristaService minoristaService;

    @BeforeEach
    void setUp() {
        minoristaService = new MinoristaService(
                minoristaRepository,
                minoristaMapper,
                personaService,
                usuarioService,
                direccionService,
                contactoService,
                eventPublisher
        );
    }

    @Test
    @DisplayName("registrarEnPortal: cliente nuevo crea Minorista y Usuario con claveTemporal=false")
    void registrarEnPortalCreaMinoristaYClaveTemporalFalse() {
        RegistroMinoristaRequest req = new RegistroMinoristaRequest(
                "Ana",
                "Suarez",
                TipoDocumento.DNI,
                "34555666",
                "ana@comercio.com",
                "MiClaveSegura123",
                "Almacén Ana"
        );

        when(usuarioService.existeNombreUsuario("ana@comercio.com")).thenReturn(false);

        Usuario nuevoUsuario = Usuario.builder()
                .id("usr-ana")
                .nombreUsuario("ana@comercio.com")
                .rol(RolUsuario.MINORISTA)
                .claveTemporal(false)
                .proveedorAuth(ProveedorAuth.LOCAL)
                .build();
        when(usuarioService.crearUsuario("ana@comercio.com", "MiClaveSegura123", RolUsuario.MINORISTA, false))
                .thenReturn(nuevoUsuario);

        AuthResponse authRes = new AuthResponse(
                "jwt-ana",
                "Bearer",
                3600L,
                false,
                new UsuarioResponse("usr-ana", "ana@comercio.com", RolUsuario.MINORISTA, false, ProveedorAuth.LOCAL)
        );
        when(usuarioService.generarAuthResponse(nuevoUsuario)).thenReturn(authRes);

        AuthResponse res = minoristaService.registrarEnPortal(req);

        assertNotNull(res);
        assertEquals(false, res.debeCambiarClave());
        verify(personaService).validarDocumentoUnico(TipoDocumento.DNI, "34555666");
        verify(minoristaRepository).save(any(Minorista.class));
    }

    @Test
    @DisplayName("registrarEnPortal: rechaza con 409 si el correo ya posee usuario")
    void registrarEnPortalCorreoDuplicadoLanza409() {
        RegistroMinoristaRequest req = new RegistroMinoristaRequest(
                "Pedro", "Gomez", TipoDocumento.DNI, "28111222",
                "pedro@kiosco.com", "ClaveSecreta123", null
        );

        when(usuarioService.existeNombreUsuario("pedro@kiosco.com")).thenReturn(true);

        assertThrows(ConflictoException.class, () -> minoristaService.registrarEnPortal(req));
    }

    @Test
    @DisplayName("actualizarMiPerfil: actualiza datos, sincroniza nombre de usuario y teléfono")
    void actualizarMiPerfilSincronizaEmailYTelefono() {
        Usuario usuario = Usuario.builder()
                .id("usr-1")
                .nombreUsuario("viejo@kiosco.com")
                .build();

        Minorista minorista = Minorista.builder()
                .id("min-1")
                .nombre("Juan")
                .apellido("Perez")
                .tipoDocumento(TipoDocumento.DNI)
                .numeroDocumento("20111222")
                .usuario(usuario)
                .contactos(new java.util.ArrayList<>())
                .direcciones(new java.util.ArrayList<>())
                .eliminado(false)
                .build();

        when(minoristaRepository.findByUsuarioIdAndEliminadoFalse("usr-1")).thenReturn(Optional.of(minorista));
        when(usuarioService.existeNombreUsuarioExcluyendoId("nuevo@kiosco.com", "usr-1")).thenReturn(false);
        when(minoristaRepository.save(any(Minorista.class))).thenAnswer(inv -> inv.getArgument(0));

        ActualizarPerfilMinoristaRequest req = new ActualizarPerfilMinoristaRequest(
                "Juan Manuel",
                "Perez Gomez",
                "nuevo@kiosco.com",
                "2614567890"
        );

        minoristaService.actualizarMiPerfil("usr-1", req);

        verify(usuarioService).actualizarNombreUsuario(usuario, "nuevo@kiosco.com");
        assertEquals("Juan Manuel", minorista.getNombre());
        assertEquals("Perez Gomez", minorista.getApellido());
    }

    @Test
    @DisplayName("eliminarMiCuenta: borra lógicamente Minorista, Usuario y emite evento")
    void eliminarMiCuentaBorraUsuarioYEmiteEvento() {
        Usuario usuario = Usuario.builder().id("usr-del").build();
        Minorista minorista = Minorista.builder()
                .id("min-del")
                .usuario(usuario)
                .contactos(new java.util.ArrayList<>())
                .direcciones(new java.util.ArrayList<>())
                .eliminado(false)
                .build();

        when(minoristaRepository.findByUsuarioIdAndEliminadoFalse("usr-del")).thenReturn(Optional.of(minorista));
        when(minoristaRepository.findByIdAndEliminadoFalse("min-del")).thenReturn(Optional.of(minorista));
        when(minoristaRepository.save(any(Minorista.class))).thenAnswer(inv -> inv.getArgument(0));

        minoristaService.eliminarMiCuenta("usr-del");

        verify(usuarioService).eliminarUsuario("usr-del");
        verify(eventPublisher).publishEvent(any(com.DulceMayor.backend.dtos.persona.MinoristaEliminadoEvent.class));
        assertTrue(minorista.isEliminado());
    }
}

