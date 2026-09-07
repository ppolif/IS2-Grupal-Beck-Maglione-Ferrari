package com.colmena.videojuegos.services;

import com.colmena.videojuegos.dtos.VideojuegoRequestDTO;
import com.colmena.videojuegos.dtos.VideojuegoResponseDTO;
import com.colmena.videojuegos.entities.Categoria;
import com.colmena.videojuegos.entities.Estudio;
import com.colmena.videojuegos.entities.Videojuego;
import com.colmena.videojuegos.repositories.RepositorioCategoria;
import com.colmena.videojuegos.repositories.RepositorioEstudio;
import com.colmena.videojuegos.repositories.RepositorioVideojuego;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ServicioVideojuego implements ServicioBase<Videojuego> {

    @Autowired
    private RepositorioVideojuego repositorioVideojuego;

    @Autowired
    private RepositorioCategoria repositorioCategoria;

    @Autowired
    private RepositorioEstudio repositorioEstudio;

    public ServicioVideojuego(RepositorioVideojuego repositorioVideojuego) {
        this.repositorioVideojuego = repositorioVideojuego;
    }

    @Override
    @Transactional
    public List<Videojuego> findAll() throws Exception {
        try {
            return this.repositorioVideojuego.findAll();
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    @Transactional
    public Videojuego findById(long id) throws Exception {
        try {
            Optional<Videojuego> opt = this.repositorioVideojuego.findById(id);
            return opt.get();
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    @Transactional
    public Videojuego saveOne(Videojuego entity) throws Exception {
        try {
            return this.repositorioVideojuego.save(entity);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    @Transactional
    public Videojuego updateOne(Videojuego entity, long id) throws Exception {
        try {
            Optional<Videojuego> opt = this.repositorioVideojuego.findById(id);
            Videojuego videojuego = opt.get();
            videojuego = this.repositorioVideojuego.save(entity);
            return videojuego;
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    @Transactional
    public boolean deleteById(long id) throws Exception {
        try {
            Optional<Videojuego> opt = this.repositorioVideojuego.findById(id);
            if (!opt.isEmpty()) {
                this.repositorioVideojuego.delete(opt.get());
                return true;
            } else {
                throw new Exception();
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Transactional
    public List<VideojuegoResponseDTO> listarDTO() throws Exception {
        return this.findAll().stream()
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public VideojuegoResponseDTO buscarPorIdDTO(Long id) throws Exception {
        Videojuego videojuego = this.findById(id);
        return convertirAResponseDTO(videojuego);
    }

    @Transactional
    public List<VideojuegoResponseDTO> buscarPorTituloDTO(String titulo) throws Exception {
        return this.repositorioVideojuego.findByTitle(titulo).stream()
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public VideojuegoRequestDTO obtenerParaEdicion(Long id) throws Exception {
        Videojuego videojuego = this.findById(id);
        VideojuegoRequestDTO dto = new VideojuegoRequestDTO();
        dto.setId(videojuego.getId());
        dto.setTitulo(videojuego.getTitulo());
        dto.setDescripcion(videojuego.getDescripcion());
        dto.setPrecio(videojuego.getPrecio());
        dto.setStock(videojuego.getStock());
        dto.setFechaLanzamiento(videojuego.getFechaLanzamiento());
        if (videojuego.getCategoria() != null) {
            dto.setIdCategoria(videojuego.getCategoria().getId());
        }
        if (videojuego.getEstudio() != null) {
            dto.setIdEstudio(videojuego.getEstudio().getId());
        }
        return dto;
    }

    @Transactional
    public Videojuego guardarDesdeDTO(VideojuegoRequestDTO dto) throws Exception {
        Videojuego videojuego;

        if (dto.getId() != null && dto.getId() > 0) {
            videojuego = this.findById(dto.getId());
        } else {
            videojuego = new Videojuego();
        }

        videojuego.setTitulo(dto.getTitulo());
        videojuego.setDescripcion(dto.getDescripcion());
        videojuego.setPrecio(dto.getPrecio());
        videojuego.setStock(dto.getStock());
        videojuego.setFechaLanzamiento(dto.getFechaLanzamiento());

        if (dto.getIdCategoria() != null) {
            Categoria categoria = repositorioCategoria.findById(dto.getIdCategoria())
                    .orElseThrow(() -> new Exception("Categoría no encontrada"));
            videojuego.setCategoria(categoria);
        }

        if (dto.getIdEstudio() != null) {
            Estudio estudio = repositorioEstudio.findById(dto.getIdEstudio())
                    .orElseThrow(() -> new Exception("Estudio no encontrado"));
            videojuego.setEstudio(estudio);
        }

        MultipartFile imagen = dto.getImagen();
        if (imagen != null && !imagen.isEmpty()) {
            String originalFilename = imagen.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String nombreFoto = Calendar.getInstance().getTimeInMillis() + extension;

            Path rutaDirectorio = Paths.get("imagenes").toAbsolutePath();
            if (!Files.exists(rutaDirectorio)) {
                Files.createDirectories(rutaDirectorio);
            }

            Path rutaCompleta = rutaDirectorio.resolve(nombreFoto);
            Files.write(rutaCompleta, imagen.getBytes());
            videojuego.setImagen(nombreFoto);
        }

        return this.repositorioVideojuego.save(videojuego);
    }

    public VideojuegoResponseDTO convertirAResponseDTO(Videojuego videojuego) {
        if (videojuego == null) {
            return null;
        }

        VideojuegoResponseDTO dto = new VideojuegoResponseDTO();
        dto.setId(videojuego.getId());
        dto.setTitulo(videojuego.getTitulo());
        dto.setDescripcion(videojuego.getDescripcion());
        dto.setPrecio(videojuego.getPrecio());
        dto.setStock(videojuego.getStock());
        dto.setImagen(videojuego.getImagen());

        if (videojuego.getCategoria() != null) {
            dto.setIdCategoria(videojuego.getCategoria().getId());
            dto.setNombreCategoria(videojuego.getCategoria().getNombre());
        }

        if (videojuego.getEstudio() != null) {
            dto.setIdEstudio(videojuego.getEstudio().getId());
            dto.setNombreEstudio(videojuego.getEstudio().getNombre());
        }

        return dto;
    }
}
