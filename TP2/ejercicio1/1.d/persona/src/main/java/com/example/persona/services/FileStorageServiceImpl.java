package com.example.persona.services;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.Normalizer;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    @Value("${biblioteca.storage.path}")
    private String rutaConfigurada;

    private Path rutaAlmacenamiento;

    @PostConstruct
    public void init() {
        try {
            this.rutaAlmacenamiento = Paths.get(this.rutaConfigurada).toAbsolutePath().normalize();
            Files.createDirectories(this.rutaAlmacenamiento);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo inicializar la carpeta de almacenamiento de biblioteca: " + this.rutaConfigurada, e);
        }
    }

    @Override
    public String almacenarPdfLibro(MultipartFile file, String tituloLibro) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("El archivo PDF a almacenar no puede estar vacío");
        }

        String nombreArchivo = generarNombreArchivo(tituloLibro);
        Path destino = this.rutaAlmacenamiento.resolve(nombreArchivo).normalize();

        // Verificación de seguridad de path traversal
        if (!destino.startsWith(this.rutaAlmacenamiento)) {
            throw new SecurityException("Intento de acceso fuera del directorio de biblioteca permitido");
        }

        try {
            Files.copy(file.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);
            return nombreArchivo;
        } catch (IOException e) {
            throw new Exception("Error al guardar el archivo PDF en el disco del servidor: " + e.getMessage(), e);
        }
    }

    @Override
    public Resource cargarArchivo(String nombreArchivo) throws Exception {
        try {
            Path archivo = this.rutaAlmacenamiento.resolve(nombreArchivo).normalize();

            if (!archivo.startsWith(this.rutaAlmacenamiento)) {
                throw new SecurityException("Acceso no autorizado al archivo solicitado");
            }

            Resource resource = new UrlResource(archivo.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new FileNotFoundException("El archivo PDF no existe o no es accesible: " + nombreArchivo);
            }
        } catch (MalformedURLException e) {
            throw new FileNotFoundException("URL inválida para el archivo: " + nombreArchivo);
        }
    }

    @Override
    public Path getRutaAlmacenamiento() {
        return this.rutaAlmacenamiento;
    }

    @Override
    public String generarNombreArchivo(String tituloLibro) {
        String base = (tituloLibro != null && !tituloLibro.trim().isEmpty()) ? tituloLibro.trim() : "libro";
        
        // Quitar tildes / diacríticos
        String normalizado = Normalizer.normalize(base, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

        // Convertir a minúsculas y reemplazar caracteres no alfanuméricos por guión bajo
        String sanitizado = normalizado.toLowerCase()
                .replaceAll("[^a-z0-9]", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_|_$", "");

        if (sanitizado.isEmpty()) {
            sanitizado = "sin_titulo";
        }

        // Formato requerido: libro_{nombrelibro}_.pdf
        return "libro_" + sanitizado + "_.pdf";
    }
}
