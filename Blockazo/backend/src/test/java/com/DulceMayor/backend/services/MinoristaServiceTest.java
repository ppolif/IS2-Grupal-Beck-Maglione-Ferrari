package com.DulceMayor.backend.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.DulceMayor.backend.dtos.AuthResponse;
import com.DulceMayor.backend.dtos.UsuarioResponse;
import com.DulceMayor.backend.dtos.persona.ActualizarPerfilMinoristaRequest;
import com.DulceMayor.backend.dtos.persona.MinoristaMapper;
import com.DulceMayor.backend.dtos.persona.RegistroMinoristaRequest;
import com.DulceMayor.backend.entities.ProveedorAuth;
import com.DulceMayor.backend.entities.Usuario;
import com.DulceMayor.backend.entities.persona.Minorista;
import com.DulceMayor.backend.repositories.persona.MinoristaRepository;
import com.DulceMayor.backend.services.direccion.DireccionService;
import com.DulceMayor.backend.services.direccion.LocalidadService;
import com.DulceMayor.backend.services.persona.MinoristaService;
import com.DulceMayor.backend.services.persona.PersonaService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

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
    private LocalidadService localidadService;

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
                localidadService,
                eventPublisher
        );
    }

    @Test
    @DisplayName("registrarEnPortal: cliente nuevo crea Minorista y Usuario con claveTemporal=false")
    void registrarEnPortalCasoCCreaMinoristaYClaveTemporalFalse() {
        RegistroMinoristaRequest req = new RegistroMinoristaRequest(
                "Ana",
                "Suarez",
                "34555666",
                "ana@comercio.com",
                "MiClaveSegura123",
                "Almacén Ana"
        );

        when(usuarioService.existeNombreUsuario("ana@comercio.com")).thenReturn(false);

        Usuario nuevoUsuario = Usuario.builder()
                .id("usr-ana")
                .nombreUsuario("ana@comercio.com")
                .claveTemporal(false)
                .proveedorAuth(ProveedorAuth.LOCAL)
                .build();
        when(usuarioService.crearUsuario("ana@comercio.com", "MiClaveSegura123", false))
                .thenReturn(nuevoUsuario);

        AuthResponse authRes = new AuthResponse(
                "jwt-ana",
                "Bearer",
                3600L,
                false,
                new UsuarioResponse("usr-ana", "ana@comercio.com", false, ProveedorAuth.LOCAL)
        );
        when(usuarioService.generarAuthResponse(nuevoUsuario)).thenReturn(authRes);

        AuthResponse res = minoristaService.registrarEnPortal(req);

        assertNotNull(res);
        assertEquals(false, res.debeCambiarClave());
        verify(personaService).validarDocumentoUnico("34555666");
        verify(minoristaRepository).save(any(Minorista.class));
    }

    @Test
    @DisplayName("actualizarMiPerfil: actualiza datos y sincroniza nombre de usuario")
    void actualizarMiPerfilSincronizaEmail() {
        Usuario usuario = Usuario.builder()
                .id("usr-1")
                .nombreUsuario("viejo@kiosco.com")
                .build();

        Minorista minorista = Minorista.builder()
                .id("min-1")
                .nombre("Juan")
                .apellido("Perez")
                .numeroDocumento("20111222")
                .usuario(usuario)
                .direcciones(new java.util.ArrayList<>())
                .eliminado(false)
                .build();

        when(minoristaRepository.findByUsuarioIdAndEliminadoFalse("usr-1")).thenReturn(Optional.of(minorista));
        when(usuarioService.existeNombreUsuarioExcluyendoId("nuevo@kiosco.com", "usr-1")).thenReturn(false);
        when(minoristaRepository.save(any(Minorista.class))).thenAnswer(inv -> inv.getArgument(0));

        ActualizarPerfilMinoristaRequest req = new ActualizarPerfilMinoristaRequest(
                "Juan Manuel",
                "Perez Gomez",
                java.time.LocalDate.of(1990, 5, 20),
                "20111222",
                "nuevo@kiosco.com"
        );

        minoristaService.actualizarMiPerfil("usr-1", req);

        verify(usuarioService).actualizarNombreUsuario(usuario, "nuevo@kiosco.com");
        verify(personaService).validarDocumentoUnicoExcluyendoId("20111222", "min-1");
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
