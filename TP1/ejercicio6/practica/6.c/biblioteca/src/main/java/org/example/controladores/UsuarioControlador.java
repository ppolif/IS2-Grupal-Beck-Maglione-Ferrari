package org.example.controladores;


import org.example.dtos.UsuarioRequestDTO;
import org.example.dtos.UsuarioResponseDTO;
import org.example.servicios.UsuarioServicioImpl;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/usuarios")
public class UsuarioControlador {

    private final UsuarioServicioImpl usuarioServicio;

    public UsuarioControlador(UsuarioServicioImpl usuarioServicio) {
        this.usuarioServicio = usuarioServicio;
    }

    // Endpoint público para que cualquiera pueda crearse una cuenta
    @PostMapping("/registro")
    public ResponseEntity<UsuarioResponseDTO> registrar(@Valid @RequestBody UsuarioRequestDTO dto) {
        return new ResponseEntity<>(usuarioServicio.registrarUsuario(dto), HttpStatus.CREATED);
    }
}
