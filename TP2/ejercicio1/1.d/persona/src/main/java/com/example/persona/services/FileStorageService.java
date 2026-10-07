package com.example.persona.services;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

public interface FileStorageService {
    String almacenarPdfLibro(MultipartFile file, String tituloLibro) throws Exception;
    Resource cargarArchivo(String nombreArchivo) throws Exception;
    Path getRutaAlmacenamiento();
    String generarNombreArchivo(String tituloLibro);
}
