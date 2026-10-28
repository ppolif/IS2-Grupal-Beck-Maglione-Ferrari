package com.example.tinder.mappers;

import com.example.tinder.dto.*;
import com.example.tinder.entidades.*;
import com.example.tinder.enumeraciones.Rol;
import com.example.tinder.enumeraciones.Sexo;
import com.example.tinder.enumeraciones.Tipo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class MapStructMappersTest {

    @Autowired
    private ZonaMapper zonaMapper;

    @Autowired
    private UsuarioMapper usuarioMapper;

    @Autowired
    private MascotaMapper mascotaMapper;

    @Autowired
    private VotoMapper votoMapper;

    @Autowired
    private FotoMapper fotoMapper;

    @Test
    void testZonaMapper() {
        Zona zona = new Zona();
        zona.setId("z-1");
        zona.setNombre("Zona Norte");
        zona.setDescripcion("Norte");

        ZonaDto dto = zonaMapper.toDto(zona);
        assertNotNull(dto);
        assertEquals("z-1", dto.getId());
        assertEquals("Zona Norte", dto.getNombre());
        assertEquals("Norte", dto.getDescripcion());

        ZonaRequestDto req = new ZonaRequestDto("Sur", "Zona Sur");
        Zona entity = zonaMapper.toEntity(req);
        assertNotNull(entity);
        assertEquals("Sur", entity.getNombre());
    }

    @Test
    void testUsuarioMapper() {
        Zona zona = new Zona();
        zona.setId("z-2");
        zona.setNombre("Centro");

        Foto foto = new Foto();
        foto.setId("f-1");
        foto.setNombre("perfil.jpg");

        Usuario usuario = new Usuario();
        usuario.setId("u-1");
        usuario.setNombre("Carlos");
        usuario.setApellido("Gomez");
        usuario.setEmail("carlos@mail.com");
        usuario.setRol(Rol.USER);
        usuario.setZona(zona);
        usuario.setFoto(foto);
        usuario.setAlta(new Date());

        UsuarioDto dto = usuarioMapper.toDto(usuario);
        assertNotNull(dto);
        assertEquals("u-1", dto.getId());
        assertEquals("Carlos", dto.getNombre());
        assertEquals("carlos@mail.com", dto.getEmail());
        assertEquals("USER", dto.getRol());
        assertEquals("f-1", dto.getFotoId());
        assertNotNull(dto.getZona());
        assertEquals("z-2", dto.getZona().getId());
    }

    @Test
    void testMascotaMapper() {
        Usuario usuario = new Usuario();
        usuario.setId("u-10");
        usuario.setNombre("Ana");

        Foto foto = new Foto();
        foto.setId("f-2");

        Mascota mascota = new Mascota();
        mascota.setId("m-1");
        mascota.setNombre("Bobby");
        mascota.setSexo(Sexo.MACHO);
        mascota.setTipo(Tipo.PERRO);
        mascota.setUsuario(usuario);
        mascota.setFoto(foto);

        MascotaDto dto = mascotaMapper.toDto(mascota);
        assertNotNull(dto);
        assertEquals("m-1", dto.getId());
        assertEquals("Bobby", dto.getNombre());
        assertEquals(Sexo.MACHO, dto.getSexo());
        assertEquals(Tipo.PERRO, dto.getTipo());
        assertEquals("u-10", dto.getUsuarioId());
        assertEquals("Ana", dto.getUsuarioNombre());
        assertEquals("f-2", dto.getFotoId());
    }

    @Test
    void testVotoMapper() {
        Mascota m1 = new Mascota();
        m1.setId("m-1");
        m1.setNombre("Rex");

        Mascota m2 = new Mascota();
        m2.setId("m-2");
        m2.setNombre("Lola");

        Voto voto = new Voto();
        voto.setId("v-1");
        voto.setFecha(new Date());
        voto.setMascota1(m1);
        voto.setMascota2(m2);

        VotoDto dto = votoMapper.toDto(voto);
        assertNotNull(dto);
        assertEquals("v-1", dto.getId());
        assertEquals("m-1", dto.getMascota1Id());
        assertEquals("Rex", dto.getMascota1Nombre());
        assertEquals("m-2", dto.getMascota2Id());
        assertEquals("Lola", dto.getMascota2Nombre());
    }
}

