package com.example.tinder.repositorios;

import com.example.tinder.entidades.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UsuarioRepositorioTest {

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    @Test
    void buscarPorMail_MailExiste_RetornaUsuario() {
        // 1. Arrange: Guardamos un usuario REAL en la base de datos de prueba
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setNombre("Integracion");
        nuevoUsuario.setApellido("Test");
        nuevoUsuario.setEmail("test@integracion.com");

        usuarioRepositorio.save(nuevoUsuario);

        // 2. Act: Ejecuta la query de busccarpormail
        Usuario resultado = usuarioRepositorio.buscarPorMail("test@integracion.com");

        // 3. Assert: Verificamos que el query trajo los datos correctos de la BD
        assertNotNull(resultado);
        assertEquals("test@integracion.com", resultado.getEmail());
        assertEquals("Integracion", resultado.getNombre());
    }

    @Test
    void buscarPorMail_MailNoExiste_RetornaNull() {
        // 1. Arrange: Buscamos un correo que sabemos que no existe en esta transacción

        // 2. Act:
        Usuario resultado = usuarioRepositorio.buscarPorMail("noexiste@correo.com");

        // 3. Assert: El query debería devolver null porque no hay coincidencias
        assertNull(resultado);
    }
}