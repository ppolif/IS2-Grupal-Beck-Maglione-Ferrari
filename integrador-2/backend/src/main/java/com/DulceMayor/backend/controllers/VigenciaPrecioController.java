package com.DulceMayor.backend.controllers;

import com.DulceMayor.backend.dtos.VigenciaPrecioRequest;
import com.DulceMayor.backend.dtos.VigenciaPrecioResponse;
import com.DulceMayor.backend.services.VigenciaPrecioService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * Controlador REST para el historial y administración de vigencias de precios.
 * este controlador no extiende del controlador base ya que sus URL estan anidadas por ID de Producto y como el controlador base recibe DTO asumiendo el recurso directo, no lo puede heredar.
 */
@RestController
@RequestMapping
public class VigenciaPrecioController {

    private final VigenciaPrecioService vigenciaPrecioService;

    public VigenciaPrecioController(VigenciaPrecioService vigenciaPrecioService) {
        this.vigenciaPrecioService = vigenciaPrecioService;
    }

    @GetMapping({"/productos/{id}/vigencias-precio", "/api/v1/productos/{id}/vigencias-precio"})
    @PreAuthorize("hasAnyRole('JEFE', 'ADMINISTRATIVO')")
    public ResponseEntity<List<VigenciaPrecioResponse>> listarVigenciaPrecio(@PathVariable String id) {
        return ResponseEntity.ok(vigenciaPrecioService.listarPorProducto(id));
    }

    @PostMapping({"/productos/{id}/vigencias-precio", "/api/v1/productos/{id}/vigencias-precio"})
    @PreAuthorize("hasAnyRole('JEFE', 'ADMINISTRATIVO')")
    public ResponseEntity<VigenciaPrecioResponse> crearVigenciaPrecio(
            @PathVariable String id,
            @Valid @RequestBody VigenciaPrecioRequest request) {//@RequestBody extrae el cuerpo de la solicitud HTTP y lo convierte en un objeto Java
        VigenciaPrecioResponse response = vigenciaPrecioService.registrarPrecio(id, request);
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()//esta parte devuelve una respuesta http correcta con el código de estado 201 (creado) y la ubicación del recurso recién creado en la cabecera Location
                .path("/vigencias-precio/{id}")//fromCurrentContextPath(): Toma la URL base del servidor de la petición actual, .path("/vigencias-precio/{id}"): Le añade la ruta específica del recurso, dejando un espacio dinámico {id}
                .buildAndExpand(response.id())//.buildAndExpand(response.id()): Reemplaza el {id} con el identificador real del objeto recién creado (response.id())
                .toUri();//.toUri(): Convierte todo esto en un objeto de tipo URI
        return ResponseEntity.created(location).body(response);// .created(location): Genera un estado HTTP 201 (Created).  .body(response): Incluye el objeto creado (response) en el cuerpo de la respuesta 
    }

    @PutMapping({"/vigencias-precio/{id}", "/api/v1/vigencias-precio/{id}"})
    @PreAuthorize("hasAnyRole('JEFE', 'ADMINISTRATIVO')")
    public ResponseEntity<VigenciaPrecioResponse> modificarVigenciaPrecio(
            @PathVariable String id,
            @Valid @RequestBody VigenciaPrecioRequest request) {
        return ResponseEntity.ok(vigenciaPrecioService.modificar(id, request));
    }

    @DeleteMapping({"/vigencias-precio/{id}", "/api/v1/vigencias-precio/{id}"})
    @PreAuthorize("hasAnyRole('JEFE', 'ADMINISTRATIVO')")
    public ResponseEntity<Void> eliminarVigenciaPrecio(@PathVariable String id) {
        vigenciaPrecioService.eliminar(id);
        return ResponseEntity.noContent().build(); //se devuelve una respuesta sin contenido porque el recurso fue borrado (204 No Content)
    }
}

