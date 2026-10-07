package com.example.persona.services;

import com.example.persona.dtos.LibroDto;
import com.example.persona.entities.Libro;
import com.example.persona.repositories.BaseRepository;
import com.example.persona.repositories.LibroRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Optional;

@Service
public class LibroServiceImpl extends BaseServiceImpl<Libro, LibroDto, Long> implements LibroService {

    @Autowired
    private LibroRepository libroRepository;

    @Autowired
    private FileStorageService fileStorageService;

    public LibroServiceImpl(BaseRepository<Libro, Long> baseRepository, FileStorageService fileStorageService) {
        super(baseRepository, Libro.class, LibroDto.class);
        this.fileStorageService = fileStorageService;
    }

    @Override
    @Transactional
    public LibroDto saveConArchivo(LibroDto dto, MultipartFile file) throws Exception {
        try {
            if (file != null && !file.isEmpty()) {
                String nombreArchivo = fileStorageService.almacenarPdfLibro(file, dto.getTitulo());
                dto.setNombreArchivoPdf(nombreArchivo);
            }
            return this.save(dto);
        } catch (Exception e) {
            throw new Exception("Error al guardar el libro con su archivo PDF: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public LibroDto updateConArchivo(Long id, LibroDto dto, MultipartFile file) throws Exception {
        try {
            Optional<Libro> optLibro = libroRepository.findById(id);
            if (optLibro.isEmpty()) {
                throw new FileNotFoundException("No existe el libro con id: " + id);
            }
            Libro libroExistente = optLibro.get();
            if (file != null && !file.isEmpty()) {
                String nombreArchivo = fileStorageService.almacenarPdfLibro(file, dto.getTitulo());
                dto.setNombreArchivoPdf(nombreArchivo);
            } else {
                dto.setNombreArchivoPdf(libroExistente.getNombreArchivoPdf());
            }
            return this.update(id, dto);
        } catch (Exception e) {
            throw new Exception("Error al actualizar el libro: " + e.getMessage(), e);
        }
    }

    @Override
    public Resource getPdfAsResource(Long id) throws Exception {
        try {
            Optional<Libro> optionalLibro = libroRepository.findById(id);
            if (optionalLibro.isEmpty()) {
                throw new FileNotFoundException("No existe el libro con id: " + id);
            }

            Libro libro = optionalLibro.get();
            String nombreArchivo = libro.getNombreArchivoPdf();

            if (nombreArchivo == null || nombreArchivo.trim().isEmpty()) {
                throw new FileNotFoundException("El libro '" + libro.getTitulo() + "' (ID: " + id + ") no tiene ningún archivo PDF asignado.");
            }

            return fileStorageService.cargarArchivo(nombreArchivo);
        } catch (Exception e) {
            throw new Exception(e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public List<LibroDto> search(String filtro) throws Exception {
        try {
            List<Libro> libros = libroRepository.search(filtro);
            return libros.stream().map(this::toDto).toList();
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    @Transactional
    public Page<LibroDto> search(String filtro, Pageable pageable) throws Exception {
        try {
            Page<Libro> libros = libroRepository.search(filtro, pageable);
            return libros.map(this::toDto);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }
}
