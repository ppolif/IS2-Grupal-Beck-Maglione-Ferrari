package com.example.tinder.service;

import com.example.tinder.dto.MascotaEdicionDTO;
import com.example.tinder.entidades.Foto;
import com.example.tinder.entidades.Mascota;
import com.example.tinder.entidades.Usuario;
import com.example.tinder.enumeraciones.Sexo;
import com.example.tinder.enumeraciones.Tipo;
import com.example.tinder.errores.ErrorServicio;
import com.example.tinder.repositorios.MascotaRepositorio;
import com.example.tinder.repositorios.UsuarioRepositorio;
import com.example.tinder.servicios.FotoServicio;
import com.example.tinder.servicios.MascotaServicio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class MascotaServicioTest {

    //esta anotacion indica a que clase le hacemos el test, que es a la que le vamos
    //a inyectar los mocks justamente
    @InjectMocks
    private MascotaServicio mascotaServicio;

    //esto simula los otros componentes del proyecto que necesita para funcionar
    //la clase testeada, asi los usamos sin generar cambios reales en el resto del sistema
    //por ejemplo en la base de datos
    @Mock
    private MascotaRepositorio mascotaRepositorio;

    @Mock
    private UsuarioRepositorio usuarioRepositorio;

    @Mock
    private FotoServicio fotoServicio;

    //esta anotacion de junit significa que el metodo se ejecuta antes de cada test
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    //lo que se verifica es que el metodo agregarMascota siga el flujo correcto,
    //o sea que si tenemos un usuario valido y un dto de mascota valido, que se
    //busque en la base de datos al user y que se guarde algun objeto mascota con save
    @Test
    public void testAgregarMascota() throws Exception {
        Usuario usuarioMock = new Usuario();
        usuarioMock.setId("123");

        MascotaEdicionDTO dto = new MascotaEdicionDTO();
        dto.setNombre("choco");
        dto.setSexo(Sexo.MACHO);
        dto.setTipo(Tipo.PERRO);

        when(usuarioRepositorio.findById("123")).thenReturn(Optional.of(usuarioMock));
        when(mascotaRepositorio.save(any(Mascota.class))).thenReturn(new Mascota());

        mascotaServicio.agregarMascota("123", dto);

        verify(usuarioRepositorio, times(1)).findById("123");
        verify(mascotaRepositorio, times(1)).save(any(Mascota.class));
    }

    //tmb se testea el caso de fracaso de los metodos, es decir se testea
    //que tiren excepciones bajo las condiciones impuestas
    @Test
    public void testAgregarMascotaExcepcion() {
        when(usuarioRepositorio.findById("123")).thenReturn(Optional.empty());

        MascotaEdicionDTO dto = new MascotaEdicionDTO();
        dto.setNombre("romi");
        dto.setTipo(Tipo.GATO);
        dto.setSexo(Sexo.HEMBRA);

        Exception exception = assertThrows(Exception.class, () -> {
            mascotaServicio.agregarMascota("123", dto);
        });

        assertNotNull(exception);
    }

    @Test
    public void testActualizarMascota() throws Exception {
        Usuario user = new Usuario();
        user.setId("123");

        Mascota mascotaOriginal = new Mascota();
        mascotaOriginal.setId("1");
        mascotaOriginal.setUsuario(user);

        MascotaEdicionDTO dto = new MascotaEdicionDTO();
        dto.setId("1");
        dto.setNombre("actualizado");
        dto.setSexo(Sexo.MACHO);
        dto.setTipo(Tipo.PERRO);

        when(mascotaRepositorio.findById("1")).thenReturn(Optional.of(mascotaOriginal));
        when(mascotaRepositorio.save(any(Mascota.class))).thenReturn(new Mascota());

        mascotaServicio.actualizar("123", dto);

        verify(mascotaRepositorio, times(1)).save(any(Mascota.class));
    }

    @Test
    public void testActualizarMascotaExceptionNoExisteMascota() {
        MascotaEdicionDTO dto = new MascotaEdicionDTO();
        dto.setId("1");
        dto.setNombre("actualizar");
        dto.setSexo(Sexo.MACHO);

        when(mascotaRepositorio.findById("1")).thenReturn(Optional.empty());

        ErrorServicio exception = assertThrows(ErrorServicio.class, () -> {
            mascotaServicio.actualizar("123", dto);
        });

        assertEquals("No existe la mascota", exception.getMessage());
    }

    @Test
    public void testActualizarMascotaExceptionUsuario() {
        Usuario real = new Usuario();
        real.setId("real");

        Mascota mascotaOriginal = new Mascota();
        mascotaOriginal.setId("1");
        mascotaOriginal.setUsuario(real);

        MascotaEdicionDTO dto = new MascotaEdicionDTO();
        dto.setId("1");
        dto.setNombre("nose");
        dto.setSexo(Sexo.HEMBRA);

        when(mascotaRepositorio.findById("1")).thenReturn(Optional.of(mascotaOriginal));

        ErrorServicio exception = assertThrows(ErrorServicio.class, () -> {
            mascotaServicio.actualizar("falso", dto);
        });

        assertEquals("El usuario debe ser el dueño de la mascota", exception.getMessage());
    }

    @Test
    public void testEliminarMascota() throws Exception {
        Usuario user = new Usuario();
        user.setId("123");

        Mascota mascotaOriginal = new Mascota();
        mascotaOriginal.setId("1");
        mascotaOriginal.setUsuario(user);

        when(mascotaRepositorio.findById("1")).thenReturn(Optional.of(mascotaOriginal));

        mascotaServicio.eliminarMascota("123", "1");

        verify(mascotaRepositorio, times(1)).save(any(Mascota.class));
    }
}
