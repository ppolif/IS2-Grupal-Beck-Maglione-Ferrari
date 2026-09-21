package com.example.tinder.repositorios;

import com.example.tinder.entidades.Mascota;
import com.example.tinder.entidades.Usuario;
import com.example.tinder.entidades.Voto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class VotoRepositorioTest {

    @Autowired
    private VotoRepositorio votoRepositorio;

    // Necesitamos estos dos para respetar las claves foráneas al guardar en MySQL
    @Autowired
    private MascotaRepositorio mascotaRepositorio;
    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    @Test
    void buscarVotos_PropiosYRecibidos_TraeDatosOrdenadosPorFechaDescendente() {
        // 1. Arrange: Preparamos los datos base
        Usuario duenio = new Usuario();
        usuarioRepositorio.save(duenio);

        Mascota mascotaVotante = new Mascota();
        mascotaVotante.setUsuario(duenio);
        mascotaRepositorio.save(mascotaVotante);

        Mascota mascotaRecibe = new Mascota();
        mascotaRecibe.setUsuario(duenio);
        mascotaRepositorio.save(mascotaRecibe);

        // Creamos un voto ANTIGUO (hace 100 segundos)
        Voto votoAntiguo = new Voto();
        votoAntiguo.setMascota1(mascotaVotante);
        votoAntiguo.setMascota2(mascotaRecibe);
        votoAntiguo.setFecha(new Date(System.currentTimeMillis() - 100000));
        votoRepositorio.save(votoAntiguo);

        // Creamos un voto NUEVO (ahora mismo)
        Voto votoNuevo = new Voto();
        votoNuevo.setMascota1(mascotaVotante);
        votoNuevo.setMascota2(mascotaRecibe);
        votoNuevo.setFecha(new Date());
        votoRepositorio.save(votoNuevo);

        // 2. Act: Ejecutamos tus consultas personalizadas
        List<Voto> votosPropios = votoRepositorio.buscarVotosPropios(mascotaVotante.getId());
        List<Voto> votosRecibidos = votoRepositorio.buscarVotosRecibidos(mascotaRecibe.getId());

        // 3. Assert:
        // Verificamos que trajo exactamente los 2 votos
        assertEquals(2, votosPropios.size());
        assertEquals(2, votosRecibidos.size());

        // Verificamos el ORDEN (DESC): El índice 0 debe ser el voto más nuevo.
        assertEquals(votoNuevo.getId(), votosPropios.get(0).getId());
        assertEquals(votoAntiguo.getId(), votosPropios.get(1).getId());

        assertEquals(votoNuevo.getId(), votosRecibidos.get(0).getId());
    }
}