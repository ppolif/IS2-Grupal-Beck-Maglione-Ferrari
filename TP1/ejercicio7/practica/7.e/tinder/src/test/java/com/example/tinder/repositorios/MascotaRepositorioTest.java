package com.example.tinder.repositorios;

import com.example.tinder.entidades.Mascota;
import com.example.tinder.entidades.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MascotaRepositorioTest {

    @Autowired
    private MascotaRepositorio mascotaRepositorio;

    // Necesitamos el de usuario para respetar la relación de la base de datos (Clave foránea)
    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    @Test
    void buscarMascotaPorUsuario_TraeSoloMascotasActivasDelDuenio() {
        // 1. Arrange: Creamos un dueño y lo guardamos
        Usuario duenio = new Usuario();
        duenio.setNombre("Dueño Test");
        usuarioRepositorio.save(duenio);

        // Creamos una mascota ACTIVA para ese dueño
        Mascota mascotaActiva = new Mascota();
        mascotaActiva.setNombre("Activa");
        mascotaActiva.setUsuario(duenio);
        mascotaActiva.setBaja(null); // Está activa
        mascotaRepositorio.save(mascotaActiva);

        // Creamos una mascota DADA DE BAJA para ese dueño
        Mascota mascotaInactiva = new Mascota();
        mascotaInactiva.setNombre("Inactiva");
        mascotaInactiva.setUsuario(duenio);
        mascotaInactiva.setBaja(new Date()); // Tiene fecha de baja
        mascotaRepositorio.save(mascotaInactiva);

        // 2. Act: Ejecutamos el query personalizado
        List<Mascota> resultados = mascotaRepositorio.buscarMascotaPorUsuario(duenio.getId());

        // 3. Assert:
        // Solo debería traer 1 mascota (la activa), ignorando a la inactiva
        assertEquals(1, resultados.size());
        assertEquals("Activa", resultados.get(0).getNombre());
    }

    @Test
    void buscarMascotaPorUsuario_UsuarioSinMascotas_RetornaListaVacia() {
        // 1. Arrange: Creamos un usuario sin asignarle mascotas
        Usuario usuarioSinMascotas = new Usuario();
        usuarioRepositorio.save(usuarioSinMascotas);

        // 2. Act
        List<Mascota> resultados = mascotaRepositorio.buscarMascotaPorUsuario(usuarioSinMascotas.getId());

        // 3. Assert: Debería devolver una lista vacía
        assertTrue(resultados.isEmpty());
    }
}