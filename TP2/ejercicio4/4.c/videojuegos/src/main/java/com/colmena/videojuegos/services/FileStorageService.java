package com.colmena.videojuegos.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${app.upload.dir:uploads/imagenes}")
    private String uploadDir;

    private Path uploadPath;

    @PostConstruct
    public void init() {
        this.uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadPath);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo inicializar el directorio de almacenamiento de imágenes: " + uploadPath, e);
        }
    }

    public Path getUploadPath() {
        if (this.uploadPath == null) {
            init();
        }
        return this.uploadPath;
    }

    public boolean validarExtension(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            return false;
        }

        String contentType = archivo.getContentType();
        if (contentType != null && contentType.toLowerCase().startsWith("image/")) {
            return true;
        }

        String originalFilename = archivo.getOriginalFilename();
        if (originalFilename != null) {
            String lower = originalFilename.toLowerCase();
            if (lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png")
                    || lower.endsWith(".gif") || lower.endsWith(".webp") || lower.endsWith(".bmp")) {
                return true;
            }
        }

        try (InputStream is = archivo.getInputStream()) {
            return ImageIO.read(is) != null;
        } catch (Exception e) {
            return false;
        }
    }

    public String guardarArchivo(MultipartFile archivo, String nombreArchivoPrevio) throws IOException {
        if (archivo == null || archivo.isEmpty()) {
            return nombreArchivoPrevio;
        }

        Path basePath = getUploadPath();
        Files.createDirectories(basePath);

        String originalFilename = archivo.getOriginalFilename();
        String extension = ".jpg";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        }

        String nuevoNombre = System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8) + extension;
        Path destino = basePath.resolve(nuevoNombre).normalize();

        try (InputStream inputStream = archivo.getInputStream()) {
            Files.copy(inputStream, destino, StandardCopyOption.REPLACE_EXISTING);
        }

        // Si se actualizó la imagen y había un archivo previo válido, lo eliminamos si no es default
        if (nombreArchivoPrevio != null && !nombreArchivoPrevio.trim().isEmpty() && !nombreArchivoPrevio.startsWith("http")) {
            try {
                Path archivoPrevio = basePath.resolve(nombreArchivoPrevio).normalize();
                if (Files.exists(archivoPrevio) && !archivoPrevio.equals(destino)) {
                    Files.deleteIfExists(archivoPrevio);
                }
            } catch (Exception ignored) {
            }
        }

        return nuevoNombre;
    }
}
